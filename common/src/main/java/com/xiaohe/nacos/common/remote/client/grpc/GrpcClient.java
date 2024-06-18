package com.xiaohe.nacos.common.remote.client.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.xiaohe.nacos.api.ability.constant.AbilityMode;
import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.grpc.auto.BiRequestStreamGrpc;
import com.xiaohe.nacos.api.grpc.auto.Payload;
import com.xiaohe.nacos.api.grpc.auto.RequestGrpc;
import com.xiaohe.nacos.api.remote.request.ConnectionSetupRequest;
import com.xiaohe.nacos.api.remote.request.Request;
import com.xiaohe.nacos.api.remote.request.ServerCheckRequest;
import com.xiaohe.nacos.api.remote.request.SetupAckRequest;
import com.xiaohe.nacos.api.remote.response.ErrorResponse;
import com.xiaohe.nacos.api.remote.response.Response;
import com.xiaohe.nacos.api.remote.response.ServerCheckResponse;
import com.xiaohe.nacos.api.remote.response.SetupAckResponse;
import com.xiaohe.nacos.common.remote.ConnectionType;
import com.xiaohe.nacos.common.remote.client.*;
import com.xiaohe.nacos.common.utils.LoggerUtils;
import com.xiaohe.nacos.common.utils.ThreadFactoryBuilder;
import com.xiaohe.nacos.common.utils.VersionUtils;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class GrpcClient extends RpcClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcClient.class);

    private final GrpcClientConfig clientConfig;

    private ThreadPoolExecutor grpcExecutor;

    /**
     * 客户端收到响应之前，让当前线程阻塞
     */
    private final RecAbilityContext recAbilityContext = new RecAbilityContext(null);


    private SetupRequestHandler setupRequestHandler;


    protected abstract AbilityMode abilityMode();


    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.GRPC;
    }


    public GrpcClient(String name) {
        this(DefaultGrpcClientConfig.newBuilder().setName(name).build());
    }

    public GrpcClient(Properties properties) {
        this(DefaultGrpcClientConfig.newBuilder().fromProperties(properties).build());
    }

    public GrpcClient(GrpcClientConfig clientConfig) {
        super(clientConfig);
        this.clientConfig = clientConfig;
        initSetupHandler();
    }
    public GrpcClient(GrpcClientConfig clientConfig, ServerListFactory serverListFactory) {
        super(clientConfig, serverListFactory);
        this.clientConfig = clientConfig;
        initSetupHandler();
    }
    public GrpcClient(String name, Integer threadPoolCoreSize, Integer threadPoolMaxSize, Map<String, String> labels) {
        this(DefaultGrpcClientConfig.newBuilder().setName(name).setThreadPoolCoreSize(threadPoolCoreSize)
                .setThreadPoolMaxSize(threadPoolMaxSize).setLabels(labels).build());
    }
    public GrpcClient(String name, Integer threadPoolCoreSize, Integer threadPoolMaxSize, Map<String, String> labels,
                      RpcClientTlsConfig tlsConfig) {
        this(DefaultGrpcClientConfig.newBuilder().setName(name).setThreadPoolCoreSize(threadPoolCoreSize)
                .setTlsConfig(tlsConfig).setThreadPoolMaxSize(threadPoolMaxSize).setLabels(labels).build());
    }

    private void initSetupHandler() {
        setupRequestHandler = new SetupRequestHandler(this.recAbilityContext);
    }

    protected ThreadPoolExecutor createGrpcExecutor(String serverIp) {
        serverIp = serverIp.replaceAll("%", "-");
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(clientConfig.threadPoolCoreSize(),
                clientConfig.threadPoolMaxSize(), clientConfig.threadPoolKeepAlive(), TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(clientConfig.threadPoolQueueSize()),
                new ThreadFactoryBuilder().daemon(true).nameFormat("nacos-grpc-client-executor-" + serverIp + "-%d")
                        .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
    }

    /**
     * 创建用于和服务器连接的channel
     * @param serverIp
     * @param serverPort
     * @return
     */
    private ManagedChannel createNewManagedChannel(String serverIp, int serverPort) {
        ManagedChannelBuilder<?> managedChannelBuilder = buildChannel(serverIp, serverPort, buildSslContext())
                .executor(grpcExecutor)
                .compressorRegistry(CompressorRegistry.getDefaultInstance())
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .maxInboundMessageSize(clientConfig.maxInboundMessageSize())
                .keepAliveTime(clientConfig.channelKeepAlive(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(clientConfig.channelKeepAliveTimeout(), TimeUnit.MILLISECONDS);
        return managedChannelBuilder.build();
    }

    /**
     * 当接收到来自服务端的请求后，调用 StreamObserver.onNext() 方法处理
     * @param streamStub
     * @param grpcConnection
     * @return
     */
    private StreamObserver<Payload> bindRequestStream(final BiRequestStreamGrpc.BiRequestStreamStub streamStub,
                                                      final GrpcConnection grpcConnection) {
        StreamObserver<Payload> payloadStreamObserver = streamStub.requestBiStream(new StreamObserver<Payload>() {
            @Override
            public void onNext(Payload payload) {
                LoggerUtils.printIfDebugEnabled(LOGGER, "[{}]Stream server request receive, original info: {}", grpcConnection.getConnectionId(), payload.toString());
                try {
                    Object parseBody = GrpcUtils.parse(payload);
                    final Request request = (Request) parseBody;
                    if (request != null) {
                        try {
                            if (request instanceof SetupAckRequest) {
                                setupRequestHandler.requestReply(request, null);
                                return;
                            }
                            Response response = handleServerRequest(request);
                            if (response != null) {
                                response.setRequestId(request.getRequestId());
                                sendResponse(response);
                            } else {
                                LOGGER.warn("[{}]Fail to process server request, ackId->{}", grpcConnection.getConnectionId(), request.getRequestId());
                            }
                        } catch (Exception e) {
                            Response errResponse = ErrorResponse.build(NacosException.CLIENT_ERROR, "Handle server request error");
                            errResponse.setRequestId(request.getRequestId());
                            sendResponse(errResponse);
                        }
                    }
                } catch (Exception e) {
                    LoggerUtils.printIfErrorEnabled(LOGGER, "[{}]Error to process server push response: {}", grpcConnection.getConnectionId(), payload.getBody().getValue().toStringUtf8());
                    // remove and notify
                    recAbilityContext.release(null);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                boolean isRunning = isRunning();
                boolean isAbandon = grpcConnection.isAbandon();
                if (isRunning && !isAbandon) {
                    LoggerUtils.printIfErrorEnabled(LOGGER, "[{}]Request stream error, switch server,error={}", grpcConnection.getConnectionId(), throwable);
                    if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
                        switchServerAsync();
                    }
                } else {
                    LoggerUtils.printIfWarnEnabled(LOGGER, "[{}]Ignore error event,isRunning:{},isAbandon={}", grpcConnection.getConnectionId(), isRunning, isAbandon);
                }
            }

            @Override
            public void onCompleted() {
                boolean isRunning = isRunning();
                boolean isAbandon = grpcConnection.isAbandon();
                if (isRunning && !isAbandon) {
                    LoggerUtils.printIfErrorEnabled(LOGGER, "[{}]Request stream onCompleted, switch server", grpcConnection.getConnectionId());
                    if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
                        switchServerAsync();
                    }
                } else {
                    LoggerUtils.printIfInfoEnabled(LOGGER, "[{}]Ignore complete event,isRunning:{},isAbandon={}", grpcConnection.getConnectionId(), isRunning, isAbandon);
                }
            }
        });
        return payloadStreamObserver;
    }

    @Override
    public Connection connectToServer(ServerInfo serverInfo) throws Exception {
        String connectionId = "";
        try {
            if (grpcExecutor == null) {
                this.grpcExecutor = createGrpcExecutor(serverInfo.getServerIp());
            }
            int port = serverInfo.getServerPort();
            // 创建channel
            ManagedChannel managedChannel = createNewManagedChannel(serverInfo.getServerIp(), port);
            // RequestFutureStub 向服务端发送消息的工具
            RequestGrpc.RequestFutureStub newChannelStubTemp = createNewChannelStub(managedChannel);
            // 检查连接是否成功
            Response response = serverCheck(serverInfo.getServerIp(), port, newChannelStubTemp);
            if (!(response instanceof ServerCheckResponse)) {
                shuntDownChannel(managedChannel);
                return null;
            }
            ServerCheckResponse serverCheckResponse = (ServerCheckResponse) response;
            connectionId = serverCheckResponse.getConnectionId();
            // 创建一个双向的流式存根
            BiRequestStreamGrpc.BiRequestStreamStub biRequestStreamStub = BiRequestStreamGrpc.newStub(newChannelStubTemp.getChannel());
            // 构建自己的GrpcConnection
            GrpcConnection grpcConnection = new GrpcConnection(serverInfo, grpcExecutor);
            grpcConnection.setConnectionId(connectionId);

            if (serverCheckResponse.isSupportAbilityNegotiation()) {
                this.recAbilityContext.reset(grpcConnection);
                grpcConnection.setAbilityTable(null);
            }
            // 将 GrpcConnection 交给双向流存根使用
            StreamObserver<Payload> payloadStreamObserver = bindRequestStream(biRequestStreamStub, grpcConnection);
            grpcConnection.setPayloadStreamObserver(payloadStreamObserver);
            grpcConnection.setGrpcFutureServiceStub(newChannelStubTemp);
            grpcConnection.setChannel(managedChannel);
            // 将客户端的信息封装在这里面，给服务端发过去
            ConnectionSetupRequest connectionSetupRequest = new ConnectionSetupRequest();
            connectionSetupRequest.setClientVersion(VersionUtils.getFullClientVersion());
            connectionSetupRequest.setLabels(super.getLabels());
            connectionSetupRequest.setTenant(super.getTenant());
            grpcConnection.sendRequest(connectionSetupRequest);
            if (recAbilityContext.isNeedToSync()) {
                recAbilityContext.await(this.clientConfig.capabilityNegotiationTimeout(), TimeUnit.MILLISECONDS);
                if (!recAbilityContext.check(grpcConnection)) {
                    return null;
                }
            } else {
                Thread.sleep(100L);
            }
            return grpcConnection;
        } catch (Exception e) {

        }
        return null;
    }

    private void sendResponse(Response response) {
        try {
            ((GrpcConnection) this.currentConnection).sendResponse(response);
        } catch (Exception e) {
            LOGGER.error("[{}]Error to send ack response, ackId->{}", this.currentConnection.getConnectionId(), response.getRequestId());
        }
    }


    private void shuntDownChannel(ManagedChannel managedChannel) {
        if (managedChannel != null && !managedChannel.isShutdown()) {
            managedChannel.shutdownNow();
        }
    }

    /**
     * 检查客户端与服务器是否成功建立连接
     * @param ip
     * @param port
     * @param requestBlockingStub
     * @return
     */
    private Response serverCheck(String ip, int port, RequestGrpc.RequestFutureStub requestBlockingStub) {
        try {
            ServerCheckRequest serverCheckRequest = new ServerCheckRequest();
            // 把请求转换为protobuf定义的请求
            Payload grpcRequest = GrpcUtils.convert(serverCheckRequest);
            // 发送给服务器并接受响应
            ListenableFuture<Payload> responseFuture = requestBlockingStub.request(grpcRequest);
            Payload response = responseFuture.get(clientConfig.serverCheckTimeOut(), TimeUnit.MILLISECONDS);
            return (Response) GrpcUtils.parse(response);
        } catch (Exception e) {
            LoggerUtils.printIfErrorEnabled(LOGGER, "Server check fail, please check server {} ,port {} is available , error ={}", ip, port, e);
            if (this.clientConfig != null && this.clientConfig.tlsConfig() != null && this.clientConfig.tlsConfig().getEnableTls()) {
                LoggerUtils.printIfErrorEnabled(LOGGER, "current client is require tls encrypted ,server must support tls ,please check");
            }
            return null;
        }
    }
    @Override
    public void shutdown() throws NacosException {
        super.shutdown();
        if (grpcExecutor != null) {
            grpcExecutor.shutdown();
        }
    }
    protected RequestGrpc.RequestFutureStub createNewChannelStub(ManagedChannel managedChannelTemp) {
        return RequestGrpc.newFutureStub(managedChannelTemp);
    }

    private ManagedChannelBuilder buildChannel(String serverIp, int port, Optional<SslContext> sslContext) {
        if(sslContext.isPresent()) {
            return NettyChannelBuilder.forAddress(serverIp, port).negotiationType(NegotiationType.TLS).sslContext(sslContext.get());
        } else {
            return ManagedChannelBuilder.forAddress(serverIp, port).usePlaintext();
        }
    }
    private Optional<SslContext> buildSslContext() {
        RpcClientTlsConfig tlsConfig = clientConfig.tlsConfig();
        if (!tlsConfig.getEnableTls()) {
            return Optional.empty();
        }
        return null;
    }
    private void shutDownChannel(ManagedChannel managedChannel) {
        if (managedChannel != null && !managedChannel.isShutdown()) {
            managedChannel.shutdownNow();
        }
    }



    static class RecAbilityContext {


        private volatile Connection connection;


        private volatile CountDownLatch blocker;

        private volatile boolean needToSync = false;

        public RecAbilityContext(Connection connection) {
            this.connection = connection;
            this.blocker = new CountDownLatch(1);
        }


        public boolean isNeedToSync() {
            return this.needToSync;
        }


        public void reset(Connection connection) {
            this.connection = connection;
            this.blocker = new CountDownLatch(1);
            this.needToSync = true;
        }


        public void release(Map<String, Boolean> abilities) {
            if (this.connection != null) {
                this.connection.setAbilityTable(abilities);
                // avoid repeat setting
                this.connection = null;
            }
            if (this.blocker != null) {
                blocker.countDown();
            }
            this.needToSync = false;
        }


        public void await(long timeout, TimeUnit unit) throws InterruptedException {
            if (this.blocker != null) {
                this.blocker.await(timeout, unit);
            }
            this.needToSync = false;
        }


        public boolean check(Connection connection) {
            if (!connection.isAbilitiesSet()) {
                LOGGER.error(
                        "Client don't receive server abilities table even empty table but server supports ability negotiation."
                                + " You can check if it is need to adjust the timeout of ability negotiation by property: {}"
                                + " if always fail to connect.",
                        GrpcConstants.GRPC_CHANNEL_CAPABILITY_NEGOTIATION_TIMEOUT);
                connection.setAbandon(true);
                connection.close();
                return false;
            }
            return true;
        }
    }

    class SetupRequestHandler implements ServerRequestHandler {

        private final RecAbilityContext abilityContext;

        public SetupRequestHandler(RecAbilityContext abilityContext) {
            this.abilityContext = abilityContext;
        }

        @Override
        public Response requestReply(Request request, Connection connection) {
            if (request instanceof SetupAckRequest) {
                SetupAckRequest setupAckRequest = (SetupAckRequest) request;
                recAbilityContext.release(Optional.ofNullable(setupAckRequest.getAbilityTable()).orElse(new HashMap<>(0)));
                return new SetupAckResponse();
            }
            return null;
        }
    }
}
