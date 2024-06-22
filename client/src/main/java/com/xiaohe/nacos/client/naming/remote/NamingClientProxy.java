package com.xiaohe.nacos.client.naming.remote;

import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.naming.pojo.Instance;
import com.xiaohe.nacos.api.naming.pojo.ServiceInfo;
import com.xiaohe.nacos.common.lifecycle.Closeable;

public interface NamingClientProxy extends Closeable {

    /**
     * 向服务端注册一个服务实例
     * @param serviceName
     * @param groupName
     * @param instance
     * @throws NacosException
     */
    void registerService(String serviceName, String groupName, Instance instance) throws NacosException;

    /**
     * 订阅某个服务
     * @param serviceName
     * @param groupName
     * @param clusters
     * @return
     * @throws NacosException
     */
    ServiceInfo subscribe(String serviceName, String groupName, String clusters) throws NacosException;

    /** TODO
     * @param serviceName
     * @param groupName
     * @param clusters
     * @return
     * @throws NacosException
     */
    boolean isSubscribed(String serviceName, String groupName, String clusters) throws NacosException;

    /**
     * 当前实例是否健康
     * @return
     */
    boolean serverHealthy();


    ServiceInfo queryInstancesOfService(String serviceName, String groupName, String clusters, boolean healthyOnly) throws NacosException;

    /**
     * 取消注册
     * @param serviceName
     * @param groupName
     * @param instance
     * @throws NacosException
     */
    void deregisterService(String serviceName, String groupName, Instance instance) throws NacosException;

    /**
     * 取消订阅
     * @param serviceName
     * @param groupName
     * @param clusters
     * @throws NacosException
     */
    void unsubscribe(String serviceName, String groupName, String clusters) throws NacosException;

}
