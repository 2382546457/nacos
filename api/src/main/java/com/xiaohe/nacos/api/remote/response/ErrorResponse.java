package com.xiaohe.nacos.api.remote.response;

import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.exception.runtime.NacosRuntimeException;

public class ErrorResponse extends Response {
    public static Response build(int errorCode, String msg) {
        ErrorResponse response = new ErrorResponse();
        response.setErrorInfo(errorCode, msg);
        return response;
    }

    public static Response build(Throwable exception) {
        int errorCode;
        if (exception instanceof NacosException) {
            errorCode = ((NacosException) exception).getErrCode();
        } else if (exception instanceof NacosRuntimeException) {
            errorCode = ((NacosRuntimeException) exception).getErrCode();
        } else {
            errorCode = ResponseCode.FAIL.getCode();
        }
        ErrorResponse response = new ErrorResponse();
        response.setErrorInfo(errorCode, exception.getMessage());
        return response;
    }
}
