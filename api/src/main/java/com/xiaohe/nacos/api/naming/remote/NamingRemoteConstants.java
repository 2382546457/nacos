package com.xiaohe.nacos.api.naming.remote;

/**
 * 发送到服务端的请求要做的事情
 */
public class NamingRemoteConstants {

    // 这个就是注册服务实例的操作
    public static final String REGISTER_INSTANCE = "registerInstance";

    public static final String BATCH_REGISTER_INSTANCE = "batchRegisterInstance";

    public static final String DE_REGISTER_INSTANCE = "deregisterInstance";

    public static final String QUERY_SERVICE = "queryService";

    public static final String SUBSCRIBE_SERVICE = "subscribeService";

    public static final String NOTIFY_SUBSCRIBER = "notifySubscriber";

    public static final String LIST_SERVICE = "listService";

    public static final String FORWARD_INSTANCE = "forwardInstance";

    public static final String FORWARD_HEART_BEAT = "forwardHeartBeat";
}