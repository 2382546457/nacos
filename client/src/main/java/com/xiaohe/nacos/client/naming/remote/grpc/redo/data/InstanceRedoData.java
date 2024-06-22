package com.xiaohe.nacos.client.naming.remote.grpc.redo.data;

import com.xiaohe.nacos.api.naming.pojo.Instance;

public class InstanceRedoData extends RedoData<Instance> {

    protected InstanceRedoData(String serviceName, String groupName) {
        super(serviceName, groupName);
    }

    public static InstanceRedoData build(String serviceName, String groupName, Instance instance) {
        InstanceRedoData result = new InstanceRedoData(serviceName, groupName);
        result.set(instance);
        return result;
    }

}
