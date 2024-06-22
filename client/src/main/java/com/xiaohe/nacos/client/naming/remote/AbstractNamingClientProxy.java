package com.xiaohe.nacos.client.naming.remote;

import com.xiaohe.nacos.client.naming.event.ServerListChangedEvent;
import com.xiaohe.nacos.client.utils.AppNameUtils;
import com.xiaohe.nacos.common.notify.listener.Subscriber;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractNamingClientProxy extends Subscriber<ServerListChangedEvent> implements NamingClientProxy {
    private static final String APP_FILED = "app";

    protected Map<String, String> getSecurityHeaders() {
        Map<String, String> result = new HashMap<>();
        result.putAll(getAppHeaders());
        return result;
    }

    protected Map<String, String> getAppHeaders() {
        Map<String, String> result = new HashMap<>(1);
        result.put(APP_FILED, AppNameUtils.getAppName());
        return result;
    }
}
