package com.xiaohe.nacos.common.notify.event;

public abstract class SlowEvent extends Event {

    @Override
    public long sequence() {
        return 0;
    }
}