package com.xiaohe.nacos.common.remote.client;

import com.xiaohe.nacos.common.remote.TlsConfig;

import java.util.Properties;


/**
 * 从 Properties 中获取配置，然后设置到 TlsConfig 中
 */
public class RpcClientTlsConfig extends TlsConfig {


    /**
     *  get tls config from properties.
     */
    public static RpcClientTlsConfig properties(Properties properties) {
        RpcClientTlsConfig tlsConfig = new RpcClientTlsConfig();
        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_ENABLE)) {
            tlsConfig.setEnableTls(Boolean.parseBoolean(
                    properties.getProperty(RpcConstants.RPC_CLIENT_TLS_ENABLE)));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_PROVIDER)) {
            tlsConfig.setSslProvider(properties.getProperty(RpcConstants.RPC_CLIENT_TLS_PROVIDER));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_MUTUAL_AUTH)) {
            tlsConfig.setMutualAuthEnable(Boolean.parseBoolean(
                    properties.getProperty(RpcConstants.RPC_CLIENT_MUTUAL_AUTH)));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_PROTOCOLS)) {
            tlsConfig.setProtocols(properties.getProperty(RpcConstants.RPC_CLIENT_TLS_PROTOCOLS));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_CIPHERS)) {
            tlsConfig.setCiphers(properties.getProperty(RpcConstants.RPC_CLIENT_TLS_CIPHERS));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_TRUST_COLLECTION_CHAIN_PATH)) {
            tlsConfig.setTrustCollectionCertFile(properties.getProperty(RpcConstants.RPC_CLIENT_TLS_TRUST_COLLECTION_CHAIN_PATH));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_CERT_CHAIN_PATH)) {
            tlsConfig.setCertChainFile(properties.getProperty(RpcConstants.RPC_CLIENT_TLS_CERT_CHAIN_PATH));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_CERT_KEY)) {
            tlsConfig.setCertPrivateKey(properties.getProperty(RpcConstants.RPC_CLIENT_TLS_CERT_KEY));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_TRUST_ALL)) {
            tlsConfig.setTrustAll(Boolean.parseBoolean(properties.getProperty(RpcConstants.RPC_CLIENT_TLS_TRUST_ALL)));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_TRUST_PWD)) {
            tlsConfig.setCertPrivateKeyPassword(properties.getProperty(RpcConstants.RPC_CLIENT_TLS_TRUST_PWD));
        }

        if (properties.containsKey(RpcConstants.RPC_CLIENT_TLS_PROVIDER)) {
            tlsConfig.setSslProvider(properties.getProperty(RpcConstants.RPC_CLIENT_TLS_PROVIDER));
        }
        return tlsConfig;
    }

}