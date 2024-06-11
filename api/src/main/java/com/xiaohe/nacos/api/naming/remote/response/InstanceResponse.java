package com.xiaohe.nacos.api.naming.remote.response;

import com.xiaohe.nacos.api.remote.response.Response;

/**
 * 与 InstanceRequest 对应的响应
 */
public class InstanceResponse extends Response {

    private String type;

    public InstanceResponse() {
    }

    public InstanceResponse(String type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}