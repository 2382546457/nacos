package com.xiaohe.nacos.api.remote;

/**
 * 标识RPC消息来源于哪个方面，可能是集群、sdk、服务发现、配置中心..
 */
public class RemoteConstants {


    // 该常量代表消息的来源
    public static final String LABEL_SOURCE = "source";

    // 来源为sdk，就代表为客户端消息
    public static final String LABEL_SOURCE_SDK = "sdk";

    // 代表消息来源于集群
    public static final String LABEL_SOURCE_CLUSTER = "cluster";

    // 代表消息所属的模块
    public static final String LABEL_MODULE = "module";

    // 来自配置中心模块
    public static final String LABEL_MODULE_CONFIG = "config";

    // 来自服务发现模块
    public static final String LABEL_MODULE_NAMING = "naming";

    // 表示超出标识范围
    public static final String MONITOR_LABEL_NONE = "none";
}