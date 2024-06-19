package com.xiaohe.nacos.common.notify.listener;

import com.xiaohe.nacos.common.notify.event.Event;

import java.util.List;

public abstract class SmartSubscriber extends Subscriber<Event> {


    public abstract List<Class<? extends Event>> subscribeTypes();

    @Override
    public final Class<? extends Event> subscribeType() {
        return null;
    }

    @Override
    public final boolean ignoreExpireEvent() {
        return false;
    }
}