package com.xiaohe.nacos.api.naming.remote.response;

import com.xiaohe.nacos.api.naming.pojo.ServiceInfo;
import com.xiaohe.nacos.api.remote.response.Response;

public class SubscribeServiceResponse extends Response {

    private ServiceInfo serviceInfo;

    public SubscribeServiceResponse() {
    }

    public SubscribeServiceResponse(int resultCode, String message, ServiceInfo serviceInfo) {
        super();
        setResultCode(resultCode);
        setMessage(message);
        this.serviceInfo = serviceInfo;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
}
