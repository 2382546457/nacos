package com.xiaohe.nacos.api.naming;

public class PreservedMetadataKeys {



    // 服务实例心跳超时的key，用于 Instance 的 Metadata
    public static final String HEART_BEAT_TIMEOUT = "preserved.heart.beat.timeout";

    // 服务实例IP删除超时的键，用来设定服务实例IP删除的超时时间, 用于 Instance 的 Metadata
    public static final String IP_DELETE_TIMEOUT = "preserved.ip.delete.timeout";

    // 服务实例心跳间隔的键，用来设定服务实例发送心跳的间隔时间, 用于 Instance 的 Metadata
    public static final String HEART_BEAT_INTERVAL = "preserved.heart.beat.interval";


}