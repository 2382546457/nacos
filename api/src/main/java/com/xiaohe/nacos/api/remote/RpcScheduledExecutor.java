package com.xiaohe.nacos.api.remote;


import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcScheduledExecutor extends ScheduledThreadPoolExecutor {

    public static final RpcScheduledExecutor TIMEOUT_SCHEDULER = new RpcScheduledExecutor(
            1,
            "com.xiaohe.nacos.remote.TimerScheduler"
    );

    public static final RpcScheduledExecutor CONTROL_SCHEDULER = new RpcScheduledExecutor(
            1,
            "com.xiaohe.nacos.control.DelayScheduler"
    );

    public static final RpcScheduledExecutor COMMON_SERVER_EXECUTOR = new RpcScheduledExecutor(
            1,
            "com.xiaohe.nacos.remote.ServerCommonScheduler"
    );

    public RpcScheduledExecutor(int corePoolSize, final String threadName) {
        super(corePoolSize, new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, threadName + "." + index.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        });
    }
}
