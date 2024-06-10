package com.xiaohe.nacos.api.selector;

/**
 * 选择器类型
 */
public enum SelectorType {
    /**
     * not match any type.
     */
    unknown,
    /**
     * not filter out any entity.
     */
    none,
    /**
     * select by label.
     */
    label,
    /**
     * select by cluster.
     */
    cluster,
    /**
     * select by health state.
     */
    health,
    /**
     * select by enable state.
     */
    enable
}
