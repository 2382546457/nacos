package com.xiaohe.nacos.api.naming.listener;

import java.util.concurrent.Executor;

public abstract class AbstractEventListener implements EventListener {

    public Executor getExecutor() {
        return null;
    }

}
