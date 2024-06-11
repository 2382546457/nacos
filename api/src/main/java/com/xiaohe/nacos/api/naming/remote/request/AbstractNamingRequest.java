package com.xiaohe.nacos.api.naming.remote.request;

import com.xiaohe.nacos.api.remote.request.Request;

import static com.xiaohe.nacos.api.common.Constants.Naming.NAMING_MODULE;

public abstract class AbstractNamingRequest extends Request {

    private String namespace;

    private String serviceName;

    private String groupName;

    public AbstractNamingRequest() {
    }

    public AbstractNamingRequest(String namespace, String serviceName, String groupName) {
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.groupName = groupName;
    }

    @Override
    public String getModule() {
        return NAMING_MODULE;
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
