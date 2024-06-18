package com.xiaohe.nacos.common.remote.exception;

import com.xiaohe.nacos.api.exception.runtime.NacosRuntimeException;

public class RemoteException extends NacosRuntimeException {

    public RemoteException(int errorCode) {
        super(errorCode);
    }

    public RemoteException(int errorCode, String msg) {
        super(errorCode, msg);
    }

    public RemoteException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
