package com.xiaohe.nacos.common.notify;

import com.xiaohe.nacos.api.exception.runtime.NacosRuntimeException;
import com.xiaohe.nacos.common.notify.event.Event;
import com.xiaohe.nacos.common.notify.event.SlowEvent;
import com.xiaohe.nacos.common.notify.listener.SmartSubscriber;
import com.xiaohe.nacos.common.notify.listener.Subscriber;
import com.xiaohe.nacos.common.notify.publisher.*;
import com.xiaohe.nacos.common.spi.NacosServiceLoader;
import com.xiaohe.nacos.common.utils.ClassUtils;
import com.xiaohe.nacos.common.utils.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import com.xiaohe.nacos.common.utils.ThreadUtils;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.xiaohe.nacos.api.exception.NacosException.SERVER_ERROR;

/**
 * 事件通知中心
 */
public class NotifyCenter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyCenter.class);


    public static int ringBufferSize;

    public static int shareBufferSize;

    /**
     * 通知中心是否关闭
     */
    private static final AtomicBoolean CLOSED = new AtomicBoolean(false);

    private static NotifyCenter INSTANCE = new NotifyCenter();

    /**
     * 事件发布器的工厂
     */
    private static final EventPublisherFactory DEFAULT_PUBLISHER_FACTORY;

    private DefaultSharePublisher sharePublisher;

    /**
     * 默认使用的事件发布器类型，使用SPI机制加载，用户没有设置就使用默认的 DefaultPublisher
     */
    private static Class<? extends EventPublisher> clazz;

    private final Map<String, EventPublisher> publisherMap = new ConcurrentHashMap<>(16);

    static {
        String ringBufferSizeProperty = "nacos.core.notify.ring-buffer-size";
        ringBufferSize = Integer.getInteger(ringBufferSizeProperty, 16384);

        String shareBufferSizeProperty = "nacos.core.notify.share-buffer-size";
        shareBufferSize = Integer.getInteger(shareBufferSizeProperty, 1024);

        final Collection<EventPublisher> publishers = NacosServiceLoader.load(EventPublisher.class);
        Iterator<EventPublisher> iterator = publishers.iterator();
        if (iterator.hasNext()) {
            clazz = iterator.next().getClass();
        } else {
            clazz = DefaultPublisher.class;
        }
        // 通过SPI获取发布器类型后，创建对象并赋值、初始化
        DEFAULT_PUBLISHER_FACTORY = (cls, buffer) -> {
            try {
                EventPublisher publisher = clazz.newInstance();
                publisher.init(cls, buffer);
                return publisher;
            } catch (Exception e) {
                throw new NacosRuntimeException(SERVER_ERROR, e);
            }
        };

        try {
            // 创建了默认的共享事件发布器
            INSTANCE.sharePublisher = new DefaultSharePublisher();
            INSTANCE.sharePublisher.init(SlowEvent.class, shareBufferSize);
        } catch (Throwable ex) {
            LOGGER.error("Service class newInstance has error : ", ex);
        }
        ThreadUtils.addShutdownHook(NotifyCenter::shutdown);
    }

    public static void shutdown() {
        // 更新事件通知中心状态
        if (!CLOSED.compareAndSet(false, true)) {
            return;
        }
        LOGGER.warn("[NotifyCenter] Start destroying Publisher");
        // 遍历所有的事件发布器
        for (Map.Entry<String, EventPublisher> entry : INSTANCE.publisherMap.entrySet()) {
            try {
                EventPublisher eventPublisher = entry.getValue();
                // 关闭事件发布器
                eventPublisher.shutdown();
            } catch (Throwable e) {
                LOGGER.error("[EventPublisher] shutdown has error : ", e);
            }
        }
        try {
            // 关闭共享事件发布器
            INSTANCE.sharePublisher.shutdown();
        } catch (Throwable e) {
            LOGGER.error("[SharePublisher] shutdown has error : ", e);
        }
        LOGGER.warn("[NotifyCenter] Destruction of the end");
    }

    public static boolean publishEvent(final Event event) {
        try {
            return publishEvent(event.getClass(), event);
        } catch (Throwable ex) {
            LOGGER.error("There was an exception to the message publishing : ", ex);
            return false;
        }
    }

    private static boolean publishEvent(Class<? extends Event> eventType, final Event event) {
        // 如果是慢事件就给共享发布器
        if (ClassUtils.isAssignableFrom(SlowEvent.class, eventType)) {
            return INSTANCE.sharePublisher.publish(event);
        }
        String topic = ClassUtils.getCanonicalName(eventType);
        EventPublisher publisher = INSTANCE.publisherMap.get(topic);
        if (publisher != null) {
            return publisher.publish(event);
        }
        if (event.isPluginEvent()) {
            return true;
        }
        return false;
    }

    //得到共享事件发布器的方法
    public static EventPublisher getSharePublisher() {
        return INSTANCE.sharePublisher;
    }

    public static EventPublisher getPublisher(Class<? extends Event> topic) {
        // 如果事件是慢事件类型，就返回共享事件发布器
        if (ClassUtils.isAssignableFrom(SlowEvent.class, topic)) {
            return INSTANCE.sharePublisher;
        }
        // 走到这里就意味是单一事件，就直接从publisherMap中得到对应的默认事件发布器即可
        return INSTANCE.publisherMap.get(topic.getCanonicalName());
    }

    public static void registerSubscriber(final Subscriber consumer) {
        registerSubscriber(consumer, DEFAULT_PUBLISHER_FACTORY);
    }
    public static void registerSubscriber(final Subscriber consumer, final EventPublisherFactory factory) {
        // SmartSubscriber 类型的观察者可以处理多种类型的 Event
        if (consumer instanceof SmartSubscriber) {
            SmartSubscriber smartSubscriber = (SmartSubscriber) consumer;
            for (Class<? extends Event> subscribeType : smartSubscriber.subscribeTypes()) {
                if (ClassUtils.isAssignableFrom(SlowEvent.class, subscribeType)) {
                    INSTANCE.sharePublisher.addSubscriber(consumer, subscribeType);
                } else {
                    addSubscriber(consumer, subscribeType, factory);
                }
            }
            return;
        }
        // 走到这里说明观察者是普通类型
        Class<? extends Event> subscribeType = consumer.subscribeType();
        // 如果这个观察者关注的事件是慢事件，将其加入共享事件发布器中
        if (ClassUtils.isAssignableFrom(SlowEvent.class, subscribeType)) {
            INSTANCE.sharePublisher.addSubscriber(consumer, subscribeType);
            return;
        }
        addSubscriber(consumer, subscribeType, factory);
    }

    private static void addSubscriber(final Subscriber consumer,
                                      Class<? extends Event> subscribeType,
                                      EventPublisherFactory factory) {

        // 根据订阅者订阅的事件类型获得一个topic，其实就是订阅事件的字符串
        final String topic = ClassUtils.getCanonicalName(subscribeType);
        synchronized (NotifyCenter.class) {
            // 判断publisherMap中是否存在了对应的事件发布器，如果不存在，就以topic为key事件工厂创建的事件发布器为map为value
            // 把键值对放到publisherMap中，到这里大家可以明白了，所有的订阅者其实都会存放到事件发布器中，而事件发布器又会存放到时间通知中心的publisherMap中
            // 所以时间通知中心一旦发布事件，就可以从publisherMap中获得对应的事件发布器，事件发布器再进一步执行内部存放的订阅者的回调方法即可
            MapUtil.computeIfAbsent(INSTANCE.publisherMap, topic, factory, subscribeType, ringBufferSize);
        }
        // 得到事件对应的事件发布器
        EventPublisher publisher = INSTANCE.publisherMap.get(topic);
        // 判断事件发布器是不是共享事件发布器，根据结果执行把订阅者添加到事件发布器的不同逻辑
        if (publisher instanceof ShardedEventPublisher) {
            ((ShardedEventPublisher) publisher).addSubscriber(consumer, subscribeType);
        } else {
            publisher.addSubscriber(consumer);
        }
    }

    public static void deregisterSubscriber(final Subscriber consumer) {
        if (consumer instanceof SmartSubscriber) {
            for (Class<? extends Event> subscribeType : ((SmartSubscriber) consumer).subscribeTypes()) {
                if (ClassUtils.isAssignableFrom(SlowEvent.class, subscribeType)) {
                    INSTANCE.sharePublisher.removeSubscriber(consumer, subscribeType);
                } else {
                    removeSubscriber(consumer, subscribeType);
                }
            }
            return;
        }
        final Class<? extends Event> subscribeType = consumer.subscribeType();
        if (ClassUtils.isAssignableFrom(SlowEvent.class, subscribeType)) {
            INSTANCE.sharePublisher.removeSubscriber(consumer, subscribeType);
            return;
        }
        if (removeSubscriber(consumer, subscribeType)) {
            return;
        }
        throw new NoSuchElementException("The subscriber has no event publisher");
    }
    private static boolean removeSubscriber(final Subscriber consumer, Class<? extends Event> subscribeType) {
        final String topic = ClassUtils.getCanonicalName(subscribeType);
        EventPublisher eventPublisher = INSTANCE.publisherMap.get(topic);
        if (null == eventPublisher) {
            return false;
        }
        if (eventPublisher instanceof ShardedEventPublisher) {
            ((ShardedEventPublisher) eventPublisher).removeSubscriber(consumer, subscribeType);
        } else {
            eventPublisher.removeSubscriber(consumer);
        }
        return true;
    }

    public static EventPublisher registerToSharePublisher(final Class<? extends SlowEvent> eventType) {
        return INSTANCE.sharePublisher;
    }


    public static EventPublisher registerToPublisher(final Class<? extends Event> eventType, final int queueMaxSize) {
        return registerToPublisher(eventType, DEFAULT_PUBLISHER_FACTORY, queueMaxSize);
    }


    public static EventPublisher registerToPublisher(final Class<? extends Event> eventType,
                                                     final EventPublisherFactory factory, final int queueMaxSize) {
        if (ClassUtils.isAssignableFrom(SlowEvent.class, eventType)) {
            return INSTANCE.sharePublisher;
        }
        final String topic = ClassUtils.getCanonicalName(eventType);
        synchronized (NotifyCenter.class) {
            MapUtil.computeIfAbsent(INSTANCE.publisherMap, topic, factory, eventType, queueMaxSize);
        }
        return INSTANCE.publisherMap.get(topic);
    }



    public static void registerToPublisher(final Class<? extends Event> eventType, final EventPublisher publisher) {
        if (null == publisher) {
            return;
        }
        final String topic = ClassUtils.getCanonicalName(eventType);
        synchronized (NotifyCenter.class) {
            INSTANCE.publisherMap.putIfAbsent(topic, publisher);
        }
    }


    public static void deregisterPublisher(final Class<? extends Event> eventType) {
        final String topic = ClassUtils.getCanonicalName(eventType);
        EventPublisher publisher = INSTANCE.publisherMap.remove(topic);
        try {
            publisher.shutdown();
        } catch (Throwable ex) {
            LOGGER.error("There was an exception when publisher shutdown : ", ex);
        }
    }
}
