package com.xiaohe.nacos.api.remote.response;

/**
 * 服务端检查响应
 */
public class ServerCheckResponse extends Response {
    private String connectionId;

    private boolean supportAbilityNegotiation;

    public ServerCheckResponse() {
    }

    public ServerCheckResponse(String connectionId, boolean supportAbilityNegotiation) {
        this.connectionId = connectionId;
        this.supportAbilityNegotiation = supportAbilityNegotiation;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public boolean isSupportAbilityNegotiation() {
        return supportAbilityNegotiation;
    }

    public void setSupportAbilityNegotiation(boolean supportAbilityNegotiation) {
        this.supportAbilityNegotiation = supportAbilityNegotiation;
    }
}
