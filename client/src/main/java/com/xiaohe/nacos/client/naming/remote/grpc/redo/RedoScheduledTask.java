package com.xiaohe.nacos.client.naming.remote.grpc.redo;

import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.client.naming.remote.grpc.NamingGrpcClientProxy;
import com.xiaohe.nacos.client.naming.remote.grpc.redo.data.BatchInstanceRedoData;
import com.xiaohe.nacos.client.naming.remote.grpc.redo.data.InstanceRedoData;
import com.xiaohe.nacos.client.naming.remote.grpc.redo.data.RedoData;
import com.xiaohe.nacos.client.naming.remote.grpc.redo.data.SubscriberRedoData;
import com.xiaohe.nacos.common.task.AbstractExecuteTask;

public class RedoScheduledTask extends AbstractExecuteTask {
    private final NamingGrpcClientProxy clientProxy;
    private final NamingGrpcRedoService redoService;

    public RedoScheduledTask(NamingGrpcClientProxy clientProxy, NamingGrpcRedoService redoService) {
        this.clientProxy = clientProxy;
        this.redoService = redoService;
    }

    private boolean isClientDisabled() {
        return !clientProxy.isEnable();
    }

    @Override
    public void run() {
        // 先判断连接是否健康
        if (redoService.isConnected()) {
            return;
        }
        try {
            redoForInstances();
            redoForSubscribes();
        } catch (Exception e) {

        }
    }

    /**
     * 和服务实例相关的重做操作
     */
    private void redoForInstances() {
        for (InstanceRedoData each : redoService.findInstanceRedoData()) {
            try {
                redoForInstance(each);
            } catch (NacosException e) {

            }
        }
    }
    private void redoForInstance(InstanceRedoData redoData) throws NacosException {
        if (isClientDisabled()) {
            return;
        }
        RedoData.RedoType redoType = redoData.getRedoType();
        String serviceName = redoData.getServiceName();
        String groupName = redoData.getGroupName();
        switch (redoType) {
            case REGISTER:
                // 注册重做
                processRegisterRedoType(redoData, serviceName, groupName);
                break;
            case UNREGISTER:
                // 取消注册重做
                clientProxy.doDeregisterService(serviceName, groupName, redoData.getData());
                break;
            case REMOVE:
                // 移除重做操作
                redoService.removeInstanceForRedo(serviceName, groupName);
                break;
            default:
        }
    }
    private void processRegisterRedoType(InstanceRedoData redoData, String serviceName, String groupName) throws NacosException {
        if (redoData instanceof BatchInstanceRedoData) {
            BatchInstanceRedoData data = (BatchInstanceRedoData) redoData;
            // TODO 批量实例重做
            return;
        }
        clientProxy.doRegisterService(serviceName, groupName, redoData.getData());
    }

    private void redoForSubscribes() {
        for (SubscriberRedoData each : redoService.findSubscriberRedoData()) {
            try {
                redoForSubscribe(each);
            } catch (NacosException e) {

            }
        }
    }

    private void redoForSubscribe(SubscriberRedoData redoData) throws NacosException {
        if (isClientDisabled()) {
            return;
        }
        RedoData.RedoType redoType = redoData.getRedoType();
        String serviceName = redoData.getServiceName();
        String groupName = redoData.getGroupName();
        String cluster = redoData.getData();

        switch (redoType) {
            case REGISTER:
                clientProxy.doSubscribe(serviceName, groupName, cluster);
                break;
            case UNREGISTER:
                clientProxy.doUnsubscribe(serviceName, groupName, cluster);
                break;
            case REMOVE:
                redoService.removeSubscriberForRedo(serviceName, groupName, cluster);
                break;
            default:
        }
    }


}
