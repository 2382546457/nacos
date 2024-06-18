package com.xiaohe.nacos.common.lifecycle;

import com.xiaohe.nacos.api.exception.NacosException;

public interface Closeable {

    void shutdown() throws NacosException;

}
