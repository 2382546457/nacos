package com.xiaohe.nacos.common.notify.publisher;

import com.xiaohe.nacos.common.lifecycle.Closeable;
import com.xiaohe.nacos.common.notify.event.Event;
import com.xiaohe.nacos.common.notify.listener.Subscriber;

/**
 * 事件发布器
 */
public interface EventPublisher extends Closeable {

    /**
     * 初始化事件发布器
     * @param type 此事件发布器关注的事件类型
     * @param bufferSize 事件队列的容量
     */
    void init(Class<? extends Event> type, int bufferSize);

    long currentEventSize();


    /**
     * 给事件添加观察者
     * @param subscriber
     */
    void addSubscriber(Subscriber subscriber);

    /**
     * 移除观察者
     * @param subscriber
     */
    void removeSubscriber(Subscriber subscriber);

    /**
     * 发布事件
     * @param event
     * @return
     */
    boolean publish(Event event);

    /**
     * 通知对应的观察者
     * @param subscriber
     * @param event
     */
    void notifySubscriber(Subscriber subscriber, Event event);

}
