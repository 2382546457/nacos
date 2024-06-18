package com.xiaohe.nacos.common.task;

public abstract class AbstractExecuteTask implements NacosTask, Runnable {

    protected static final long INTERVAL = 3000L;

    @Override
    public boolean shouldProcess() {
        return true;
    }
}
