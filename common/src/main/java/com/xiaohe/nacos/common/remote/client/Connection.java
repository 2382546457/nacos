package com.xiaohe.nacos.common.remote.client;

import com.xiaohe.nacos.api.ability.constant.AbilityKey;
import com.xiaohe.nacos.api.ability.constant.AbilityStatus;
import com.xiaohe.nacos.api.remote.Requester;

import java.util.Map;

public abstract class Connection implements Requester {
    private String connectionId;

    private boolean abandon;

    protected RpcClient.ServerInfo serverInfo;

    /**
     * 当前连接具备的能力
     */
    protected Map<String, Boolean> abilityTable;

    public Connection(RpcClient.ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    //根据key判断当前连接是否具备对应功能的方法
    public AbilityStatus getConnectionAbility(AbilityKey abilityKey) {
        if (abilityTable == null || !abilityTable.containsKey(abilityKey.getName())) {
            return AbilityStatus.UNKNOWN;
        }
        return  abilityTable.get(abilityKey.getName()) ? AbilityStatus.SUPPORTED : AbilityStatus.NOT_SUPPORTED;
    }

    public boolean isAbilitiesSet() {
        return abilityTable != null;
    }


    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }


    public boolean isAbandon() {
        return abandon;
    }


    public void setAbandon(boolean abandon) {
        this.abandon = abandon;
    }
}
