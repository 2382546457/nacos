package com.xiaohe.nacos.client.naming.remote.grpc;

import com.xiaohe.nacos.api.common.Constants;
import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.naming.CommonParams;
import com.xiaohe.nacos.api.naming.pojo.Instance;
import com.xiaohe.nacos.api.naming.pojo.ServiceInfo;
import com.xiaohe.nacos.api.naming.remote.NamingRemoteConstants;
import com.xiaohe.nacos.api.naming.remote.request.AbstractNamingRequest;
import com.xiaohe.nacos.api.naming.remote.request.InstanceRequest;
import com.xiaohe.nacos.api.naming.remote.request.ServiceQueryRequest;
import com.xiaohe.nacos.api.naming.remote.request.SubscribeServiceRequest;
import com.xiaohe.nacos.api.naming.remote.response.QueryServiceResponse;
import com.xiaohe.nacos.api.naming.remote.response.SubscribeServiceResponse;
import com.xiaohe.nacos.api.naming.utils.NamingUtils;
import com.xiaohe.nacos.api.remote.RemoteConstants;
import com.xiaohe.nacos.api.remote.response.Response;
import com.xiaohe.nacos.api.remote.response.ResponseCode;
import com.xiaohe.nacos.client.env.NacosClientProperties;
import com.xiaohe.nacos.client.naming.cache.ServiceInfoHolder;
import com.xiaohe.nacos.client.naming.event.ServerListChangedEvent;
import com.xiaohe.nacos.client.naming.remote.AbstractNamingClientProxy;
import com.xiaohe.nacos.client.naming.remote.grpc.redo.NamingGrpcRedoService;
import com.xiaohe.nacos.client.naming.remote.grpc.redo.data.BatchInstanceRedoData;
import com.xiaohe.nacos.client.naming.remote.grpc.redo.data.InstanceRedoData;
import com.xiaohe.nacos.client.utils.AppNameUtils;
import com.xiaohe.nacos.common.notify.NotifyCenter;
import com.xiaohe.nacos.common.notify.event.Event;
import com.xiaohe.nacos.common.remote.ConnectionType;
import com.xiaohe.nacos.common.remote.client.RpcClient;
import com.xiaohe.nacos.common.remote.client.RpcClientFactory;
import com.xiaohe.nacos.common.remote.client.RpcClientTlsConfig;
import com.xiaohe.nacos.common.remote.client.ServerListFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NamingGrpcClientProxy extends AbstractNamingClientProxy {

    /**
     * 服务实例的命名空间
     */
    private final String namespaceId;

    /**
     * RPC客户端的唯一标识
     */
    private final String uuid;

    private final Long requestTimeout;

    /**
     * rpc客户端
     */
    private final RpcClient rpcClient;

    /**
     * 重做服务，负责断线重连
     */
    private final NamingGrpcRedoService redoService;


    public NamingGrpcClientProxy(String namespaceId,
                                 ServerListFactory serverListFactory,
                                 NacosClientProperties properties,
                                 ServiceInfoHolder serviceInfoHolder) throws NacosException {
        this.namespaceId = namespaceId;
        this.uuid = UUID.randomUUID().toString();
        this.requestTimeout = Long.parseLong(properties.getProperty(CommonParams.NAMING_REQUEST_TIMEOUT, "-1"));
        //
        Map<String, String> labels = new HashMap<>();
        labels.put(RemoteConstants.LABEL_SOURCE, RemoteConstants.LABEL_SOURCE_SDK);
        labels.put(RemoteConstants.LABEL_MODULE, RemoteConstants.LABEL_MODULE_NAMING);
        labels.put(Constants.APPNAME, AppNameUtils.getAppName());
        this.rpcClient = RpcClientFactory.createClient(uuid, ConnectionType.GRPC, labels, RpcClientTlsConfig.properties(properties.asProperties()));
        this.redoService = new NamingGrpcRedoService(this, properties);
        // 启动rpc客户端
        start(serverListFactory, serviceInfoHolder);
    }

    private void start(ServerListFactory serverListFactory, ServiceInfoHolder serviceInfoHolder) throws NacosException {
        rpcClient.serverListFactory(serverListFactory);
        rpcClient.registerConnectionListener(redoService);
        rpcClient.registerServerRequestHandler(new NamingPushRequestHandler(serviceInfoHolder));
        rpcClient.start();
        NotifyCenter.registerSubscriber(this);
    }

    @Override
    public void registerService(String serviceName, String groupName, Instance instance) throws NacosException {
        if (instance.isEphemeral()) {
            registerServiceForEphemeral(serviceName, groupName, instance);
        } else {

        }
    }

    /**
     * 注册临时实例
     * @param serviceName
     * @param groupName
     * @param instance
     * @throws NacosException
     */
    private void registerServiceForEphemeral(String serviceName, String groupName, Instance instance) throws NacosException {
        redoService.cacheInstanceForRedo(serviceName, groupName, instance);
        doRegisterService(serviceName, groupName, instance);
    }

    public void doRegisterService(String serviceName, String groupName, Instance instance) throws NacosException {
        // 创建请求对象
        InstanceRequest request = new InstanceRequest(namespaceId, serviceName, groupName, NamingRemoteConstants.REGISTER_INSTANCE, instance);
        requestToServer(request, Response.class);
        redoService.instanceRegistered(serviceName, groupName);
    }

    /**
     * 发送 AbstractNamingRequest 请求给该客户端连接的服务端
     * @param request
     * @param responseClass
     * @return
     * @param <T>
     * @throws NacosException
     */
    private <T extends Response> T requestToServer(AbstractNamingRequest request, Class<T> responseClass) throws NacosException {
        Response response = null;
        try {
            // 设置请求头
            request.putAllHeader(getSecurityHeaders());
            response = requestTimeout < 0 ? rpcClient.request(request) : rpcClient.request(request, requestTimeout);
            if (ResponseCode.SUCCESS.getCode() != response.getResultCode()) {
                throw new NacosException(response.getErrorCode(), response.getMessage());
            }
            if (responseClass.isAssignableFrom(response.getClass())) {
                return (T) response;
            }
            throw new NacosException(NacosException.SERVER_ERROR, "Server return invalid response");
        } catch (NacosException e) {
            throw e;
        } catch (Exception e) {
            throw new NacosException(NacosException.SERVER_ERROR, "Request nacos server failed: ", e);
        }
    }

    @Override
    public ServiceInfo subscribe(String serviceName, String groupName, String clusters) throws NacosException {
        // 创建一个订阅重做对象
        redoService.cacheSubscriberForRedo(serviceName, groupName, clusters);
        return doSubscribe(serviceName, groupName, clusters);
    }

    public ServiceInfo doSubscribe(String serviceName, String groupName, String clusters) throws NacosException {
        SubscribeServiceRequest request = new SubscribeServiceRequest(namespaceId, groupName, serviceName, clusters, true);
        SubscribeServiceResponse response = requestToServer(request, SubscribeServiceResponse.class);
        // 将订阅操作的重做对象 registered 设置为 true
        redoService.subscriberRegistered(serviceName, groupName, clusters);
        return response.getServiceInfo();
    }

    public void doUnsubscribe(String serviceName, String groupName, String clusters) throws NacosException {
        SubscribeServiceRequest request = new SubscribeServiceRequest(namespaceId, groupName, serviceName, clusters,
                false);
        requestToServer(request, SubscribeServiceResponse.class);
        redoService.removeSubscriberForRedo(serviceName, groupName, clusters);
    }

    /**
     * 是否订阅了某个实例
     * @param serviceName
     * @param groupName
     * @param clusters
     * @return
     * @throws NacosException
     */
    @Override
    public boolean isSubscribed(String serviceName, String groupName, String clusters) throws NacosException {
        return redoService.isSubscriberRegistered(serviceName, groupName, clusters);
    }

    /**
     * 客户端是否健康
     * @return
     */
    @Override
    public boolean serverHealthy() {
        return rpcClient.isRunning();
    }

    /**
     * 查询某个服务下所有服务实例信息，并不会保存到客户端中，查了就返回
     * @param serviceName
     * @param groupName
     * @param clusters
     * @param healthyOnly
     * @return
     * @throws NacosException
     */
    @Override
    public ServiceInfo queryInstancesOfService(String serviceName, String groupName, String clusters, boolean healthyOnly) throws NacosException {
        ServiceQueryRequest request = new ServiceQueryRequest(namespaceId, serviceName, groupName);
        request.setCluster(clusters);
        // 是否只要健康的服务实例
        request.setHealthyOnly(healthyOnly);
        QueryServiceResponse response = requestToServer(request, QueryServiceResponse.class);
        return response.getServiceInfo();
    }

    @Override
    public void deregisterService(String serviceName, String groupName, Instance instance) throws NacosException {
        // 是否为临时实例
        if (instance.isEphemeral()) {
            deregisterServiceForEphemeral(serviceName, groupName, instance);
        } else {

        }
    }

    private void deregisterServiceForEphemeral(String serviceName, String groupName, Instance instance) throws NacosException {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        InstanceRedoData instanceRedoData = redoService.getRegisteredInstancesByKey(key);
        if (instanceRedoData instanceof BatchInstanceRedoData) {
            // 批量注销
        } else {
            redoService.instanceRegistered(serviceName, groupName);
            doDeregisterService(serviceName, groupName, instance);
        }
    }

    public void doDeregisterService(String serviceName, String groupName, Instance instance) throws NacosException {
        InstanceRequest request = new InstanceRequest(namespaceId, serviceName, groupName, NamingRemoteConstants.DE_REGISTER_INSTANCE, instance);
        requestToServer(request, Response.class);
        redoService.instanceDeregistered(serviceName, groupName);
    }

    @Override
    public void unsubscribe(String serviceName, String groupName, String clusters) throws NacosException {
        redoService.subscriberDeregister(serviceName, groupName, clusters);
        doUnsubscribe(serviceName, groupName, clusters);
    }

    @Override
    public void shutdown() throws NacosException {

    }

    @Override
    public void onEvent(ServerListChangedEvent event) {

    }

    @Override
    public Class<? extends Event> subscribeType() {
        return null;
    }

    public boolean isEnable() {
        return rpcClient.isRunning();
    }
}
