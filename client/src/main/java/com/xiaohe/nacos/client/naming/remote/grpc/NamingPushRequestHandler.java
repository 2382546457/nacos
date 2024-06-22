package com.xiaohe.nacos.client.naming.remote.grpc;

import com.xiaohe.nacos.api.naming.remote.request.NotifySubscriberRequest;
import com.xiaohe.nacos.api.naming.remote.response.NotifySubscriberResponse;
import com.xiaohe.nacos.api.remote.request.Request;
import com.xiaohe.nacos.api.remote.response.Response;
import com.xiaohe.nacos.client.naming.cache.ServiceInfoHolder;
import com.xiaohe.nacos.common.remote.client.Connection;
import com.xiaohe.nacos.common.remote.client.ServerRequestHandler;

/**
 * 服务端接收到 A服务 的客户端的服务变更后，会通知使用到 A服务 的其他客户端，NamingPushRequestHandler用于处理这个通知请求
 */
public class NamingPushRequestHandler implements ServerRequestHandler {

    private final ServiceInfoHolder serviceInfoHolder;

    public NamingPushRequestHandler(ServiceInfoHolder serviceInfoHolder) {
        this.serviceInfoHolder = serviceInfoHolder;
    }

    @Override
    public Response requestReply(Request request, Connection connection) {
        if (request instanceof NotifySubscriberRequest) {
            NotifySubscriberRequest notifyRequest = (NotifySubscriberRequest) request;
            serviceInfoHolder.processServiceInfo(notifyRequest.getServiceInfo());
            return new NotifySubscriberResponse();
        }
        return null;
    }
}
