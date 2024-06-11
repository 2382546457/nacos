package com.xiaohe.nacos.api.remote.request;

import static com.xiaohe.nacos.api.common.Constants.Remote.INTERNAL_MODULE;


/**
 * 连接重置请求
 */
public class ConnectResetRequest extends ServerRequest {
    
    String serverIp;
    
    String serverPort;
    
    String connectionId;
    
    @Override
    public String getModule() {
        return INTERNAL_MODULE;
    }
    
    /**
     * Getter method for property <tt>connectionId</tt>.
     *
     * @return property value of connectionId
     */
    public String getConnectionId() {
        return connectionId;
    }
    
    /**
     * Setter method for property <tt>connectionId</tt>.
     *
     * @param connectionId value to be assigned to property connectionId
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /**
     * Getter method for property <tt>serverIp</tt>.
     *
     * @return property value of serverIp
     */
    public String getServerIp() {
        return serverIp;
    }
    
    /**
     * Setter method for property <tt>serverIp</tt>.
     *
     * @param serverIp value to be assigned to property serverIp
     */
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
    
    /**
     * Getter method for property <tt>serverPort</tt>.
     *
     * @return property value of serverPort
     */
    public String getServerPort() {
        return serverPort;
    }
    
    /**
     * Setter method for property <tt>serverPort</tt>.
     *
     * @param serverPort value to be assigned to property serverPort
     */
    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }
}