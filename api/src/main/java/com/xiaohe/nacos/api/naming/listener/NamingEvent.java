package com.xiaohe.nacos.api.naming.listener;


import com.xiaohe.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 命名服务的事件
 */
public class NamingEvent implements Event {

    private String serviceName;

    private String groupName;

    private String clusters;

    private List<Instance> instances;
    public NamingEvent(String serviceName, List<Instance> instances) {
        this.serviceName = serviceName;
        this.instances = instances;
    }

    public NamingEvent(String serviceName, String groupName, String clusters, List<Instance> instances) {
        this.serviceName = serviceName;
        this.groupName = groupName;
        this.clusters = clusters;
        this.instances = instances;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getClusters() {
        return clusters;
    }

    public void setClusters(String clusters) {
        this.clusters = clusters;
    }
}
