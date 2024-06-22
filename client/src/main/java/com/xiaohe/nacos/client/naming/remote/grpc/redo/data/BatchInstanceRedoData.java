package com.xiaohe.nacos.client.naming.remote.grpc.redo.data;

import com.xiaohe.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Objects;

public class BatchInstanceRedoData extends InstanceRedoData {
    List<Instance> instances;


    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    protected BatchInstanceRedoData(String serviceName, String groupName) {
        super(serviceName, groupName);
    }

    public static BatchInstanceRedoData build(String serviceName, String groupName, List<Instance> instances) {
        BatchInstanceRedoData result = new BatchInstanceRedoData(serviceName, groupName);
        result.setInstances(instances);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BatchInstanceRedoData)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        BatchInstanceRedoData redoData = (BatchInstanceRedoData) o;
        return Objects.equals(instances, redoData.instances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instances);
    }
}
