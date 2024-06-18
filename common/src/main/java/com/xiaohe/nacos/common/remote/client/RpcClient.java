package com.xiaohe.nacos.common.remote.client;

import com.xiaohe.nacos.api.common.Constants;
import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.remote.request.ClientDetectionRequest;
import com.xiaohe.nacos.api.remote.request.ConnectResetRequest;
import com.xiaohe.nacos.api.remote.request.HealthCheckRequest;
import com.xiaohe.nacos.api.remote.request.Request;
import com.xiaohe.nacos.api.remote.response.ClientDetectionResponse;
import com.xiaohe.nacos.api.remote.response.ConnectResetResponse;
import com.xiaohe.nacos.api.remote.response.ErrorResponse;
import com.xiaohe.nacos.api.remote.response.Response;
import com.xiaohe.nacos.common.lifecycle.Closeable;
import com.xiaohe.nacos.common.remote.ConnectionType;
import com.xiaohe.nacos.common.remote.PayloadRegistry;
import com.xiaohe.nacos.common.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xiaohe.nacos.api.exception.NacosException.SERVER_ERROR;

/**
 * 客户端的顶级父类，grpc 和 http 都实现它
 */
public abstract class RpcClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    /**
     * nacos 服务器地址管理器
     */
    private ServerListFactory serverListFactory;

    /**
     * 并发安全更新客户端的状态
     */
    protected volatile AtomicReference<RpcClientStatus> rpcClientStatus = new AtomicReference<>(RpcClientStatus.STARTING.WAIT_INIT);

    /**
     * 和服务端的连接对象
     */
    protected volatile Connection currentConnection;

    /**
     * 表示租户信息
     */
    private String tenant;

    /**
     * 最新的收到服务器信息的时间戳
     */
    private long lastActiveTimeStamp;


    protected List<ServerRequestHandler> serverRequestHandlers = new ArrayList<>();

    private static final Pattern EXCLUDE_PROTOCOL_PATTERN = Pattern.compile("(?<=\\w{1,5}://)(.*)");

    /**
     * 客户端配置
     */
    protected RpcClientConfig rpcClientConfig;

    /**
     * 存放连接事件的阻塞队列
     */
    protected BlockingQueue<ConnectionEvent> eventLinkedBlockingQueue = new LinkedBlockingQueue<>();

    /**
     * 客户端事件执行器，执行 健康检查、连接重建
     */
    protected ScheduledExecutorService clientEventExecutor;

    private final BlockingQueue<ReconnectContext> reconnectionSignal = new ArrayBlockingQueue<>(1);

    /**
     * 连接事件监听器
     */
    protected List<ConnectionEventListener> connectionEventListeners = new ArrayList<>();

    static {
        PayloadRegistry.init();
    }

    public RpcClient(RpcClientConfig rpcClientConfig) {

    }

    public RpcClient(RpcClientConfig rpcClientConfig, ServerListFactory serverListFactory) {
        this.rpcClientConfig = rpcClientConfig;
        this.serverListFactory = serverListFactory;
        // 将客户端状态从未初始化改为已初始化
        init();
    }

    /**
     * 将客户端状态从未初始化改为已初始化
     */
    protected void init() {
        if (this.serverListFactory != null) {
            rpcClientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED);
            LoggerUtils.printIfInfoEnabled(logger, "RpcClient init in constructor, ServerListFactory = {}",
                    serverListFactory.getClass().getName());
        }
    }

    /**
     * 设置服务器地址并将客户端状态从未初始化改为已初始化
     *
     * @param serverListFactory
     * @return
     */
    public RpcClient serverListFactory(ServerListFactory serverListFactory) {
        if (!isWaitInitiated()) {
            return this;
        }
        this.serverListFactory = serverListFactory;
        rpcClientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED);
        return this;
    }

    public final void start() throws NacosException {
        boolean success = rpcClientStatus.compareAndSet(RpcClientStatus.INITIALIZED, RpcClientStatus.STARTING);
        if (!success) {
            return;
        }
        // clientEventExecutor 一共有两个线程，它俩的任务都会在下面赋予
        clientEventExecutor = new ScheduledThreadPoolExecutor(2, r -> {
            Thread t = new Thread(r);
            t.setName("com.alibaba.nacos.client.remote.worker");
            ;
            t.setDaemon(true);
            return t;
        });
        // clientEventExecutor第一个任务: 处理 连接/断开连接 事件
        clientEventExecutor.submit(() -> {
            // 只要当前线程池没有被关闭就一直执行
            while (!clientEventExecutor.isTerminated() && !clientEventExecutor.isShutdown()) {
                ConnectionEvent take;
                try {
                    // 从存放连接事件的队列中取出事件，若无事件, 该线程就可以阻塞在这
                    take = eventLinkedBlockingQueue.take();
                    if (take.isConnected()) {
                        notifyConnected(take.connection);
                    } else if (take.isDisConnected()) {
                        notifyDisConnected(take.connection);
                    }
                } catch (Throwable e) {

                }
            }
        });
        // clientEventExecutor第二个事件: 处理重连任务
        clientEventExecutor.submit(() -> {
            while (true) {
                try {
                    if (isShutdown()) {
                        break;
                    }
                    // 从队列中拿重连任务，任务为空不需要重连
                    ReconnectContext reconnectContext = reconnectionSignal.poll(rpcClientConfig.connectionKeepAlive(), TimeUnit.MILLISECONDS);
                    // ReconnectContext有两个来源:
                    // 1. 自己几次连接失败，于是开启异步重连，这时的 ServerInfo 为空，随便选一个ServerInfo就可以连
                    // 2. 服务端主动让客户端重连，ServerInfo 可能为空
                    if (reconnectContext == null) {
                        // 最近一次通信到现在超出了keepAlive的时间，就做一次健康检查，没有超出就下一轮从队列取任务
                        if (System.currentTimeMillis() - lastActiveTimeStamp < rpcClientConfig.connectionKeepAlive()) {
                            continue;
                        }
                        boolean isHealthy = healthyCheck();
                        if (isHealthy) {
                            lastActiveTimeStamp = System.currentTimeMillis();
                            continue;
                        }
                        // 健康检查失败有两种情况:
                        // 1. 正常情况，断联了
                        // 2. 线程刚启动，从阻塞队列获取的值为空，此时判断连接是否建立，没建立就算了
                        if (currentConnection == null) {
                            continue;
                        }
                        // 如果客户端已经关闭连接，跳出循环
                        RpcClientStatus rpcClientStatus = RpcClient.this.rpcClientStatus.get();
                        if (RpcClientStatus.SHUTDOWN.equals(rpcClientStatus)) {
                            break;
                        }
                        boolean statusFlowSuccess = RpcClient.this.rpcClientStatus.compareAndSet(rpcClientStatus, RpcClientStatus.UNHEALTHY);
                        if (statusFlowSuccess) {
                            reconnectContext = new ReconnectContext(null, false);
                        } else {
                            continue;
                        }
                    }
                    // 这个重连对象的 serverInfo 是服务端发送给客户端的，如果服务端压力过大，会让一些客户端连接其他服务端
                    if (reconnectContext.serverInfo != null) {
                        boolean serverExist = false;
                        for (String server : getServerListFactory().getServerList()) {
                            ServerInfo serverInfo = resolveServerInfo(server);
                            if (serverInfo.getServerIp().equals(reconnectContext.serverInfo.getServerIp())) {
                                serverExist = true;
                                reconnectContext.serverInfo.serverPort = serverInfo.serverPort;
                                break;
                            }
                        }
                        if (!serverExist) {
                            LoggerUtils.printIfInfoEnabled(logger, "[{}] Recommend server is not in server list, ignore recommend server {}", rpcClientConfig.name(), reconnectContext.serverInfo.getAddress());
                            // 把重连上下文中的服务信息清除了，后面会有操作，判断这个reconnectContext.serverInfo是否为null
                            // 如果为null，就直接从客户端的服务地址管理器中获取一个地址
                            reconnectContext.serverInfo = null;
                        }
                    }
                    reconnect(reconnectContext.serverInfo, reconnectContext.onRequestFail);
                } catch (Throwable t) {

                }
            }
        });

        // 客户端连接服务端
        Connection connectToServer = null;
        rpcClientStatus.set(RpcClientStatus.STARTING);
        // 连接失败的重试次数
        int startUpRetryTimes = rpcClientConfig.retryTimes();
        while (startUpRetryTimes >= 0 && connectToServer == null) {
            try {
                startUpRetryTimes--;
                ServerInfo serverInfo = nextRpcServer();
                // 调用子类的连接方法建立连接
                connectToServer = connectToServer(serverInfo);
            } catch (Throwable e) {
                LoggerUtils.printIfWarnEnabled(logger, "[{}] Fail to connect to server on start up, error message = {}, start up retry times left: {}", rpcClientConfig.name(), e.getMessage(), startUpRetryTimes, e);
            }
        }
        // 退出循环两种可能:
        // 1. 连接成功，更改状态，向阻塞队列添加连接成功事件
        // 2. 连接失败，说明重试次数已经用光
        if (connectToServer != null) {
            this.currentConnection = connectToServer;
            rpcClientStatus.set(RpcClientStatus.RUNNING);
            // 向阻塞队列中添加已连接事件
            eventLinkedBlockingQueue.offer(new ConnectionEvent(ConnectionEvent.CONNECTED, currentConnection));
        } else {
            // 异步连接服务端
            switchServerAsync();
        }
        // 注册一个处理器，用于处理重连请求（服务端给客户端发送的）
        registerServerRequestHandler(new ConnectResetRequestHandler());
        // 注册一个处理器，用于处理 ClientDetectionRequest
        registerServerRequestHandler((request, connection) -> {
            if (request instanceof ClientDetectionRequest) {
                return new ClientDetectionResponse();
            }
            return null;
        });

    }

    /**
     * 与服务器建立连接
     *
     * @param serverInfo
     * @return
     * @throws Exception
     */
    public abstract Connection connectToServer(ServerInfo serverInfo) throws Exception;

    public abstract int rpcPortOffset();

    public abstract ConnectionType getConnectionType();


    /**
     * 客户端与服务端成功建立连接, 通知那些订阅了事件的listener
     *
     * @param connection
     */
    protected void notifyConnected(Connection connection) {
        // 如果没有监听者就算了
        if (connectionEventListeners.isEmpty()) {
            return;
        }
        for (ConnectionEventListener connectionEventListener : connectionEventListeners) {
            try {
                connectionEventListener.onConnected(connection);
            } catch (Throwable e) {
                LoggerUtils.printIfErrorEnabled(logger, "[{}] Notify connect listener error, listener = {}", rpcClientConfig.name(), connectionEventListener.getClass().getName());
            }
        }
    }

    protected void notifyDisConnected(Connection connection) {
        if (connectionEventListeners.isEmpty()) {
            return;
        }
        for (ConnectionEventListener connectionEventListener : connectionEventListeners) {
            try {
                connectionEventListener.onDisConnect(connection);
            } catch (Throwable e) {
                LoggerUtils.printIfErrorEnabled(logger, "[{}] Notify connect listener error, listener = {}", rpcClientConfig.name(), connectionEventListener.getClass().getName());
            }
        }
    }

    // 通过服务地址管理器得到下一个nacos服务器地址的方法
    protected ServerInfo nextRpcServer() {
        String serverAddress = getServerListFactory().genNextServer();
        return resolveServerInfo(serverAddress);
    }

    private ServerInfo resolveServerInfo(String serverAddress) {
        // 使用正则表达式移除服务地址中的协议部分
        Matcher matcher = EXCLUDE_PROTOCOL_PATTERN.matcher(serverAddress);
        if (matcher.find()) {
            serverAddress = matcher.group(1);
        }
        // 下面就是得到具体的ip地址和端口号的操作了
        String[] ipPortTuple = InternetAddressUtil.splitIPPortStr(serverAddress);
        int defaultPort = Integer.parseInt(System.getProperty("nacos.server.port", "8848"));
        String serverPort = CollectionUtils.getOrDefault(ipPortTuple, 1, Integer.toString(defaultPort));
        // 最后把地址封装在ServerInfo对象中
        return new ServerInfo(ipPortTuple[0], NumberUtils.toInt(serverPort, defaultPort));
    }

    /**
     * 跟已经建立连接的服务端做一次健康检查
     *
     * @return
     */
    private boolean healthyCheck() {
        HealthCheckRequest healthCheckRequest = new HealthCheckRequest();
        if (this.currentConnection == null) {
            return false;
        }
        int retryTimes = rpcClientConfig.healthCheckRetryTimes();
        Random random = new Random();
        while (retryTimes >= 0) {
            retryTimes--;
            try {
                if (retryTimes > 1) {
                    Thread.sleep(random.nextInt(500));
                }
                Response response = this.currentConnection.request(healthCheckRequest, rpcClientConfig.healthCheckTimeOut());
                return response != null && response.isSuccess();
            } catch (Exception e) {

            }
        }
        return false;
    }

    /**
     * 异步连接服务器
     * 其实就是创建一个 ReconnectContext 放到阻塞队列中, clientEventExecutor 会扫描这个队列进行连接重建
     */
    public void switchServerAsync() {
        switchServerAsync(null, false);
    }

    /**
     * 请求发送失败时重建连接
     */
    public void switchServerAsyncOnRequestFail() {
        switchServerAsync(null, true);
    }

    public void switchServerAsync(final ServerInfo recommendServerInfo, boolean onRequestFail) {
        reconnectionSignal.offer(new ReconnectContext(recommendServerInfo, onRequestFail));
    }

    /**
     * 接收到服务端发送的请求，遍历 serverRequestHandlers 处理，当然，只有一个会处理
     *
     * @param request
     * @return
     */
    protected Response handleServerRequest(final Request request) {
        lastActiveTimeStamp = System.currentTimeMillis();
        for (ServerRequestHandler serverRequestHandler : serverRequestHandlers) {
            try {
                Response response = serverRequestHandler.requestReply(request, currentConnection);
                if (response != null) {
                    return response;
                }
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    /**
     * 注册一个请求处理器
     *
     * @param serverRequestHandler
     */
    public synchronized void registerServerRequestHandler(ServerRequestHandler serverRequestHandler) {
        this.serverRequestHandlers.add(serverRequestHandler);
    }

    /**
     * 注册一个连接事件监听器
     *
     * @param connectionEventListener
     */
    public synchronized void registerConnectionListener(ConnectionEventListener connectionEventListener) {
        this.connectionEventListeners.add(connectionEventListener);
    }

    /**
     * 连接重建
     *
     * @param recommendServerInfo
     * @param onRequestFail       此次重连是否因为客户端与服务端连接失败
     */
    protected void reconnect(final ServerInfo recommendServerInfo, boolean onRequestFail) {
        try {
            AtomicReference<ServerInfo> recommendServer = new AtomicReference<>(recommendServerInfo);
            // 再次做健康检测，如果成功说明之前的请求有点问题，现在没了
            if (onRequestFail && healthyCheck()) {
                rpcClientStatus.set(RpcClientStatus.RUNNING);
                return;
            }
            // 走到这里说明确实需要重连
            boolean switchSuccess = false;
            int reConnectTimes = 0;
            int retryTurns = 0;
            Exception lastException;
            while (!switchSuccess && !isShutdown()) {
                ServerInfo serverInfo = null;
                try {
                    // 如果传入的 ServerInfo 为空，就用自己集合里的
                    serverInfo = recommendServer.get() == null ? nextRpcServer() : recommendServer.get();
                    Connection connectionNew = connectToServer(serverInfo);
                    // 如果新连接建立成功
                    if (connectionNew != null) {
                        if (currentConnection != null) {
                            currentConnection.setAbandon(true);
                            closeConnection(currentConnection);
                        }
                        currentConnection = connectionNew;
                        rpcClientStatus.set(RpcClientStatus.RUNNING);
                        switchSuccess = true;
                        eventLinkedBlockingQueue.add(new ConnectionEvent(ConnectionEvent.CONNECTED, currentConnection));
                        return;
                    }
                    // 如果连接建立失败, 且客户端没关闭，就关掉连接
                    if (isShutdown()) {
                        closeConnection(currentConnection);
                    }
                    lastException = null;
                } catch (Exception e) {
                    lastException = e;
                } finally {
                    recommendServer.set(null);
                }
                if (CollectionUtils.isEmpty(RpcClient.this.serverListFactory.getServerList())) {
                    throw new Exception("server list is empty");
                }
                // 如果试了一轮都没有建立成功
                if (reConnectTimes > 0 && reConnectTimes % RpcClient.this.serverListFactory.getServerList().size() == 0) {
                    if (Integer.MAX_VALUE == retryTurns) {
                        retryTurns = 50;
                    } else {
                        retryTurns++;
                    }
                }
                reConnectTimes++;
                try {
                    if (!isRunning()) {
                        Thread.sleep(Math.min(retryTurns + 1, 50) * 100L);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (isShutdown()) {
                LoggerUtils.printIfInfoEnabled(logger, "[{}] Client is shutdown, stop reconnect to server", rpcClientConfig.name());
            }

        } catch (Exception e) {
            LoggerUtils.printIfWarnEnabled(logger, "[{}] Fail to reconnect to server, error is {}", rpcClientConfig.name(), e);
        }
    }

    /**
     * 客户端向服务端发送请求
     *
     * @param request
     * @return
     * @throws NacosException
     */
    public Response request(Request request) throws NacosException {
        return request(request, rpcClientConfig.timeOutMills());
    }

    public Response request(Request request, long timeoutMills) throws NacosException {
        int retryTimes = 0;
        Response response;
        Throwable exceptionThrow = null;
        long start = System.currentTimeMillis();

        while (retryTimes <= rpcClientConfig.retryTimes() && (timeoutMills <= 0 || System.currentTimeMillis() < timeoutMills + start)) {
            boolean waitReconnect = false;
            try {
                // 如果当前客户端与服务端的连接为空，或者客户端已经停止, 抛出异常进行连接重建
                if (this.currentConnection == null || !isRunning()) {
                    waitReconnect = true;
                    throw new NacosException(NacosException.CLIENT_DISCONNECT, "Client not connected, current status:" + rpcClientStatus.get());
                }
                // 调用 connection 进行请求的发送
                response = this.currentConnection.request(request, timeoutMills);
                // 得到response，如果为空直接抛异常
                // 如果response的code说连接未建立，可以异步进行连接
                if (response == null) {
                    throw new NacosException(SERVER_ERROR, "Unknown Exception.");
                }
                if (response instanceof ErrorResponse) {
                    if (response.getErrorCode() == NacosException.UN_REGISTER) {
                        synchronized (this) {
                            waitReconnect = true;
                            if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
                                LoggerUtils.printIfErrorEnabled(logger, "Connection is unregistered, switch server, connectionId = {}, request = {}", currentConnection.getConnectionId(), request.getClass().getSimpleName());
                                switchServerAsync();
                            }
                        }
                    }
                    throw new NacosException(response.getErrorCode(), response.getMessage());
                }
                lastActiveTimeStamp = System.currentTimeMillis();
                return response;
            } catch (Exception e) {
                if (waitReconnect) {
                    try {
                        Thread.sleep(Math.min(100, timeoutMills / 3));
                    } catch (Exception exception) {

                    }
                }
                exceptionThrow = e;
            }
            retryTimes++;
        }
        // 发送这么多次都没有发出去，肯定是连接出问题了, 因为上面在发送时已经判断了连接的问题，结果一直连不上
        if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            switchServerAsyncOnRequestFail();
        }
        if (exceptionThrow != null) {
            throw (exceptionThrow instanceof NacosException) ? (NacosException) exceptionThrow : new NacosException(SERVER_ERROR, exceptionThrow);
        } else {
            throw new NacosException(SERVER_ERROR, "Request fail, unknown Error");
        }

    }


    /**
     * 关闭客户端与服务端的连接
     *
     * @param connection
     */
    private void closeConnection(Connection connection) {
        if (connection != null) {
            connection.close();
            eventLinkedBlockingQueue.add(new ConnectionEvent(ConnectionEvent.DISCONNECTED, connection));
        }
    }

    //得到客户端名称
    public String getName() {
        return rpcClientConfig.name();
    }

    //得到客户端附加信息的方法
    public Map<String, String> getLabels() {
        return rpcClientConfig.labels();
    }

    //得到租户信息的方法
    public String getTenant() {
        return tenant;
    }

    // 设置租户信息
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    protected ServerInfo currentRpcServer() {
        String serverAddress = getServerListFactory().getCurrentServer();
        return resolveServerInfo(serverAddress);
    }

    public ServerInfo getCurrentServer() {
        if (this.currentConnection != null) {
            return currentConnection.serverInfo;
        }
        return null;
    }


    /**
     * 关闭客户端
     *
     * @throws NacosException
     */
    @Override
    public void shutdown() throws NacosException {
        rpcClientStatus.set(RpcClientStatus.SHUTDOWN);
        if (clientEventExecutor != null) {
            clientEventExecutor.shutdownNow();
        }
        closeConnection(currentConnection);
    }

    public boolean isWaitInitiated() {
        return this.rpcClientStatus.get() == RpcClientStatus.WAIT_INIT;
    }


    public boolean isRunning() {
        return this.rpcClientStatus.get() == RpcClientStatus.RUNNING;
    }


    public boolean isShutdown() {
        return this.rpcClientStatus.get() == RpcClientStatus.SHUTDOWN;
    }

    // 得到服务地址管理器
    public ServerListFactory getServerListFactory() {
        return serverListFactory;
    }

    /**
     * 封装服务器信息
     */
    public static class ServerInfo {
        protected String serverIp;

        protected int serverPort;

        public ServerInfo() {
        }

        public ServerInfo(String serverIp, int serverPort) {
            this.serverIp = serverIp;
            this.serverPort = serverPort;
        }

        public String getAddress() {
            return serverIp + Constants.COLON + serverPort;
        }


        public void setServerIp(String serverIp) {
            this.serverIp = serverIp;
        }

        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }


        public String getServerIp() {
            return serverIp;
        }


        public int getServerPort() {
            return serverPort;
        }

        @Override
        public String toString() {
            return "{serverIp = '" + serverIp + '\'' + ", server main port = " + serverPort + '}';
        }
    }

    public static class ConnectionEvent {

        public static final int DISCONNECTED = 0;

        public static final int CONNECTED = 1;
        // 事件类型
        int eventType;

        Connection connection;

        public ConnectionEvent(int eventType, Connection connection) {
            this.eventType = eventType;
            this.connection = connection;
        }

        public boolean isConnected() {
            return eventType == CONNECTED;
        }


        public boolean isDisConnected() {
            return eventType == DISCONNECTED;
        }
    }

    static class ReconnectContext {
        /**
         * 本次重连是不是因为请求发送失败而触发的
         */
        boolean onRequestFail;

        ServerInfo serverInfo;

        public ReconnectContext(ServerInfo serverInfo, boolean onRequestFail) {
            this.onRequestFail = onRequestFail;
            this.serverInfo = serverInfo;
        }
    }

    class ConnectResetRequestHandler implements ServerRequestHandler {

        @Override
        public Response requestReply(Request request, Connection connection) {
            if (!(request instanceof ConnectResetRequest)) {
                return null;
            }
            try {
                // 判断当前客户端是否再运行状态
                if (isRunning()) {
                    ConnectResetRequest connectResetRequest = (ConnectResetRequest) request;
                    // 从服务端发送的 request 的服务地址是否为空，不为空就连接该服务。为空就让客户端使用自己serverFactoryList中的
                    if (StringUtils.isNotBlank(connectResetRequest.getServerIp())) {
                        ServerInfo serverInfo = resolveServerInfo(connectResetRequest.getServerIp() + Constants.COLON + connectResetRequest.getServerPort());
                        switchServerAsync(serverInfo, false);
                    } else {
                        switchServerAsync();
                    }
                    // 扩展点
                    afterReset(connectResetRequest);
                }
            } catch (Exception e) {
                LoggerUtils.printIfErrorEnabled(logger, "[{}] Switch server error, {}", rpcClientConfig.name(), e);
            }
            return new ConnectResetResponse();
        }
    }

    // 该方法会在接收到服务端的重连请求时被调用，是一个可以由用户自己实现的扩展点
    protected void afterReset(ConnectResetRequest request) {

    }
}
