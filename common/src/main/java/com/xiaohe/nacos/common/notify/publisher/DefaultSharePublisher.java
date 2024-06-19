package com.xiaohe.nacos.common.notify.publisher;

import com.xiaohe.nacos.common.notify.event.Event;
import com.xiaohe.nacos.common.notify.event.SlowEvent;
import com.xiaohe.nacos.common.notify.listener.Subscriber;
import com.xiaohe.nacos.common.utils.ConcurrentHashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultSharePublisher extends DefaultPublisher implements ShardedEventPublisher {

    private final Map<Class<? extends SlowEvent>, Set<Subscriber>> subMappings = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();

    @Override
    public void addSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType) {
        Class<? extends SlowEvent> subSlowScribeType = (Class<? extends SlowEvent>) subscribeType;
        subscribers.add(subscriber);
        lock.lock();
        try {
            Set<Subscriber> sets = subMappings.get(subSlowScribeType);
            if (sets == null) {
                Set<Subscriber> newSet = new ConcurrentHashSet<>();
                newSet.add(subscriber);
                subMappings.put(subSlowScribeType, newSet);
                return;
            }
            sets.add(subscriber);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType) {
        Class<? extends SlowEvent> subSlowEventType = (Class<? extends SlowEvent>) subscribeType;
        subscribers.remove(subscriber);
        lock.lock();
        try {
            Set<Subscriber> sets = subMappings.get(subSlowEventType);
            if (sets != null) {
                sets.remove(subscriber);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 分发事件
     * @param event
     */
    @Override
    void receiveEvent(Event event) {
        long sequence = event.sequence();
        final Class<? extends SlowEvent> slowEventType = (Class<? extends SlowEvent>) event.getClass();
        Set<Subscriber> subscribers = subMappings.get(slowEventType);
        if (subscribers == null) {
            return;
        }
        for (Subscriber subscriber : subscribers) {
            if (subscriber.ignoreExpireEvent() && lastEventSequence > sequence) {
                continue;
            }
            notifySubscriber(subscriber, event);
        }
    }
}
