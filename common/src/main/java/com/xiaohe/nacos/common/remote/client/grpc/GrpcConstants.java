package com.xiaohe.nacos.common.remote.client.grpc;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GrpcConstants {

    public static final String NACOS_SERVER_GRPC_PORT_OFFSET_KEY = "nacos.server.grpc.port.offset";

    public static final String NACOS_CLIENT_GRPC = "nacos.remote.client.grpc";

    @GRpcConfigLabel
    public static final String GRPC_NAME = NACOS_CLIENT_GRPC + ".name";

    @GRpcConfigLabel
    public static final String GRPC_THREADPOOL_KEEPALIVETIME = NACOS_CLIENT_GRPC + ".pool.alive";

    @GRpcConfigLabel
    public static final String GRPC_THREADPOOL_CORE_SIZE = NACOS_CLIENT_GRPC + ".pool.core.size";

    @GRpcConfigLabel
    public static final String GRPC_RETRY_TIMES = NACOS_CLIENT_GRPC + ".retry.times";

    @GRpcConfigLabel
    public static final String GRPC_TIMEOUT_MILLS = NACOS_CLIENT_GRPC + ".timeout";

    @GRpcConfigLabel
    public static final String GRPC_CONNECT_KEEP_ALIVE_TIME = NACOS_CLIENT_GRPC + ".connect.keep.alive";

    @GRpcConfigLabel
    public static final String GRPC_THREADPOOL_MAX_SIZE = NACOS_CLIENT_GRPC + ".pool.max.size";

    @GRpcConfigLabel
    public static final String GRPC_SERVER_CHECK_TIMEOUT = NACOS_CLIENT_GRPC + ".server.check.timeout";

    @GRpcConfigLabel
    public static final String GRPC_QUEUESIZE = NACOS_CLIENT_GRPC + ".queue.size";

    @GRpcConfigLabel
    public static final String GRPC_HEALTHCHECK_RETRY_TIMES = NACOS_CLIENT_GRPC + ".health.retry";

    @GRpcConfigLabel
    public static final String GRPC_HEALTHCHECK_TIMEOUT = NACOS_CLIENT_GRPC + ".health.timeout";

    @GRpcConfigLabel
    public static final String GRPC_MAX_INBOUND_MESSAGE_SIZE = NACOS_CLIENT_GRPC + ".maxinbound.message.size";

    @GRpcConfigLabel
    public static final String GRPC_CHANNEL_KEEP_ALIVE_TIME = NACOS_CLIENT_GRPC + ".channel.keep.alive";

    @GRpcConfigLabel
    public static final String GRPC_CHANNEL_KEEP_ALIVE_TIMEOUT = NACOS_CLIENT_GRPC + ".channel.keep.alive.timeout";

    @GRpcConfigLabel
    public static final String GRPC_CHANNEL_CAPABILITY_NEGOTIATION_TIMEOUT = NACOS_CLIENT_GRPC + ".channel.capability.negotiation.timeout";

    private static final Set<String> CONFIG_NAMES = new HashSet<>();

    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface GRpcConfigLabel {

    }

    static {
        Class clazz = GrpcConstants.class;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (declaredField.getType().equals(String.class) && null != declaredField.getAnnotation(
                    GRpcConfigLabel.class)) {
                try {
                    CONFIG_NAMES.add((String) declaredField.get(null));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    public static Set<String> getRpcParams() {
        return Collections.unmodifiableSet(CONFIG_NAMES);
    }
}
