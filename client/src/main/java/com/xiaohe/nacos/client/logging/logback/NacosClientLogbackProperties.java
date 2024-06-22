package com.xiaohe.nacos.client.logging.logback;


import com.xiaohe.nacos.client.env.NacosClientProperties;
import com.xiaohe.nacos.common.log.NacosLogbackProperties;

/**
 * adapter to higher version of logback (>= 1.4.5).
 *
 * @author hujun
 */
public class NacosClientLogbackProperties implements NacosLogbackProperties {

    @Override
    public String getValue(String source, String defaultValue) {
        return NacosClientProperties.PROTOTYPE.getProperty(source, defaultValue);
    }
}