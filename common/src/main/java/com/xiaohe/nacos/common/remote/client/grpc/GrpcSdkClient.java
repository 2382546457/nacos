package com.xiaohe.nacos.common.remote.client.grpc;

import com.xiaohe.nacos.api.ability.constant.AbilityMode;
import com.xiaohe.nacos.api.common.Constants;
import com.xiaohe.nacos.common.remote.client.RpcClientTlsConfig;

import java.util.Map;
import java.util.Properties;

public class GrpcSdkClient extends GrpcClient {

    /**
     * Constructor.
     *
     * @param name name of client.
     */
    public GrpcSdkClient(String name) {
        super(name);
    }

    /**
     * Constructor.
     *
     * @param properties .
     */
    public GrpcSdkClient(Properties properties) {
        super(properties);
    }

    /**
     * Constructor.
     *
     * @param name               name of client.
     * @param threadPoolCoreSize .
     * @param threadPoolMaxSize  .
     * @param labels             .
     */
    public GrpcSdkClient(String name, Integer threadPoolCoreSize, Integer threadPoolMaxSize, Map<String, String> labels) {
        this(name, threadPoolCoreSize, threadPoolMaxSize, labels, null);
    }

    public GrpcSdkClient(String name, Integer threadPoolCoreSize, Integer threadPoolMaxSize, Map<String, String> labels,
                         RpcClientTlsConfig tlsConfig) {
        super(name, threadPoolCoreSize, threadPoolMaxSize, labels, tlsConfig);
    }

    @Override
    protected AbilityMode abilityMode() {
        return AbilityMode.SDK_CLIENT;
    }

    /**
     * constructor.
     *
     * @param config of GrpcClientConfig.
     */
    public GrpcSdkClient(GrpcClientConfig config) {
        super(config);
    }



    /**
     * 这个方法就是用来得到grpc服务端端口号偏移量的方法
     */
    @Override
    public int rpcPortOffset() {
        return Integer.parseInt(System.getProperty(GrpcConstants.NACOS_SERVER_GRPC_PORT_OFFSET_KEY,
                String.valueOf(Constants.SDK_GRPC_PORT_DEFAULT_OFFSET)));
    }

}