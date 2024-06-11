package com.xiaohe.nacos.api.remote.request;

import com.xiaohe.nacos.api.common.Constants;

public abstract class InternalRequest extends Request {
    @Override
    public String getModule() {
        return Constants.Remote.INTERNAL_MODULE;
    }
}
