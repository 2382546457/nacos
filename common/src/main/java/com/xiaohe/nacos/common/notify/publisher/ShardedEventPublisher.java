package com.xiaohe.nacos.common.notify.publisher;

import com.xiaohe.nacos.common.notify.event.Event;
import com.xiaohe.nacos.common.notify.listener.Subscriber;

public interface ShardedEventPublisher extends EventPublisher {

    /**
     * 添加订阅者，该订阅者会订阅指定类型的事件
     */
    void addSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType);

    /**
     * 移除监听者
     */
    void removeSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType);
}