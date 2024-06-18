package com.xiaohe.nacos.common.log;

import java.net.URL;

public interface NacosLogbackConfigurator {

    void configure(URL resourceUrl) throws Exception;

    int getVersion();

    void setContext(Object loggerContext);
}
