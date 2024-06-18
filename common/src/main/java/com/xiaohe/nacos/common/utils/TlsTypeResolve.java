package com.xiaohe.nacos.common.utils;

import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;

/**
 * gRPC config for sdk.
 *
 * @author githubcheng2978
 */
public class TlsTypeResolve {

    /**
     * JDK SSL is very slower than OPENSSL, recommend use openSSl.
     *
     * @param provider name of ssl provider.
     * @return SslProvider.
     */
    public static SslProvider getSslProvider(String provider) {
        if (SslProvider.OPENSSL.name().equalsIgnoreCase(provider)) {
            return SslProvider.OPENSSL;
        }
        if (SslProvider.JDK.name().equalsIgnoreCase(provider)) {
            return SslProvider.JDK;
        }
        if (SslProvider.OPENSSL_REFCNT.name().equalsIgnoreCase(provider)) {
            return SslProvider.OPENSSL_REFCNT;
        }
        return SslProvider.OPENSSL;
    }
}
