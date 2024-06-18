package com.xiaohe.nacos.common.remote.client;

public enum RpcClientStatus {

    // 等待初始化
    WAIT_INIT(0, "Wait to init server list factory..."),

    // 初始化完毕
    INITIALIZED(1, "Server list factory is ready, wait to starting..."),

    // 正在启动
    STARTING(2, "Client already staring, wait to connect with server..."),

    // 不健康的状态
    UNHEALTHY(3, "Client unhealthy, may closed by server, in reconnecting"),

    // 工作中
    RUNNING(4, "Client is running"),

    // 停止工作
    SHUTDOWN(5, "Client is shutdown");

    int status;

    String desc;

    RpcClientStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}