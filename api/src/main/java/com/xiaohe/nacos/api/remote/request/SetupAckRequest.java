package com.xiaohe.nacos.api.remote.request;

import java.util.Map;

import static com.xiaohe.nacos.api.common.Constants.Remote.INTERNAL_MODULE;

public class SetupAckRequest extends ServerRequest {
    
    private Map<String, Boolean> abilityTable;
    
    public SetupAckRequest() {
    }
    
    public SetupAckRequest(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }
    
    public Map<String, Boolean> getAbilityTable() {
        return abilityTable;
    }
    
    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }
    
    @Override
    public String getModule() {
        return INTERNAL_MODULE;
    }
}