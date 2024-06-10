package com.xiaohe.nacos.api.selector;

/**
 * 使用表达式筛选资源的选择器
 */
public class ExpressionSelector extends AbstractSelector {

    private String expression;

    protected ExpressionSelector() {
        super(SelectorType.label.name());
    }


    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
