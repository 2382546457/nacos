package com.xiaohe.nacos.api.naming.remote.request;

import com.xiaohe.nacos.api.naming.pojo.Instance;

/**
 * 服务注册时使用的请求
 */
public class InstanceRequest extends AbstractNamingRequest {

    private String type;

    private Instance instance;

    public InstanceRequest() {
    }

    public InstanceRequest(String namespace, String serviceName, String groupName, String type, Instance instance) {
        super(namespace, serviceName, groupName);
        this.type = type;
        this.instance = instance;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Instance getInstance() {
        return instance;
    }
}
