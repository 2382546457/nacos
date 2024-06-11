package com.xiaohe.nacos.api.naming.remote.request;

import com.xiaohe.nacos.api.naming.pojo.ServiceInfo;
import com.xiaohe.nacos.api.remote.request.ServerRequest;

/**
 * 通知订阅者的请求
 */
public class NotifySubscriberRequest extends ServerRequest {

    private String namespace;

    private String serviceName;

    private String groupName;

    private ServiceInfo serviceInfo;

    @Override
    public String getModule() {
        return "";
    }
}
