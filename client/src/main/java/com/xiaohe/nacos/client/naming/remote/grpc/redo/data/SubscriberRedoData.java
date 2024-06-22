package com.xiaohe.nacos.client.naming.remote.grpc.redo.data;

public class SubscriberRedoData extends RedoData<String> {

    private SubscriberRedoData(String serviceName, String groupName) {
        super(serviceName, groupName);
    }


    public static SubscriberRedoData build(String serviceName, String groupName, String clusters) {
        SubscriberRedoData result = new SubscriberRedoData(serviceName, groupName);
        result.set(clusters);
        return result;
    }
}