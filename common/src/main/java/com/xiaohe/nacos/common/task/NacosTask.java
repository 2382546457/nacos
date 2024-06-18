package com.xiaohe.nacos.common.task;

public interface NacosTask {

    /**
     * 判断这个 NacosTask 是否要执行
     * @return
     */
    boolean shouldProcess();
}
