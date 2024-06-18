package com.xiaohe.nacos.common.remote;

/**
 * 连接类型
 */
public enum ConnectionType {

    /**
     * gRPC connection.
     */
    GRPC("GRPC", "Grpc Connection");

    final String type;

    final String name;

    public static ConnectionType getByType(String type) {
        ConnectionType[] values = ConnectionType.values();
        for (ConnectionType connectionType : values) {
            if (connectionType.getType().equals(type)) {
                return connectionType;
            }
        }
        return null;
    }

    ConnectionType(String type, String name) {
        this.type = type;
        this.name = name;
    }


    public String getType() {
        return type;
    }


    public String getName() {
        return name;
    }
}
