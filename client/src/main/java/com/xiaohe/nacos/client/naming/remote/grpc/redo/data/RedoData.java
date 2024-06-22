package com.xiaohe.nacos.client.naming.remote.grpc.redo.data;

import java.util.Objects;

public class RedoData<T> {
    private final String serviceName;

    private final String groupName;

    // 最终期望的操作
    private volatile boolean expectedRegistered;

    // 是否已经注册
    private volatile boolean registered;

    // 是否已注销
    private volatile boolean unregistering;

    private T data;

    protected RedoData(String serviceName, String groupName) {
        this.serviceName = serviceName;
        this.groupName = groupName;
        this.expectedRegistered = true;
    }

    public RedoData(boolean expectedRegistered, String groupName, String serviceName) {
        this.expectedRegistered = expectedRegistered;
        this.groupName = groupName;
        this.serviceName = serviceName;
    }

    //判断操作是否需要重做的逻辑，核心逻辑就在getRedoType()方法中
    public boolean isNeedRedo() {
        return !RedoType.NONE.equals(getRedoType());
    }

    public RedoType getRedoType() {
        if (isRegistered() && !isUnregistering()) {
            return expectedRegistered ? RedoType.NONE : RedoType.UNREGISTER;
        } else if (isRegistered() && isUnregistering()) {
            return RedoType.UNREGISTER;
        } else if (!isRegistered() && !isUnregistering()) {
            return RedoType.REGISTER;
        } else {
            return expectedRegistered ? RedoType.REGISTER : RedoType.REMOVE;
        }
    }


    // 设置已注册状态
    public void registered() {
        this.registered = true;
        this.unregistering = false;
    }

    // 设置已注销状态
    public void unregistered() {
        this.registered = false;
        this.unregistering = true;
    }

    public enum RedoType {

        // 需要进行注册操作
        REGISTER,

        // 需要执行注销操作
        UNREGISTER,

        // 不执行任何操作
        NONE,

        // 从 map 中移除重做对象
        REMOVE;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isExpectedRegistered() {
        return expectedRegistered;
    }

    public void setExpectedRegistered(boolean expectedRegistered) {
        this.expectedRegistered = expectedRegistered;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isUnregistering() {
        return unregistering;
    }

    public void setUnregistering(boolean unregistering) {
        this.unregistering = unregistering;
    }

    public T getData() {
        return data;
    }
    public void set(T data) {
        this.data = data;
    }
    public void setData(T data) {
        this.data = data;
    }
    @Override
    public String toString() {
        return "RedoData{" +
                "serviceName='" + serviceName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", expectedRegistered=" + expectedRegistered +
                ", registered=" + registered +
                ", unregistering=" + unregistering +
                ", data=" + data +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedoData<?> redoData = (RedoData<?>) o;
        return expectedRegistered == redoData.expectedRegistered && registered == redoData.registered && unregistering == redoData.unregistering && Objects.equals(serviceName, redoData.serviceName) && Objects.equals(groupName, redoData.groupName) && Objects.equals(data, redoData.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, groupName, expectedRegistered, registered, unregistering, data);
    }
}
