package com.xiaohe.nacos.common.notify.publisher;

import com.xiaohe.nacos.common.notify.event.Event;

import java.util.function.BiFunction;

public interface EventPublisherFactory extends BiFunction<Class<? extends Event>, Integer, EventPublisher> {

    /**
     * 创建一个 EventPublisher
     * @param eventType 事件发布器监听的类型
     * @param maxQueueSize 时间发布器的容量
     * @return
     */
    @Override
    EventPublisher apply(Class<? extends Event> eventType, Integer maxQueueSize);
}
