package com.xiaohe.nacos.api.remote.response;

public enum ResponseCode {

    /**
     * Request success.
     */
    SUCCESS(200, "Response ok"),

    /**
     * Request failed.
     */
    FAIL(500, "Response fail");

    int code;

    String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * Getter method for property <tt>code</tt>.
     *
     * @return property value of code
     */
    public int getCode() {
        return code;
    }

    /**
     * Getter method for property <tt>desc</tt>.
     *
     * @return property value of desc
     */
    public String getDesc() {
        return desc;
    }
}