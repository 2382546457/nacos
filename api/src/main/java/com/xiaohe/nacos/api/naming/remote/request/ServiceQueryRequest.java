package com.xiaohe.nacos.api.naming.remote.request;

/**
 * 查询服务的请求
 */
public class ServiceQueryRequest extends AbstractNamingRequest {

    private String cluster;

    private boolean healthyOnly;

    private int udpPort;

    public ServiceQueryRequest() {
    }

    public ServiceQueryRequest(String namespace, String serviceName, String groupName) {
        super(namespace, serviceName, groupName);
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public boolean isHealthyOnly() {
        return healthyOnly;
    }

    public void setHealthyOnly(boolean healthyOnly) {
        this.healthyOnly = healthyOnly;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }
}
