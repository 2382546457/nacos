package com.xiaohe.nacos.api.remote.request;

import com.xiaohe.nacos.api.common.Constants;

public class ClientDetectionRequest extends ServerRequest {
    @Override
    public String getModule() {
        return Constants.Remote.INTERNAL_MODULE;
    }
}
