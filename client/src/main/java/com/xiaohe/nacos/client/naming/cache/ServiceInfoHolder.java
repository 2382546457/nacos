package com.xiaohe.nacos.client.naming.cache;


import com.xiaohe.nacos.api.PropertyKeyConst;
import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.naming.pojo.Instance;
import com.xiaohe.nacos.api.naming.pojo.ServiceInfo;
import com.xiaohe.nacos.api.naming.utils.NamingUtils;
import com.xiaohe.nacos.client.env.NacosClientProperties;
import com.xiaohe.nacos.client.naming.backups.FailoverReactor;
import com.xiaohe.nacos.client.naming.event.InstancesChangeEvent;
import com.xiaohe.nacos.common.lifecycle.Closeable;
import com.xiaohe.nacos.common.notify.NotifyCenter;
import com.xiaohe.nacos.common.utils.ConvertUtils;
import com.xiaohe.nacos.common.utils.JacksonUtils;
import com.xiaohe.nacos.common.utils.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 客户端保存从服务端拉取的服务信息
 */
public class ServiceInfoHolder implements Closeable {
    //用来从系统环境变量中得到本地用来存储服务实例信息的文件目录的键
    private static final String JM_SNAPSHOT_PATH_PROPERTY = "JM.SNAPSHOT.PATH";

    //本地存储服务实例信息的文件目录的一部分
    private static final String FILE_PATH_NACOS = "nacos";

    //本地存储服务实例信息的文件目录的一部分
    private static final String FILE_PATH_NAMING = "naming";

    //用来从系统环境变量中得到用户主目录的键
    private static final String USER_HOME_PROPERTY = "user.home";

    /**
     * 从服务端获取的服务实例信息
     * key : 服务标识，groupName + @@ + serviceName + @@ + clusters
     * value : ServiceInfo, 该服务下所有的服务实例信息
     */
    private final ConcurrentMap<String, ServiceInfo> serviceInfoMap;

    /**
     * 空推送保护机制
     */
    private final boolean pushEmptyProtection;

    /**
     * 本地保存服务实例信息的磁盘路径
     */
    private String cacheDir;

    /**
     * 事件通知唯一标识
     */
    private String notifierEventScope;

    /**
     * 故障转移器
     */
    private final FailoverReactor failoverReactor;

    public ServiceInfoHolder(String namespace, String notifierEventScope, NacosClientProperties properties) {
        // 初始化本地磁盘
        initCacheDir(namespace, properties);
        // 初始化时要不要将磁盘中存储的信息加载到内存中
        if (isLoadCacheAtStart(properties)) {
            this.serviceInfoMap = new ConcurrentHashMap<>(DiskCache.read(this.cacheDir));
        } else {
            this.serviceInfoMap = new ConcurrentHashMap<>(16);
        }

        //创建故障转移器
        this.failoverReactor = new FailoverReactor(this, cacheDir);
        //初始化空推送保护机制
        this.pushEmptyProtection = isPushEmptyProtect(properties);
        this.notifierEventScope = notifierEventScope;

    }

    private void initCacheDir(String namespace, NacosClientProperties properties) {
        // 从配置文件中得到用户自定义的文件目录
        String jmSnapshotPath = properties.getProperty(JM_SNAPSHOT_PATH_PROPERTY);
        String namingCacheRegistryDir = "";
        // 如果用户还定义了文件的部分目录，就把这个也拼接到完整的目录中
        if (properties.getProperty(PropertyKeyConst.NAMING_CACHE_REGISTRY_DIR) != null) {
            namingCacheRegistryDir = File.separator + properties.getProperty(PropertyKeyConst.NAMING_CACHE_REGISTRY_DIR);
        }
        // 拼接得到完整文件目录的操作
        if (!StringUtils.isBlank(jmSnapshotPath)) {
            // 得到的是用户自定义的文件目录
            cacheDir = jmSnapshotPath + File.separator + FILE_PATH_NACOS + namingCacheRegistryDir
                    + File.separator + FILE_PATH_NAMING + File.separator + namespace;
        } else {
            // 得到的就是默认的文件目录，这里面只有一个是用户自己定义的，那就是namingCacheRegistryDir这个目录
            // 如果用户没有自定义任何目录，最后得到的存储服务实例信息的目录就是这样的：${user.home}/nacos/naming/public
            cacheDir = properties.getProperty(USER_HOME_PROPERTY) + File.separator + FILE_PATH_NACOS + namingCacheRegistryDir + File.separator + FILE_PATH_NAMING + File.separator + namespace;
        }
    }

    /**
     * 是否在程序启动时把本地存储的服务实例信息加载到内存中
     * @param properties
     * @return
     */
    private boolean isLoadCacheAtStart(NacosClientProperties properties) {
        boolean loadCacheAtStart = false;
        if (properties != null && StringUtils.isNotEmpty(properties.getProperty(PropertyKeyConst.NAMING_LOAD_CACHE_AT_START))) {
            loadCacheAtStart = ConvertUtils.toBoolean(properties.getProperty(PropertyKeyConst.NAMING_LOAD_CACHE_AT_START));
        }
        return loadCacheAtStart;
    }

    /**
     * 是否启用空推送保护机制
     * @param properties
     * @return
     */
    private boolean isPushEmptyProtect(NacosClientProperties properties) {
        boolean pushEmptyProtection = false;
        if (properties != null && StringUtils.isNotEmpty(properties.getProperty(PropertyKeyConst.NAMING_PUSH_EMPTY_PROTECTION))) {
            pushEmptyProtection = ConvertUtils.toBoolean(properties.getProperty(PropertyKeyConst.NAMING_PUSH_EMPTY_PROTECTION));
        }
        return pushEmptyProtection;
    }

    /**
     * 获取客户端缓存的所有服务实例信息的方法
     * @return
     */
    public Map<String, ServiceInfo> getServiceInfoMap() {
        return serviceInfoMap;
    }

    /**
     * 根据服务信息得到对应的服务实例
     * @param serviceName
     * @param groupName
     * @param clusters
     * @return
     */
    public ServiceInfo getServiceInfo(final String serviceName, final String groupName, final String clusters) {
        // groupName + @@ + serviceName
        String groupedServiceName = NamingUtils.getGroupedName(serviceName, groupName);
        // groupName + @@ + serviceName + @@ + clusters
        String key = ServiceInfo.getKey(groupedServiceName, clusters);
        // 如果开启了故障转移模式，就优先从故障转移器中获得服务实例
        if (failoverReactor.isFailoberSwitch()) {
            return failoverReactor.getService(key);
        }
        return serviceInfoMap.get(key);

    }

    /**
     * 将服务端发送的 ServiceInfo 与本地的 ServiceInfo 做对比
     * @param serviceInfo
     * @return
     */
    public ServiceInfo processServiceInfo(ServiceInfo serviceInfo) {
        String serviceKey = serviceInfo.getKey();
        if (serviceKey == null) {
            return null;
        }
        ServiceInfo oldService = serviceInfoMap.get(serviceInfo.getKey());
        // 如果接收到的是空推送对象，就不清空服务实例，而是将旧实例返回
        if (isEmptyOrErrorPush(serviceInfo)) {
            return oldService;
        }
        boolean changed = isChangedServiceInfo(oldService, serviceInfo);
        if (StringUtils.isBlank(serviceInfo.getJsonFromServer())) {
            serviceInfo.setJsonFromServer(JacksonUtils.toJson(serviceInfo));
        }
        if (changed) {
            // 其他客户端的信息更新了，发布事件
            NotifyCenter.publishEvent(new InstancesChangeEvent(notifierEventScope, serviceInfo.getName(), serviceInfo.getGroupName(), serviceInfo.getClusters(), serviceInfo.getHosts()));
            DiskCache.write(serviceInfo, cacheDir);
        }
        return serviceInfo;
    }

    /**
     * 判断这两个 ServiceInfo 是否一样
     * @param oldService
     * @param serviceInfo
     * @return
     */
    public boolean isChangedServiceInfo(ServiceInfo oldService, ServiceInfo serviceInfo) {
        if (oldService == null) {
            return true;
        }
        if (oldService.getLastRefTime() > serviceInfo.getLastRefTime()) {
            return false;
        }
        boolean changed = false;
        // 存放旧 ServiceInfo 对象中所有服务实例
        Map<String, Instance> oldHostMap = new HashMap<>(oldService.getHosts().size());
        for (Instance instance : oldService.getHosts()) {
            oldHostMap.put(instance.toInetAddr(), instance);
        }
        // 存放新 ServiceInfo 对象中所有服务实例
        Map<String, Instance> newHostMap = new HashMap<>(serviceInfo.getHosts().size());
        for (Instance host : serviceInfo.getHosts()) {
            newHostMap.put(host.toInetAddr(), host);
        }

        Set<Instance> modHosts = new HashSet<>();
        Set<Instance> newHosts = new HashSet<>();
        Set<Instance> remvHosts = new HashSet<>();
        // 对比新旧ServiceInfo对象是否不同的操作
        List<Map.Entry<String, Instance>> newServiceHosts = new ArrayList<>(newHostMap.entrySet());
        for (Map.Entry<String, Instance> entry : newServiceHosts) {
            Instance host = entry.getValue();
            String key = entry.getKey();
            // 判断是否为更新的服务实例
            if (oldHostMap.containsKey(key) && !StringUtils.equals(host.toString(), oldHostMap.get(key).toString())) {
                modHosts.add(host);
                continue;
            }
            // 判断是否新增了服务实例
            if (!oldHostMap.containsKey(key)) {
                newHosts.add(host);
            }
        }
        // 判断是否删除了服务实例
        for (Map.Entry<String, Instance> entry : oldHostMap.entrySet()) {
            Instance host = entry.getValue();
            String key = entry.getKey();
            if (!newHostMap.containsKey(key)) {
                remvHosts.add(host);
            }
        }
        // 只要上面的三个判断有一个满足，就意味着ServiceInfo对象更新了
        if (newHosts.size() > 0 || remvHosts.size() > 0 || modHosts.size() > 0) {
            changed = true;
        }
        return changed;
    }

    /**
     * 判断接收到的ServiceInfo对象是否为空推送对象
     * @param serviceInfo
     * @return
     */
    private boolean isEmptyOrErrorPush(ServiceInfo serviceInfo) {
        return null == serviceInfo.getHosts() || (pushEmptyProtection && !serviceInfo.validate());
    }

    @Override
    public void shutdown() throws NacosException {

    }
}
