package com.xiaohe.nacos.api.naming.pojo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xiaohe.nacos.api.common.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceInfo {
    private static final String EMPTY = "";
    private static final String DEFAULT_CHARSET = "UTF-8";

    @JsonIgnore
    private String jsonFromServer = EMPTY;

    // 服务名称
    private String name;

    // 组名
    private String groupName;

    // 集群名称
    private String clusters;

    /**
     * 这个 serviceInfo 对象中包含的所有实例信息
     */
    private List<Instance> hosts = new ArrayList<>();

    /**
     * 当前服务实例要在客户端缓存的时长，服务端每次传递该对象时会将该值设置为 10000
     */
    private long cacheMillis = 1000L;

    // 最新引用时间
    private Long lastRefTime = 0L;

    private String checksum = "";

    // 该 serviceInfo 是否持有所有服务实例信息
    private volatile boolean allIPs = false;

    // 是否达到保护阈值
    private volatile boolean reachProtectionThreshold = false;


    //groupName在key中的位置
    private static final int GROUP_POSITION = 0;

    //service name在key中的位置
    private static final int SERVICE_POSITION = 1;

    //集群名称在key中的位置
    private static final int CLUSTER_POSITION = 2;

    //key的长度
    //以上几个成员变量在本类的ServiceInfo(key)方法中会被用到，直接去这个方法中就能很清楚地明白这几个变量的作用
    private static final int FILE_NAME_PARTS = 3;

    // 根据 key 构造 ServiceInfo
    public ServiceInfo(final String key) {
        String[] keys = key.split(Constants.SERVICE_INFO_SPLITER);
        // 数组长度为3，包含组名、服务名、集群名。
        // 数组长度为2，包含组名、服务名
        if (keys.length >= FILE_NAME_PARTS) {
            this.groupName = keys[GROUP_POSITION];
            this.name = keys[SERVICE_POSITION];
            this.clusters = keys[CLUSTER_POSITION];
        } else if (keys.length == CLUSTER_POSITION) {
            this.groupName = keys[GROUP_POSITION];
            this.name = keys[SERVICE_POSITION];
            this.clusters = null;
        } else {
            throw new IllegalArgumentException("Can't parse out 'groupName',but it must not be null!");
        }
    }




    public ServiceInfo() {
    }

    public boolean isAllIPs() {
        return allIPs;
    }

    public void setAllIPs(boolean allIPs) {
        this.allIPs = allIPs;
    }
    public ServiceInfo(String name, String clusters) {
        this.name = name;
        this.clusters = clusters;
    }

    public int ipCount() {
        return hosts.size();
    }

    public boolean expired() {
        return System.currentTimeMillis() - lastRefTime > cacheMillis;
    }

    public void setHosts(List<Instance> hosts) {
        this.hosts = hosts;
    }

    public void addHost(Instance host) {
        hosts.add(host);
    }

    public void addAllHosts(List<? extends Instance> hosts) {
        this.hosts.addAll(hosts);
    }

    public List<Instance> getHosts() {
        return new ArrayList<>(hosts);
    }

    public boolean isValid() {
        return hosts != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setLastRefTime(long lastRefTime) {
        this.lastRefTime = lastRefTime;
    }

    public long getLastRefTime() {
        return lastRefTime;
    }

    public String getClusters() {
        return clusters;
    }

    public void setClusters(String clusters) {
        this.clusters = clusters;
    }

    //当前服务实例要在客户端缓存的时常，默认是1000，但是这个ServiceInfo对象封装的信息是从服务端传递过来的
    //服务端每次传送该对象是会把缓存时间设置成10000
    public long getCacheMillis() {
        return cacheMillis;
    }

    public void setCacheMillis(long cacheMillis) {
        this.cacheMillis = cacheMillis;
    }

    /**
     * Judge whether service info is validate.
     *
     * @return true if validate, otherwise false
     */
    public boolean validate() {
        if (isAllIPs()) {
            return true;
        }
        if (hosts == null) {
            return false;
        }
        boolean existValidHosts = false;
        for (Instance host : hosts) {
            if (host.isHealthy() && host.getWeight() > 0) {
                existValidHosts = true;
                break;
            }
        }
        return existValidHosts;
    }

    @JsonIgnore
    public String getJsonFromServer() {
        return jsonFromServer;
    }

    public void setJsonFromServer(String jsonFromServer) {
        this.jsonFromServer = jsonFromServer;
    }

    @JsonIgnore
    public String getKey() {
        String serviceName = getGroupedServiceName();
        return getKey(serviceName, clusters);
    }

    @JsonIgnore
    public static String getKey(String name, String clusters) {

        if (!isEmpty(clusters)) {
            return name + Constants.SERVICE_INFO_SPLITER + clusters;
        }

        return name;
    }

    @JsonIgnore
    public String getKeyEncoded() {
        String serviceName = getGroupedServiceName();
        try {
            serviceName = URLEncoder.encode(serviceName, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException ignored) {
        }
        return getKey(serviceName, clusters);
    }

    private String getGroupedServiceName() {
        String serviceName = this.name;
        if (!isEmpty(groupName) && !serviceName.contains(Constants.SERVICE_INFO_SPLITER)) {
            serviceName = groupName + Constants.SERVICE_INFO_SPLITER + serviceName;
        }
        return serviceName;
    }

    /**
     * Get {@link ServiceInfo} from key.
     *
     * @param key key of service info
     * @return new service info
     */
    public static ServiceInfo fromKey(final String key) {
        return new ServiceInfo(key);
    }

    @Override
    public String toString() {
        return getKey();
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public boolean isReachProtectionThreshold() {
        return reachProtectionThreshold;
    }

    public void setReachProtectionThreshold(boolean reachProtectionThreshold) {
        this.reachProtectionThreshold = reachProtectionThreshold;
    }
}
