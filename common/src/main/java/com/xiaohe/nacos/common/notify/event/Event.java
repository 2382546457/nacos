package com.xiaohe.nacos.common.notify.event;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Event implements Serializable {
    private static final long serialVersionUID = -3731383194964997493L;

    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    private final long sequence = SEQUENCE.getAndIncrement();

    public long sequence() {
        return sequence;
    }

    /**
     * Event scope.
     * @return
     */
    public String scope() {
        return null;
    }

    /**
     * 这是事件是否为可插拔式的、可移除的
     * @return
     */
    public boolean isPluginEvent() {
        return false;
    }
}
