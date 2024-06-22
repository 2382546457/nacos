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


    private NotifySubscriberRequest(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public static NotifySubscriberRequest buildNotifySubscriberRequest(ServiceInfo serviceInfo) {
        return new NotifySubscriberRequest(serviceInfo);
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
