package com.xiaohe.nacos.client.utils;


import com.xiaohe.nacos.common.utils.JacksonUtils;

public class PreInitUtils {

    @SuppressWarnings("PMD.AvoidManuallyCreateThreadRule")
    public static void asyncPreLoadCostComponent() {
        Thread preLoadThread = new Thread(() -> {
            JacksonUtils.createEmptyJsonNode();
        });
        preLoadThread.start();
    }
}

