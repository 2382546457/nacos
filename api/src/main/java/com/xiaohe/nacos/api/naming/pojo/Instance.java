package com.xiaohe.nacos.api.naming.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xiaohe.nacos.api.common.Constants;
import com.xiaohe.nacos.api.naming.PreservedMetadataKeys;
import com.xiaohe.nacos.api.naming.utils.NamingUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 服务实例
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Instance {

    private static final long serialVersionUID = -742906310567291979L;

    private String instanceId;

    private String ip;

    private int port;

    private double weight = 1.00;

    /**
     * 是否健康
     */
    private boolean healthy = true;

    /**
     * 是否可用
     */
    private boolean enabled = true;

    /**
     * 是否为临时实例
     * 临时实例存放于内存
     * 永久实例存放于数据库/文件系统
     */
    private boolean ephemeral = true;

    /**
     * 服务所在集群
     */
    private String clusterName;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 元数据信息
     */
    private Map<String, String> metadata = new HashMap<>();

    /**
     * 从元数据中取数据，有默认值
     * @param key
     * @param defaultValue
     * @return
     */
    private long getMetaDataByKeyWithDefault(final String key, final long defaultValue) {
        if (getMetadata() == null || getMetadata().isEmpty()) {
            return defaultValue;
        }
        final String value = getMetadata().get(key);
        if (NamingUtils.isNumber(value)) {
            return Long.parseLong(value);
        }
        return defaultValue;
    }

    /**
     * 获取服务实例的心跳间隔时间，默认5s
     * @return
     */
    public long getInstanceHeartBeatInterval() {
        return getMetaDataByKeyWithDefault(PreservedMetadataKeys.HEART_BEAT_INTERVAL, Constants.DEFAULT_HEART_BEAT_INTERVAL);
    }

    /**
     * 获取心跳超时时间，默认15s
     * @return
     */
    public long getInstanceHeartBeatTimeOut() {
        return getMetaDataByKeyWithDefault(PreservedMetadataKeys.HEART_BEAT_TIMEOUT, Constants.DEFAULT_HEART_BEAT_TIMEOUT);
    }

    /**
     * 获取可以删除服务实例的IP的时间，默认30s
     * @return
     */
    public long getIpDeleteTimeout() {
        return getMetaDataByKeyWithDefault(PreservedMetadataKeys.IP_DELETE_TIMEOUT, Constants.DEFAULT_IP_DELETE_TIMEOUT);
    }



    public String getInstanceId() {
        return this.instanceId;
    }

    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(final String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(final double weight) {
        this.weight = weight;
    }

    public boolean isHealthy() {
        return this.healthy;
    }

    public void setHealthy(final boolean healthy) {
        this.healthy = healthy;
    }

    public String getClusterName() {
        return this.clusterName;
    }

    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    public void setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
    }

    //添加实例元数据信息到Map中的方法
    public void addMetadata(final String key, final String value) {
        if (metadata == null) {
            metadata = new HashMap<>(4);
        }
        metadata.put(key, value);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEphemeral() {
        return this.ephemeral;
    }

    public void setEphemeral(final boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    @Override
    public String toString() {
        return "Instance{" + "instanceId='" + instanceId + '\'' + ", ip='" + ip + '\'' + ", port=" + port + ", weight="
                + weight + ", healthy=" + healthy + ", enabled=" + enabled + ", ephemeral=" + ephemeral
                + ", clusterName='" + clusterName + '\'' + ", serviceName='" + serviceName + '\'' + ", metadata="
                + metadata + '}';
    }

    public String toInetAddr() {
        return ip + ":" + port;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Instance)) {
            return false;
        }

        final Instance host = (Instance) obj;
        return Instance.strEquals(host.toString(), toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    private static boolean strEquals(final String str1, final String str2) {
        return Objects.equals(str1, str2);
    }

}
