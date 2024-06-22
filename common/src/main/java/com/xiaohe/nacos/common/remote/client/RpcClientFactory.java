package com.xiaohe.nacos.common.remote.client;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.xiaohe.nacos.common.remote.ConnectionType;
import com.xiaohe.nacos.common.remote.client.grpc.GrpcSdkClient;

public class RpcClientFactory {
    private static final Map<String, RpcClient> CLIENT_MAP = new ConcurrentHashMap<>();

    public static Set<Map.Entry<String,RpcClient>> getAllClientEntries() {
        return CLIENT_MAP.entrySet();
    }

    public static RpcClient getClient(String clientName) {
        return CLIENT_MAP.get(clientName);
    }

    public static RpcClient createClient(String clientName, ConnectionType connectionType, Map<String, String> labels) {
        return createClient(clientName, connectionType, null, null, labels);
    }


    /**
     * 创建rpc客户端的时候，调用的就是这个方法
     */
    public static RpcClient createClient(String clientName, ConnectionType connectionType, Map<String, String> labels,
                                         RpcClientTlsConfig tlsConfig) {
        // 虽然这里创建grpc客户端的时候，线程的信息都为null，但实际上在客户端的配置类中，已经定义了线程的默认配置信息
        return createClient(clientName, connectionType, null, null, labels, tlsConfig);

    }

    public static RpcClient createClient(String clientName, ConnectionType connectionType, Integer threadPoolCoreSize,
                                         Integer threadPoolMaxSize, Map<String, String> labels) {
        return createClient(clientName, connectionType, threadPoolCoreSize, threadPoolMaxSize, labels, null);
    }

    /**
     * 在这个方法中，创建了一个grpc客户端
     */
    public static RpcClient createClient(String clientName, ConnectionType connectionType, Integer threadPoolCoreSize,
                                         Integer threadPoolMaxSize, Map<String, String> labels, RpcClientTlsConfig tlsConfig) {

        if (!ConnectionType.GRPC.equals(connectionType)) {
            throw new UnsupportedOperationException("unsupported connection type :" + connectionType.getType());
        }
        // 这里还判断了客户端的名称是否唯一，客户端名称实际上就是之前创建的uuid
        return CLIENT_MAP.computeIfAbsent(clientName, clientNameInner -> {
            // 在这里创建了grpc客户端
            return new GrpcSdkClient(clientNameInner, threadPoolCoreSize, threadPoolMaxSize, labels, tlsConfig);
        });
    }

}
