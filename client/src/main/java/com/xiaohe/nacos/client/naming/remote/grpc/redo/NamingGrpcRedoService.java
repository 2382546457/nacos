package com.xiaohe.nacos.client.naming.remote.grpc.redo;

import com.xiaohe.nacos.api.PropertyKeyConst;
import com.xiaohe.nacos.api.common.Constants;
import com.xiaohe.nacos.api.naming.pojo.Instance;
import com.xiaohe.nacos.api.naming.pojo.ServiceInfo;
import com.xiaohe.nacos.api.naming.utils.NamingUtils;
import com.xiaohe.nacos.client.env.NacosClientProperties;
import com.xiaohe.nacos.client.naming.remote.grpc.NamingGrpcClientProxy;
import com.xiaohe.nacos.client.naming.remote.grpc.redo.data.InstanceRedoData;
import com.xiaohe.nacos.client.naming.remote.grpc.redo.data.SubscriberRedoData;
import com.xiaohe.nacos.common.executor.NameThreadFactory;
import com.xiaohe.nacos.common.remote.client.Connection;
import com.xiaohe.nacos.common.remote.client.ConnectionEventListener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class NamingGrpcRedoService implements ConnectionEventListener {

    private static final String REDO_THREAD_NAME = "com.alibaba.nacos.client.naming.grpc.redo";

    /**
     * 执行重做操作的线程的数量
     */
    private int redoThreadCount;

    private long redoDelayTime;

    /**
     * 实例注册重做
     */
    private final ConcurrentMap<String, InstanceRedoData> registeredInstances = new ConcurrentHashMap<>();

    /**
     * 监听服务重做
     */
    private final ConcurrentMap<String, SubscriberRedoData> subscribes = new ConcurrentHashMap<>();

    /**
     * 执行重做操作的线程池
     */
    private final ScheduledExecutorService redoExecutor;

    /**
     * 表示当前客户端和服务端是否成功建立了连接
     */
    private volatile boolean connected = false;

    public NamingGrpcRedoService(NamingGrpcClientProxy clientProxy, NacosClientProperties properties) {
        setProperties(properties);
        this.redoExecutor = new ScheduledThreadPoolExecutor(redoThreadCount, new NameThreadFactory(REDO_THREAD_NAME));

        this.redoExecutor.scheduleWithFixedDelay(
                new RedoScheduledTask(clientProxy, this),
                redoDelayTime,
                redoDelayTime,
                TimeUnit.MILLISECONDS
        );
    }
    private void setProperties(NacosClientProperties properties) {
        redoDelayTime = properties.getLong(PropertyKeyConst.REDO_DELAY_TIME, Constants.DEFAULT_REDO_DELAY_TIME);
        redoThreadCount = properties.getInteger(PropertyKeyConst.REDO_DELAY_THREAD_COUNT, Constants.DEFAULT_REDO_THREAD_COUNT);
    }

    /**
     * 实例在注册之前放到 registeredInstances 中
     * @param serviceName
     * @param groupName
     * @param instance
     */
    public void cacheInstanceForRedo(String serviceName, String groupName, Instance instance) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        InstanceRedoData redoData = InstanceRedoData.build(serviceName, groupName, instance);
        synchronized (registeredInstances) {
            registeredInstances.put(key, redoData);
        }
    }
    public void removeInstanceForRedo(String serviceName, String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        synchronized (registeredInstances) {
            InstanceRedoData instanceRedoData = registeredInstances.get(key);
            if (instanceRedoData != null && !instanceRedoData.isExpectedRegistered()) {
                registeredInstances.remove(key);
            }
        }
    }

    /**
     * 实例注册成功后设置为成功
     * @param serviceName
     * @param groupName
     */
    public void instanceRegistered(String serviceName, String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        synchronized (registeredInstances) {
            InstanceRedoData instanceRedoData = registeredInstances.get(key);
            if (instanceRedoData != null) {
                instanceRedoData.registered();
            }
        }
    }

    /**
     * 服务实例取消注册
     * @param serviceName
     * @param groupName
     */
    public void instanceDeregister(String serviceName, String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        synchronized (registeredInstances) {
            InstanceRedoData instanceRedoData = registeredInstances.get(key);
            if (instanceRedoData != null) {
                instanceRedoData.setUnregistering(true);
                instanceRedoData.setExpectedRegistered(false);
            }
        }
    }

    /**
     * 注销完成
     * @param serviceName
     * @param groupName
     */
    public void instanceDeregistered(String serviceName, String groupName) {
        String key = NamingUtils.getGroupedName(serviceName, groupName);
        synchronized (registeredInstances) {
            InstanceRedoData redoData = registeredInstances.get(key);
            if (null != redoData) {
                redoData.unregistered();
            }
        }
    }
    public Set<InstanceRedoData> findInstanceRedoData() {
        Set<InstanceRedoData> result = new HashSet<>();
        synchronized (registeredInstances) {
            for (InstanceRedoData each : registeredInstances.values()) {
                if (each.isNeedRedo()) {
                    result.add(each);
                }
            }
        }
        return result;
    }
    public void cacheSubscriberForRedo(String serviceName, String groupName, String cluster) {
        String key = ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        SubscriberRedoData redoData = SubscriberRedoData.build(serviceName, groupName, cluster);
        synchronized (subscribes) {
            subscribes.put(key, redoData);
        }
    }

    public void subscriberRegistered(String serviceName, String groupName, String cluster) {
        String key = ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        synchronized (subscribes) {
            SubscriberRedoData redoData = subscribes.get(key);
            if (null != redoData) {
                redoData.setRegistered(true);
            }
        }
    }

    public void subscriberDeregister(String serviceName, String groupName, String cluster) {
        String key = ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        synchronized (subscribes) {
            SubscriberRedoData redoData = subscribes.get(key);
            if (null != redoData) {
                redoData.setUnregistering(true);
                redoData.setExpectedRegistered(false);
            }
        }
    }


    public boolean isSubscriberRegistered(String serviceName, String groupName, String cluster) {
        String key = ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        synchronized (subscribes) {
            SubscriberRedoData redoData = subscribes.get(key);
            return null != redoData && redoData.isRegistered();
        }
    }

    public void removeSubscriberForRedo(String serviceName, String groupName, String cluster) {
        String key = ServiceInfo.getKey(NamingUtils.getGroupedName(serviceName, groupName), cluster);
        synchronized (subscribes) {
            SubscriberRedoData redoData = subscribes.get(key);
            if (null != redoData && !redoData.isExpectedRegistered()) {
                subscribes.remove(key);
            }
        }
    }


    public Set<SubscriberRedoData> findSubscriberRedoData() {
        Set<SubscriberRedoData> result = new HashSet<>();
        synchronized (subscribes) {
            for (SubscriberRedoData each : subscribes.values()) {
                if (each.isNeedRedo()) {
                    result.add(each);
                }
            }
        }
        return result;
    }


    public InstanceRedoData getRegisteredInstancesByKey(String combinedServiceName) {
        return registeredInstances.get(combinedServiceName);
    }


    public void shutdown() {
        registeredInstances.clear();
        subscribes.clear();
        redoExecutor.shutdownNow();
    }



    @Override
    public void onConnected(Connection connection) {
        connected = true;
    }

    /**
     * 断开连接时将重做事件设置为false
     * @param connection
     */
    @Override
    public void onDisConnect(Connection connection) {
        connected = false;
        synchronized (registeredInstances) {
            registeredInstances.values().forEach(instanceRedoData -> instanceRedoData.setRegistered(false));
        }
        synchronized (subscribes) {
            subscribes.values().forEach(subscriberRedoData -> subscriberRedoData.setRegistered(false));
        }
    }
    public boolean isConnected() {
        return connected;
    }
    public ConcurrentMap<String, InstanceRedoData> getRegisteredInstances() {
        return registeredInstances;
    }

}

