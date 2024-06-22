package com.xiaohe.nacos.client.naming.backups;


import com.xiaohe.nacos.api.naming.pojo.ServiceInfo;
import com.xiaohe.nacos.client.naming.cache.ConcurrentDiskUtil;
import com.xiaohe.nacos.client.naming.cache.DiskCache;
import com.xiaohe.nacos.client.naming.cache.ServiceInfoHolder;
import com.xiaohe.nacos.client.naming.utils.UtilAndComs;
import com.xiaohe.nacos.common.utils.CollectionUtils;
import com.xiaohe.nacos.common.utils.JacksonUtils;
import com.xiaohe.nacos.common.utils.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 故障转移器
 */
public class FailoverReactor {

    private static final String FAILOVER_DIR = "/failover";

    /**
     * 故障转移功能当前状态，这个表示开启状态
     */
    private static final String IS_FAILOVER_MODE = "1";
    /**
     * 关闭状态
     */
    private static final String NO_FAILOVER_MODE = "0";

    /**
     * 获取是否开启故障转移的 key
     */
    private static final String FAILOVER_MODE_PARAM = "failover-mode";

    /**
     * 存放服务信息
     */
    private Map<String, ServiceInfo> serviceMap = new ConcurrentHashMap<>();

    /**
     * 故障转移是否开启的信息
     */
    private final Map<String, String> switchParams = new ConcurrentHashMap<>();

    /**
     * 定义了定时任务的执行时间
     */
    private static final long DAY_PERIOD_MINUTES = 24 * 60;

    /**
     * 故障转移文件的完整目录
     */
    private final String failoverDir;

    /**
     * 服务实例信息保存器
     */
    private final ServiceInfoHolder serviceInfoHolder;

    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService executorService;

    public FailoverReactor(ServiceInfoHolder serviceInfoHolder, String cacheDir) {
        this.serviceInfoHolder = serviceInfoHolder;
        this.failoverDir = cacheDir +  FAILOVER_DIR;

        this.executorService = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("com.alibaba.nacos.naming.failover");
            return thread;
        });
        this.init();
    }

    private void init() {
        executorService.scheduleWithFixedDelay(new SwitchRefresher(), 0L, 5000L, TimeUnit.MILLISECONDS);
        executorService.scheduleWithFixedDelay(new DiskFileWriter(), 30, DAY_PERIOD_MINUTES, TimeUnit.MILLISECONDS);
        executorService.schedule(() -> {
            try {
                File cacheDir = new File(failoverDir);
                if (!cacheDir.exists() && cacheDir.mkdirs()) {
                    throw new IllegalStateException("failed to create cache dir : " + failoverDir);
                }
                File[] files = cacheDir.listFiles();
                if (files == null || files.length <= 0) {
                    new DiskFileWriter().run();
                }
            } catch (Throwable e) {

            }
        }, 10000L, TimeUnit.MILLISECONDS);
    }


    public boolean isFailoverSwitch() {
        return Boolean.parseBoolean(switchParams.get(FAILOVER_MODE_PARAM));
    }

    public ServiceInfo getService(String key) {
        ServiceInfo serviceInfo = serviceMap.get(key);
        if (serviceInfo == null) {
            serviceInfo = new ServiceInfo();
            serviceInfo.setName(key);
        }
        return serviceInfo;
    }

    class DiskFileWriter extends TimerTask {
        @Override
        public void run() {
            Map<String, ServiceInfo> map = serviceInfoHolder.getServiceInfoMap();
            for (Map.Entry<String, ServiceInfo> entry : map.entrySet()) {
                ServiceInfo serviceInfo = entry.getValue();
                if (StringUtils.equals(serviceInfo.getKey(), UtilAndComs.ALL_IPS) || StringUtils
                        .equals(serviceInfo.getName(), UtilAndComs.ENV_LIST_KEY) || StringUtils
                        .equals(serviceInfo.getName(), UtilAndComs.ENV_CONFIGS) || StringUtils
                        .equals(serviceInfo.getName(), UtilAndComs.VIP_CLIENT_FILE) || StringUtils
                        .equals(serviceInfo.getName(), UtilAndComs.ALL_HOSTS)) {
                    continue;
                }
                //将数据写入到文件中
                DiskCache.write(serviceInfo, failoverDir);
            }
        }
    }

    class SwitchRefresher implements Runnable {
        //故障转移功能开关文件最新修改时间
        long lastModifiedMillis = 0L;
        @Override
        public void run() {
            try {
                // 得到故障转移开关文件
                File switchFile = Paths.get(failoverDir, UtilAndComs.FAILOVER_SWITCH).toFile();
                // 判断文件是否存在
                if (!switchFile.exists()) {
                    // 如果文件不存在则意味着没有开启故障转移功能，直接在map中设置故障转移功能状态为false，也就是关闭即可
                    switchParams.put(FAILOVER_MODE_PARAM, Boolean.FALSE.toString());
                    return;
                }
                // 得到文件最新修改时间
                long modified = switchFile.lastModified();
                // 判断文件是否更新了
                if (lastModifiedMillis < modified) {
                    // 如果更新了则给最后更新时间按赋值
                    lastModifiedMillis = modified;
                    // 读取文件内容
                    String failover = ConcurrentDiskUtil.getFileContent(switchFile.getPath(), Charset.defaultCharset().toString());
                    // 接下来就是判断文件是否有内容，如果有内容就判断文件中是否有故障转移功能开启标志
                    if (!StringUtils.isEmpty(failover)) {
                        String[] lines = failover.split(DiskCache.getLineSeparator());
                        for (String line : lines) {
                            // 去掉空格
                            String line1 = line.trim();
                            // 判断这一行的内容是不是1，如果是1就意味着开启了故障转移功能
                            if (IS_FAILOVER_MODE.equals(line1)) {
                                // 把故障转移功能的开关设置为开启状态
                                switchParams.put(FAILOVER_MODE_PARAM, Boolean.TRUE.toString());
                                // 在这里把故障转移文件中的内容读取到内存中，也就是serviceMap中
                                // 注意，当前任务是一个定时任务，每5秒执行一次，这也就意味着每5秒就会把故障转移文件中的数据加载到内存中一次
                                new FailoverFileReader().run();
                            } else if (NO_FAILOVER_MODE.equals(line1)) {
                                // 如果文件中的内容为0，也就意味着没有开启故障转移文件，那就把故障转移功能的开关设置为false
                                switchParams.put(FAILOVER_MODE_PARAM, Boolean.FALSE.toString());
                            }
                        }
                    } else {
                        // 走到这里意味着文件没有内容，那么也把故障转移功能开关设置为false
                        switchParams.put(FAILOVER_MODE_PARAM, Boolean.FALSE.toString());
                    }
                }
            } catch (Throwable e) {

            }
        }
    }
    class FailoverFileReader implements Runnable {

        @Override
        public void run() {
            Map<String, ServiceInfo> domMap = new HashMap<>(16);
            BufferedReader reader = null;
            try {
                // 得到故障转移文件目录
                File cacheDir = new File(failoverDir);
                // 判断文件夹是否存在，不存在则创建
                if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                    throw new IllegalStateException("failed to create cache dir: " + failoverDir);
                }
                // 得到目录下的所有文件
                File[] files = cacheDir.listFiles();
                if (files == null) {
                    return;
                }
                // 遍历每一个文件
                for (File file : files) {
                    if (!file.isFile()) {
                        continue;
                    }
                    // 如果是故障转移开关文件则跳过本次循环
                    if (file.getName().equals(UtilAndComs.FAILOVER_SWITCH)) {
                        continue;
                    }
                    ServiceInfo dom = null;
                    try {
                        dom = new ServiceInfo(URLDecoder.decode(file.getName(), StandardCharsets.UTF_8.name()));
                        String dataString = ConcurrentDiskUtil.getFileContent(file, Charset.defaultCharset().toString());
                        reader = new BufferedReader(new StringReader(dataString));
                        String json;
                        // 读取文件每一行内容
                        if ((json = reader.readLine()) != null) {
                            try {
                                // 得到ServiceInfo对象
                                dom = JacksonUtils.toObj(json, ServiceInfo.class);
                            } catch (Exception e) {

                            }
                        }
                    } catch (Exception e) {

                    } finally {
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (Exception e) {

                        }
                    }
                    // 判断ServiceInfo对象是否不为空，也就是ServiceInfo对象中存储的服务实例信息不为空
                    if (dom != null && !CollectionUtils.isEmpty(dom.getHosts())) {
                        // 把对象放到map中
                        domMap.put(dom.getKey(), dom);
                    }
                }
            } catch (Exception e) {

            }
            if (!domMap.isEmpty()) {
                // 在这里把map赋值给serviceMap
                serviceMap = domMap;
            }
        }
    }

}
