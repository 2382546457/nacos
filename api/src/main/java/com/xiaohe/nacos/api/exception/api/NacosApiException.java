package com.xiaohe.nacos.api.exception.api;


import com.xiaohe.nacos.api.common.Constants;
import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.model.v2.ErrorCode;
import com.xiaohe.nacos.api.utils.StringUtils;

public class NacosApiException extends NacosException {

    private static final long serialVersionUID = 2245627968556056573L;


    private int detailErrCode;


    private String errAbstract;

    public NacosApiException() {
    }

    public NacosApiException(int statusCode, ErrorCode errorCode, Throwable throwable, String message) {
        super(statusCode, message, throwable);
        this.detailErrCode = errorCode.getCode();
        this.errAbstract = errorCode.getMsg();
    }

    public NacosApiException(int statusCode, ErrorCode errorCode, String message) {
        super(statusCode, message);
        this.detailErrCode = errorCode.getCode();
        this.errAbstract = errorCode.getMsg();
    }

    public int getDetailErrCode() {
        return detailErrCode;
    }

    public String getErrAbstract() {
        if (!StringUtils.isBlank(this.errAbstract)) {
            return this.errAbstract;
        }
        return Constants.NULL;
    }
}

