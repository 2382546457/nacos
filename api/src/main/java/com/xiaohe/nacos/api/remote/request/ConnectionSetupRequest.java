package com.xiaohe.nacos.api.remote.request;

import java.util.HashMap;
import java.util.Map;

/**
 * 连接设置请求
 */
public class ConnectionSetupRequest extends InternalRequest {

    private String clientVersion;

    private String tenant;

    private Map<String, String> labels = new HashMap<>();

    /**
     * 此次连接的设置拥有什么能力
     */
    private Map<String, Boolean> abilityTable;


    public ConnectionSetupRequest() {
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Map<String, Boolean> getAbilityTable() {
        return abilityTable;
    }

    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }
}
