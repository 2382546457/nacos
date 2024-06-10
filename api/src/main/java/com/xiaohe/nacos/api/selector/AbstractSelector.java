package com.xiaohe.nacos.api.selector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = NoneSelector.class
)
public abstract class AbstractSelector implements Serializable {
    private static final long serialVersionUID = 4530233098102379229L;

    /**
     * 选择器类型, 所有子类都要有唯一的类型
     */
    @JsonIgnore
    private final String type;

    protected AbstractSelector(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }


}
