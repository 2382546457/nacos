package com.xiaohe.nacos.api.remote;

import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.remote.request.Request;
import com.xiaohe.nacos.api.remote.response.Response;

public interface Requester {

    /**
     * send request.
     */
    Response request(Request request, long timeoutMills) throws NacosException;

    /**
     * send request.
     */
    RequestFuture requestFuture(Request request) throws NacosException;

    /**
     * send async request.
     */
    void asyncRequest(Request request, RequestCallBack requestCallBack) throws NacosException;

    /**
     * close connection.
     */
    void close();

}