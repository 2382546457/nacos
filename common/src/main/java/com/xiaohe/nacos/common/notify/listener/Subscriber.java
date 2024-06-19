package com.xiaohe.nacos.common.notify.listener;

import com.xiaohe.nacos.common.notify.event.Event;

import java.util.concurrent.Executor;

public abstract class Subscriber<T extends Event> {

    public abstract void onEvent(T event);

    public abstract Class<? extends Event> subscribeType();

    public Executor executor() {
        return null;
    }

    public boolean ignoreExpireEvent() {
        return false;
    }

    public boolean scopeMatches(T event) {
        return true;
    }
}
