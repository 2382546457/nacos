package com.xiaohe.nacos.common.remote.client.grpc;

import com.xiaohe.nacos.common.remote.client.RpcClientTlsConfig;
import com.xiaohe.nacos.common.utils.ThreadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DefaultGrpcClientConfig implements GrpcClientConfig {

    // 客户端名称，实际上就是之前在NamingGrpcClientProxy类中创建的uuid
    private String name;
    // rpc请求发送的重试次数
    private int retryTimes;
    // 请求超时时间
    private long timeOutMills;
    // 连接存活时间
    private long connectionKeepAlive;
    // channel存活时间
    private long channelKeepAliveTimeout;
    // 线程池中的线程存活时间，这里大家可以看到，这几个存活时间会在这些连接或线程空闲的时候发挥作用
    private long threadPoolKeepAlive;
    // 线程池核心线程数量
    private int threadPoolCoreSize;
    // 最大线程数量
    private int threadPoolMaxSize;
    // 服务检查超时时间
    private long serverCheckTimeOut;
    // 线程池队列容量大小
    private int threadPoolQueueSize;
    // 最大的入站消息字节大小
    private int maxInboundMessageSize;
    // channel保持连接的时间
    private int channelKeepAlive;
    // 健康检查重试次数
    private int healthCheckRetryTimes;
    // 健康检查超时时间
    private long healthCheckTimeOut;
    // 这个成员变量会在grpc客户端连接grpc服务端的时候用到
    private long capabilityNegotiationTimeout;
    // 客户端的附加信息
    private Map<String, String> labels;
    // tls配置对象
    private RpcClientTlsConfig tlsConfig = new RpcClientTlsConfig();

    /**
     * constructor.
     *
     * @param builder builder of DefaultGrpcClientConfig builder.
     */
    private DefaultGrpcClientConfig(Builder builder) {
        this.name = builder.name;
        this.retryTimes = loadIntegerConfig(GrpcConstants.GRPC_RETRY_TIMES, builder.retryTimes);
        this.timeOutMills = loadLongConfig(GrpcConstants.GRPC_TIMEOUT_MILLS, builder.timeOutMills);
        this.connectionKeepAlive = loadLongConfig(GrpcConstants.GRPC_CONNECT_KEEP_ALIVE_TIME,
                builder.connectionKeepAlive);
        this.threadPoolKeepAlive = loadLongConfig(GrpcConstants.GRPC_THREADPOOL_KEEPALIVETIME,
                builder.threadPoolKeepAlive);
        this.threadPoolCoreSize = loadIntegerConfig(GrpcConstants.GRPC_THREADPOOL_CORE_SIZE,
                builder.threadPoolCoreSize);
        this.threadPoolMaxSize = loadIntegerConfig(GrpcConstants.GRPC_THREADPOOL_MAX_SIZE, builder.threadPoolMaxSize);
        this.serverCheckTimeOut = loadLongConfig(GrpcConstants.GRPC_SERVER_CHECK_TIMEOUT, builder.serverCheckTimeOut);
        this.threadPoolQueueSize = loadIntegerConfig(GrpcConstants.GRPC_QUEUESIZE, builder.threadPoolQueueSize);
        this.maxInboundMessageSize = loadIntegerConfig(GrpcConstants.GRPC_MAX_INBOUND_MESSAGE_SIZE,
                builder.maxInboundMessageSize);
        this.channelKeepAlive = loadIntegerConfig(GrpcConstants.GRPC_CHANNEL_KEEP_ALIVE_TIME, builder.channelKeepAlive);
        this.healthCheckRetryTimes = loadIntegerConfig(GrpcConstants.GRPC_HEALTHCHECK_RETRY_TIMES,
                builder.healthCheckRetryTimes);
        this.healthCheckTimeOut = loadLongConfig(GrpcConstants.GRPC_HEALTHCHECK_TIMEOUT, builder.healthCheckTimeOut);
        this.channelKeepAliveTimeout = loadLongConfig(GrpcConstants.GRPC_CHANNEL_KEEP_ALIVE_TIMEOUT,
                builder.channelKeepAliveTimeout);
        this.capabilityNegotiationTimeout = loadLongConfig(GrpcConstants.GRPC_CHANNEL_CAPABILITY_NEGOTIATION_TIMEOUT,
                builder.capabilityNegotiationTimeout);
        this.labels = builder.labels;
        this.labels.put("tls.enable", "false");
        if (Objects.nonNull(builder.tlsConfig)) {
            this.tlsConfig = builder.tlsConfig;
            if (builder.tlsConfig.getEnableTls()) {
                this.labels.put("tls.enable", "true");
            }
        }
    }

    private int loadIntegerConfig(String key, int builderValue) {
        return Integer.getInteger(key, builderValue);
    }

    private long loadLongConfig(String key, long builderValue) {
        return Long.getLong(key, builderValue);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public int retryTimes() {
        return retryTimes;
    }

    @Override
    public long timeOutMills() {
        return timeOutMills;
    }

    @Override
    public long connectionKeepAlive() {
        return connectionKeepAlive;
    }

    @Override
    public int threadPoolCoreSize() {
        return threadPoolCoreSize;
    }

    @Override
    public int threadPoolMaxSize() {
        return threadPoolMaxSize;
    }

    @Override
    public long threadPoolKeepAlive() {
        return threadPoolKeepAlive;
    }

    @Override
    public long serverCheckTimeOut() {
        return serverCheckTimeOut;
    }

    @Override
    public int threadPoolQueueSize() {
        return threadPoolQueueSize;
    }

    @Override
    public int maxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    @Override
    public int channelKeepAlive() {
        return channelKeepAlive;
    }

    @Override
    public long channelKeepAliveTimeout() {
        return channelKeepAliveTimeout;
    }

    @Override
    public RpcClientTlsConfig tlsConfig() {
        return tlsConfig;
    }

    public void setTlsConfig(RpcClientTlsConfig tlsConfig) {
        this.tlsConfig = tlsConfig;
    }

    @Override
    public long capabilityNegotiationTimeout() {
        return this.capabilityNegotiationTimeout;
    }

    @Override
    public int healthCheckRetryTimes() {
        return healthCheckRetryTimes;
    }

    @Override
    public long healthCheckTimeOut() {
        return healthCheckTimeOut;
    }

    @Override
    public Map<String, String> labels() {
        return this.labels;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private int retryTimes = 3;

        //超时时间默认为3秒
        private long timeOutMills = 3000L;

        private long connectionKeepAlive = 5000L;

        private long threadPoolKeepAlive = 10000L;
        //在这里可以看到，线程的默认配置信息都已经定义好了
        private int threadPoolCoreSize = ThreadUtils.getSuitableThreadCount(2);

        private int threadPoolMaxSize = ThreadUtils.getSuitableThreadCount(8);

        private long serverCheckTimeOut = 3000L;

        private int threadPoolQueueSize = 10000;

        private int maxInboundMessageSize = 10 * 1024 * 1024;

        private int channelKeepAlive = 6 * 60 * 1000;

        private long channelKeepAliveTimeout = TimeUnit.SECONDS.toMillis(20L);

        private int healthCheckRetryTimes = 3;

        private long healthCheckTimeOut = 3000L;

        private long capabilityNegotiationTimeout = 5000L;

        private Map<String, String> labels = new HashMap<>();

        private RpcClientTlsConfig tlsConfig = new RpcClientTlsConfig();

        private Builder() {
        }

        /**
         * Set config from properties.
         *
         * @param properties properties
         * @return Builder
         */
        public Builder fromProperties(Properties properties) {
            if (properties.containsKey(GrpcConstants.GRPC_NAME)) {
                this.name = properties.getProperty(GrpcConstants.GRPC_NAME);
            }
            if (properties.containsKey(GrpcConstants.GRPC_RETRY_TIMES)) {
                this.retryTimes = Integer.parseInt(properties.getProperty(GrpcConstants.GRPC_RETRY_TIMES));
            }
            if (properties.containsKey(GrpcConstants.GRPC_TIMEOUT_MILLS)) {
                this.timeOutMills = Long.parseLong(properties.getProperty(GrpcConstants.GRPC_TIMEOUT_MILLS));
            }
            if (properties.containsKey(GrpcConstants.GRPC_CONNECT_KEEP_ALIVE_TIME)) {
                this.connectionKeepAlive = Long
                        .parseLong(properties.getProperty(GrpcConstants.GRPC_CONNECT_KEEP_ALIVE_TIME));
            }
            if (properties.containsKey(GrpcConstants.GRPC_THREADPOOL_KEEPALIVETIME)) {
                this.threadPoolKeepAlive = Long
                        .parseLong(properties.getProperty(GrpcConstants.GRPC_THREADPOOL_KEEPALIVETIME));
            }
            if (properties.containsKey(GrpcConstants.GRPC_THREADPOOL_CORE_SIZE)) {
                this.threadPoolCoreSize = Integer
                        .parseInt(properties.getProperty(GrpcConstants.GRPC_THREADPOOL_CORE_SIZE));
            }
            if (properties.containsKey(GrpcConstants.GRPC_THREADPOOL_MAX_SIZE)) {
                this.threadPoolMaxSize = Integer
                        .parseInt(properties.getProperty(GrpcConstants.GRPC_THREADPOOL_MAX_SIZE));
            }
            if (properties.containsKey(GrpcConstants.GRPC_SERVER_CHECK_TIMEOUT)) {
                this.serverCheckTimeOut = Long
                        .parseLong(properties.getProperty(GrpcConstants.GRPC_SERVER_CHECK_TIMEOUT));
            }
            if (properties.containsKey(GrpcConstants.GRPC_QUEUESIZE)) {
                this.threadPoolQueueSize = Integer.parseInt(properties.getProperty(GrpcConstants.GRPC_QUEUESIZE));
            }
            if (properties.containsKey(GrpcConstants.GRPC_MAX_INBOUND_MESSAGE_SIZE)) {
                this.maxInboundMessageSize = Integer
                        .parseInt(properties.getProperty(GrpcConstants.GRPC_MAX_INBOUND_MESSAGE_SIZE));
            }
            if (properties.containsKey(GrpcConstants.GRPC_CHANNEL_KEEP_ALIVE_TIME)) {
                this.channelKeepAlive = Integer
                        .parseInt(properties.getProperty(GrpcConstants.GRPC_CHANNEL_KEEP_ALIVE_TIME));
            }
            if (properties.containsKey(GrpcConstants.GRPC_CHANNEL_CAPABILITY_NEGOTIATION_TIMEOUT)) {
                this.capabilityNegotiationTimeout = Integer
                        .parseInt(properties.getProperty(GrpcConstants.GRPC_CHANNEL_CAPABILITY_NEGOTIATION_TIMEOUT));
            }
            if (properties.containsKey(GrpcConstants.GRPC_HEALTHCHECK_RETRY_TIMES)) {
                this.healthCheckRetryTimes = Integer
                        .parseInt(properties.getProperty(GrpcConstants.GRPC_HEALTHCHECK_RETRY_TIMES));
            }
            if (properties.containsKey(GrpcConstants.GRPC_HEALTHCHECK_TIMEOUT)) {
                this.healthCheckTimeOut = Long
                        .parseLong(properties.getProperty(GrpcConstants.GRPC_HEALTHCHECK_TIMEOUT));
            }
            if (properties.containsKey(GrpcConstants.GRPC_CHANNEL_KEEP_ALIVE_TIMEOUT)) {
                this.channelKeepAliveTimeout = Integer
                        .parseInt(properties.getProperty(GrpcConstants.GRPC_CHANNEL_KEEP_ALIVE_TIMEOUT));
            }
            this.tlsConfig = RpcClientTlsConfig.properties(properties);
            return this;
        }

        /**
         * set client name.
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * set retryTimes.
         */
        public Builder setRetryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        /**
         * set timeOutMills.
         */
        public Builder setTimeOutMills(long timeOutMills) {
            this.timeOutMills = timeOutMills;
            return this;
        }

        /**
         * set connectionKeepAlive.
         */
        public Builder setConnectionKeepAlive(long connectionKeepAlive) {
            this.connectionKeepAlive = connectionKeepAlive;
            return this;
        }

        /**
         * set threadPoolKeepAlive.
         */
        public Builder setThreadPoolKeepAlive(Long threadPoolKeepAlive) {
            this.threadPoolKeepAlive = threadPoolKeepAlive;
            return this;
        }

        /**
         * set threadPoolCoreSize.
         */
        public Builder setThreadPoolCoreSize(Integer threadPoolCoreSize) {
            if (!Objects.isNull(threadPoolCoreSize)) {
                this.threadPoolCoreSize = threadPoolCoreSize;
            }
            return this;
        }

        /**
         * set threadPoolMaxSize.
         */
        public Builder setThreadPoolMaxSize(Integer threadPoolMaxSize) {
            if (!Objects.isNull(threadPoolMaxSize)) {
                this.threadPoolMaxSize = threadPoolMaxSize;
            }
            return this;
        }

        /**
         * set serverCheckTimeOut.
         */
        public Builder setServerCheckTimeOut(Long serverCheckTimeOut) {
            this.serverCheckTimeOut = serverCheckTimeOut;
            return this;
        }

        /**
         * set threadPoolQueueSize.
         */
        public Builder setThreadPoolQueueSize(int threadPoolQueueSize) {
            this.threadPoolQueueSize = threadPoolQueueSize;
            return this;
        }

        /**
         * set maxInboundMessageSize.
         */
        public Builder setMaxInboundMessageSize(int maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
            return this;
        }

        /**
         * set channelKeepAlive.
         */
        public Builder setChannelKeepAlive(int channelKeepAlive) {
            this.channelKeepAlive = channelKeepAlive;
            return this;
        }

        /**
         * set channelKeepAlive.
         *
         * @param channelKeepAliveTimeout milliseconds
         * @return builder
         */
        public Builder setChannelKeepAliveTimeout(int channelKeepAliveTimeout) {
            this.channelKeepAliveTimeout = channelKeepAliveTimeout;
            return this;
        }

        public Builder setCapabilityNegotiationTimeout(long capabilityNegotiationTimeout) {
            this.capabilityNegotiationTimeout = capabilityNegotiationTimeout;
            return this;
        }

        /**
         * set healthCheckRetryTimes.
         */
        public Builder setHealthCheckRetryTimes(int healthCheckRetryTimes) {
            this.healthCheckRetryTimes = healthCheckRetryTimes;
            return this;
        }

        /**
         * set healthCheckTimeOut.
         */
        public Builder setHealthCheckTimeOut(long healthCheckTimeOut) {
            this.healthCheckTimeOut = healthCheckTimeOut;
            return this;
        }

        /**
         * set labels.
         */
        public Builder setLabels(Map<String, String> labels) {
            this.labels.putAll(labels);
            return this;
        }

        /**
         * set tlsConfig.
         *
         * @param tlsConfig tls of client.
         * @return
         */
        public Builder setTlsConfig(RpcClientTlsConfig tlsConfig) {
            this.tlsConfig = tlsConfig;
            return this;
        }

        /**
         * build GrpcClientConfig.
         */
        public GrpcClientConfig build() {
            return new DefaultGrpcClientConfig(this);
        }
    }

}

