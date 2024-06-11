package com.xiaohe.nacos.api.remote.request;

import com.xiaohe.nacos.api.remote.Payload;

import java.util.Map;
import java.util.TreeMap;

public abstract class Request implements Payload {
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private String requestId;

    public abstract String getModule();

    public void putHeader(String key, String value) {
        headers.put(key, value);
    }

    public void putAllHeader(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        this.headers.putAll(headers);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getHeader(String key, String defaultValue) {
        String value = headers.get(key);
        return (value == null) ? defaultValue : value;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void clearHeaders() {
        this.headers.clear();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "headers=" + headers + ", requestId='" + requestId + '\'' + '}';
    }
}
