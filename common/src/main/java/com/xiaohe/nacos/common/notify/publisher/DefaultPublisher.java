package com.xiaohe.nacos.common.notify.publisher;

import com.xiaohe.nacos.common.notify.event.Event;
import com.xiaohe.nacos.common.notify.listener.Subscriber;
import com.xiaohe.nacos.common.utils.CollectionUtils;
import com.xiaohe.nacos.common.utils.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.xiaohe.nacos.common.notify.NotifyCenter.ringBufferSize;

public class DefaultPublisher extends Thread implements EventPublisher {
    protected static final Logger logger = LoggerFactory.getLogger(DefaultPublisher.class);

    /**
     * 事件发布器是一个线程，只能启动一次
     */
    private volatile boolean initialized;

    private volatile boolean shutdown;

    /**
     * 当前事件发布器关注的事件
     */
    private Class<? extends Event> eventType;

    /**
     * 订阅了该事件的观察者
     */
    protected final ConcurrentHashSet<Subscriber> subscribers = new ConcurrentHashSet<>();

    private int queueMaxSize = -1;

    /**
     * 存放事件的队列
     */
    private BlockingQueue<Event> queue;

    /**
     * 被处理的最新的事件序号
     */
    protected volatile Long lastEventSequence = -1L;

    private static final AtomicReferenceFieldUpdater<DefaultPublisher, Long> UPDATER = AtomicReferenceFieldUpdater.newUpdater(DefaultPublisher.class, Long.class, "lastEventSequence");


    @Override
    public void init(Class<? extends Event> type, int bufferSize) {
        // 设置为守护进程
        setDaemon(true);
        setName("nacos.publisher" + type.getName());
        this.eventType = type;
        this.queueMaxSize = bufferSize;
        if (this.queueMaxSize == -1) {
            this.queueMaxSize = ringBufferSize;
        }
        this.queue = new ArrayBlockingQueue<>(this.queueMaxSize);
        // 启动线程
        start();
    }

    @Override
    public synchronized void start() {
        if (!initialized) {
            super.start();
            initialized = true;
        }
    }
    void checkIsStart() {
        if (!initialized) {
            throw new IllegalStateException("Publisher does not start");
        }
    }


    @Override
    public void run() {
        // 处理事件队列中的事件
        openEventHandler();
    }

    private void openEventHandler() {
        try {
            int waitTimes = 60;
            // 如果迟迟没有观察者，最多等待60s就丢弃事件. 为了避免观察者注册的太慢导致事件丢失
            while (!shutdown && !hasSubscriber() && waitTimes > 0) {
                Thread.sleep(1000L);
                waitTimes--;
            }
            while (!shutdown) {
                Event event = queue.take();
                receiveEvent(event);
                // 更新被处理过的最新事件的序号，代表之前的序号过期了
                UPDATER.compareAndSet(this, lastEventSequence, Math.max(lastEventSequence, event.sequence()));
            }
        } catch (Throwable e) {
            logger.error("Event listener exception : ", e);
        }
    }

    /**
     * 当前事件个数
     * @return
     */
    @Override
    public long currentEventSize() {
        return queue.size();
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public boolean publish(Event event) {
        checkIsStart();
        boolean success = this.queue.offer(event);
        // 添加失败说明现在队列满了，也就是事件线程处理不过来了，那么当前线程帮忙处理一下
        if (!success) {
            receiveEvent(event);
            return true;
        }
        return true;
    }

    /**
     * 处理事件 : 遍历观察者，执行 onEvent 方法
     * @param event
     */
    void receiveEvent(Event event) {
        long currentEventSequence = event.sequence();
        // 这个事件没有观察者就算了, 直接丢弃事件
        if (!hasSubscriber()) {
            return;
        }
        for (Subscriber subscriber : subscribers) {
            // 观察者对事件也是有要求的
            if (!subscriber.scopeMatches(event)) {
                continue;
            }
            if (subscriber.ignoreExpireEvent() && lastEventSequence > currentEventSequence) {
                continue;
            }
            notifySubscriber(subscriber, event);
        }

    }

    @Override
    public void notifySubscriber(Subscriber subscriber, Event event) {
        Runnable job = () -> subscriber.onEvent(event);
        Executor executor = subscriber.executor();
        if (executor != null) {
            executor.execute(job);
        } else {
            try {
                job.run();
            } catch (Throwable e) {
                logger.error("Event callback exception: ", e);
            }
        }
    }

    @Override
    public void shutdown() {
        this.shutdown = true;
        this.queue.clear();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public Class<? extends Event> getEventType() {
        return eventType;
    }

    public void setEventType(Class<? extends Event> eventType) {
        this.eventType = eventType;
    }

    public ConcurrentHashSet<Subscriber> getSubscribers() {
        return subscribers;
    }

    public int getQueueMaxSize() {
        return queueMaxSize;
    }

    public void setQueueMaxSize(int queueMaxSize) {
        this.queueMaxSize = queueMaxSize;
    }

    public BlockingQueue<Event> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<Event> queue) {
        this.queue = queue;
    }

    public Long getLastEventSequence() {
        return lastEventSequence;
    }

    public void setLastEventSequence(Long lastEventSequence) {
        this.lastEventSequence = lastEventSequence;
    }
    private boolean hasSubscriber() {
        return CollectionUtils.isNotEmpty(subscribers);
    }
}

