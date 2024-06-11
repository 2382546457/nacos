package com.xiaohe.nacos.api.remote;

import com.xiaohe.nacos.api.remote.response.Response;

public interface RequestFuture {


    boolean isDone();


    Response get() throws Exception;


    Response get(long timeout) throws Exception;

}