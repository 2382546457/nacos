package com.xiaohe.nacos.api.remote;

import com.xiaohe.nacos.api.remote.response.Response;

import java.util.concurrent.Executor;

public interface RequestCallBack<T extends Response> {

    /**
     * 获取执行这个回调的线程池
     * @return
     */
    Executor getExecutor();

    /**
     * 获取超时时间
     * @return
     */
    long getTimeout();

    /**
     * 接收到正常响应
     * @param response
     */
    void onResponse(T response);


    /**
     * 接收到异常响应
     * @param e
     */
    void onException(Throwable e);
}
