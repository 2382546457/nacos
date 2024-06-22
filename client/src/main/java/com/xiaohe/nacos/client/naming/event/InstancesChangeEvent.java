package com.xiaohe.nacos.client.naming.event;

import com.xiaohe.nacos.api.naming.pojo.Instance;
import com.xiaohe.nacos.common.notify.event.Event;

import java.util.List;

public class InstancesChangeEvent extends Event {
    private static final long serialVersionUID = -8823087028212249603L;

    private final String eventScope;

    private final String serviceName;

    private final String groupName;

    private final String clusters;

    private final List<Instance> hosts;

    public InstancesChangeEvent(String eventScope, String serviceName, String groupName, String clusters, List<Instance> hosts) {
        this.eventScope = eventScope;
        this.serviceName = serviceName;
        this.groupName = groupName;
        this.clusters = clusters;
        this.hosts = hosts;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getClusters() {
        return clusters;
    }

    public List<Instance> getHosts() {
        return hosts;
    }

    @Override
    public String scope() {
        return this.eventScope;
    }
}
