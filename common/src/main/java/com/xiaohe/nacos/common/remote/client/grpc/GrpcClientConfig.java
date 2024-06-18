package com.xiaohe.nacos.common.remote.client.grpc;

import com.xiaohe.nacos.common.remote.client.RpcClientConfig;
import com.xiaohe.nacos.common.remote.client.RpcClientTlsConfig;

public interface GrpcClientConfig extends RpcClientConfig {

    /**
     * get threadPoolCoreSize.
     *
     * @return threadPoolCoreSize.
     */
    int threadPoolCoreSize();

    /**
     * get threadPoolMaxSize.
     *
     * @return threadPoolMaxSize.
     */
    int threadPoolMaxSize();

    /**
     * get thread pool keep alive time.
     *
     * @return threadPoolKeepAlive.
     */
    long threadPoolKeepAlive();

    /**
     * get server check time out.
     *
     * @return serverCheckTimeOut.
     */
    long serverCheckTimeOut();

    /**
     * get thread pool queue size.
     *
     * @return threadPoolQueueSize.
     */
    int threadPoolQueueSize();

    /**
     * get maxInboundMessage size.
     *
     * @return maxInboundMessageSize.
     */
    int maxInboundMessageSize();

    /**
     * get channelKeepAlive time.
     *
     * @return channelKeepAlive.
     */
    int channelKeepAlive();

    /**
     * get channelKeepAliveTimeout.
     *
     * @return channelKeepAliveTimeout.
     */
    long channelKeepAliveTimeout();

    /**
     *  getTlsConfig.
     *
     * @return TlsConfig.
     */
    RpcClientTlsConfig tlsConfig();

    /**
     *Set TlsConfig.
     *
     * @param tlsConfig tlsConfig of client.
     */
    void setTlsConfig(RpcClientTlsConfig tlsConfig);

    /**
     * get timeout of connection setup(TimeUnit.MILLISECONDS).
     *
     * @return timeout of connection setup
     */
    long capabilityNegotiationTimeout();

}