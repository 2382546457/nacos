package com.xiaohe.nacos.api.selector;

public class NoneSelector extends AbstractSelector {

    public NoneSelector() {
        super(SelectorType.none.name());
    }
}
