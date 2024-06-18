package com.xiaohe.nacos.common.log;

public interface NacosLogbackProperties {
    /**
     * get value
     * @param source
     * @param defaultValue
     * @return
     */
    String getValue(String source, String defaultValue);
}
