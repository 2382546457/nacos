package com.xiaohe.nacos.api.naming.remote.response;

import com.xiaohe.nacos.api.naming.pojo.ServiceInfo;
import com.xiaohe.nacos.api.remote.response.Response;
import com.xiaohe.nacos.api.remote.response.ResponseCode;

public class QueryServiceResponse extends Response {

    private ServiceInfo serviceInfo;

    public QueryServiceResponse() {
    }

    private QueryServiceResponse(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    /**
     * Build Success response.
     *
     * @param serviceInfo service info
     * @return service query response
     */
    public static QueryServiceResponse buildSuccessResponse(ServiceInfo serviceInfo) {
        return new QueryServiceResponse(serviceInfo);
    }

    /**
     * Build fail response.
     *
     * @param message message
     * @return service query response
     */
    public static QueryServiceResponse buildFailResponse(String message) {
        QueryServiceResponse queryServiceResponse = new QueryServiceResponse();
        queryServiceResponse.setResultCode(ResponseCode.FAIL.getCode());
        queryServiceResponse.setMessage(message);
        return queryServiceResponse;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
}
