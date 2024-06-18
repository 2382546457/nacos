package com.xiaohe.nacos.common.remote;

import com.xiaohe.nacos.api.remote.Payload;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 请求和相应的注册器
 * 将请求与响应按照 name-class 形式注册到Map中
 */
public class PayloadRegistry {

    /**
     * key : 全限定类名，请求/响应
     * value : 请求/响应的 Class
     */
    private static final Map<String, Class<?>> REGISTRY_REQUEST = new HashMap<>();

    static boolean initialized = false;

    public static void init() {
        scan();
    }

    private static synchronized void scan() {
        if (initialized) {
            return;
        }
        ServiceLoader<Payload> payloads = ServiceLoader.load(Payload.class);
        for (Payload payload : payloads) {
            register(payload.getClass().getSimpleName(), payload.getClass());
        }
    }
    static void register(String type, Class<?> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return;
        }
        if (REGISTRY_REQUEST.containsKey(type)) {
            throw new RuntimeException(String.format("Fail to register, type:%s ,clazz:%s ", type, clazz.getName()));
        }
        REGISTRY_REQUEST.put(type, clazz);
    }

    public static Class<?> getClassByType(String type) {
        return REGISTRY_REQUEST.get(type);
    }
}
