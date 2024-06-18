package com.xiaohe.nacos.common.remote.client;

import java.util.Map;

public interface RpcClientConfig {

    String name();

    int retryTimes();

    long timeOutMills();

    long connectionKeepAlive();

    int healthCheckRetryTimes();

    long healthCheckTimeOut();

    Map<String, String> labels();

}
