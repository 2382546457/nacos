package com.xiaohe.nacos.common.spi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NacosServiceLoader {

    private static final Map<Class<?>, Collection<Class<?>>> SERVICES = new ConcurrentHashMap<>();

    public static <T> Collection<T> load(final Class<T> service) {
        // 如果已经包含，说明已经通过SPI机制加载过Class，直接通过Class创建对象返回就好了。
        if (SERVICES.containsKey(service)) {
            return newServiceInstances(service);
        }
        Collection<T> result = new LinkedHashSet<>();
        for (T each : ServiceLoader.load(service)) {
            result.add(each);
            cacheServiceClass(service, each);
        }
        return result;
    }
    private static <T> void cacheServiceClass(final Class<T> service, final T instance) {
        if (!SERVICES.containsKey(service)) {
            SERVICES.put(service, new LinkedHashSet<>());
        }
        SERVICES.get(service).add(instance.getClass());
    }

    /**
     * 若 service 存在，将这 service 对应的所有实现类都创建对象并返回
     * 若 service 不存在，返回空集合
     * @param service
     * @return
     * @param <T>
     */
    public static <T> Collection<T> newServiceInstances(final Class<T> service) {
        return SERVICES.containsKey(service) ? newServiceInstancesFromCache(service) : Collections.<T>emptyList();
    }
    /**
     * 将一个 service 对应的所有实现类都创建对象并返回
     * @param service
     * @return
     * @param <T>
     */
    private static <T> Collection<T> newServiceInstancesFromCache(Class<T> service) {
        Collection<T> result = new LinkedHashSet<>();
        Collection<Class<?>> classes = SERVICES.get(service);
        for (Class<?> each : classes) {
            result.add((T) newServiceInstance(each));
        }
        return result;
    }

    private static Object newServiceInstance(final Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new ServiceLoaderException(clazz, e);
        }
    }

}
