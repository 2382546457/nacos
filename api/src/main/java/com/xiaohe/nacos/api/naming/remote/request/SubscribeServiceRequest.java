package com.xiaohe.nacos.api.naming.remote.request;

/**
 * 订阅服务的请求
 */
public class SubscribeServiceRequest extends AbstractNamingRequest {

    private boolean subscribe;

    private String clusters;

    public SubscribeServiceRequest(String namespace, String groupName, String serviceName, String clusters,
                                   boolean subscribe) {
        super(namespace, serviceName, groupName);
        this.clusters = clusters;
        this.subscribe = subscribe;
    }

    public String getClusters() {
        return clusters;
    }

    public void setClusters(String clusters) {
        this.clusters = clusters;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }
}
