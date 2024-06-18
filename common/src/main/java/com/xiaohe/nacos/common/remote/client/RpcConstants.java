package com.xiaohe.nacos.common.remote.client;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RpcConstants {

    public static final String NACOS_CLIENT_RPC = "nacos.remote.client.rpc";

    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_ENABLE = NACOS_CLIENT_RPC + ".tls.enable";

    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_PROVIDER = NACOS_CLIENT_RPC + ".tls.provider";

    @RpcConfigLabel
    public static final String RPC_CLIENT_MUTUAL_AUTH = NACOS_CLIENT_RPC + ".tls.mutualAuth";

    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_PROTOCOLS = NACOS_CLIENT_RPC + ".tls.protocols";

    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_CIPHERS = NACOS_CLIENT_RPC + ".tls.ciphers";

    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_CERT_CHAIN_PATH = NACOS_CLIENT_RPC + ".tls.certChainFile";

    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_CERT_KEY = NACOS_CLIENT_RPC + ".tls.certPrivateKey";

    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_TRUST_PWD = NACOS_CLIENT_RPC + ".tls.certPrivateKeyPassword";

    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_TRUST_COLLECTION_CHAIN_PATH =
            NACOS_CLIENT_RPC + ".tls.trustCollectionChainPath";

    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_TRUST_ALL = NACOS_CLIENT_RPC + ".tls.trustAll";

    private static final Set<String> CONFIG_NAMES = new HashSet<>();

    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface RpcConfigLabel {

    }

    static {
        Class clazz = RpcConstants.class;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (declaredField.getType().equals(String.class) && null != declaredField
                    .getAnnotation(RpcConfigLabel.class)) {
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
