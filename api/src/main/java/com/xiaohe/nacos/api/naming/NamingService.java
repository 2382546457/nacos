package com.xiaohe.nacos.api.naming;

import com.xiaohe.nacos.api.exception.NacosException;

/**
 * 命名服务接口
 */
public interface NamingService {

    /**
     * 注册实例
     * @param serviceName 服务名称
     * @param ip IP地址
     * @param port 端口号
     * @param clusterName 集群名称
     * @throws NacosException
     */
    void registerInstance(String serviceName, String ip, int port, String clusterName) throws NacosException;

    /**
     * 注册实例
     * @param serviceName 服务名称
     * @param groupName 组名
     * @param ip
     * @param port 端口
     * @param clusterName 集群名称
     */
    void registerInstance(String serviceName, String groupName, String ip, int port, String clusterName) throws NacosException;


}
