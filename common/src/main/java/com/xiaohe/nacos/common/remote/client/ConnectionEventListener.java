package com.xiaohe.nacos.common.remote.client;

/**
 * 连接事件监听器
 */
public interface ConnectionEventListener {

    void onConnected(Connection connection);

    void onDisConnect(Connection connection);
}
