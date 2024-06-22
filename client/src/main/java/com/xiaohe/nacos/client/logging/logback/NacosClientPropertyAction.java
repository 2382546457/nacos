package com.xiaohe.nacos.client.logging.logback;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;
import com.xiaohe.nacos.client.env.NacosClientProperties;
import org.xml.sax.Attributes;

/**
 * support logback read properties from NacosClientProperties. just like springProperty.
 * for example:
 * <nacosClientProperty scope="context" name="logPath" source="system.log.path" defaultValue="/root" />
 * @author onewe
 */
class NacosClientPropertyAction extends Action {

    private static final String DEFAULT_VALUE_ATTRIBUTE = "defaultValue";

    private static final String SOURCE_ATTRIBUTE = "source";

    @Override
    public void begin(InterpretationContext ic, String elementName, Attributes attributes) throws ActionException {
        String name = attributes.getValue(NAME_ATTRIBUTE);
        String source = attributes.getValue(SOURCE_ATTRIBUTE);
        ActionUtil.Scope scope = ActionUtil.stringToScope(attributes.getValue(SCOPE_ATTRIBUTE));
        String defaultValue = attributes.getValue(DEFAULT_VALUE_ATTRIBUTE);
        if (OptionHelper.isEmpty(name)) {
            addError("The \"name\" and \"source\"  attributes of <nacosClientProperty> must be set");
        }
        ActionUtil.setProperty(ic, name, getValue(source, defaultValue), scope);
    }

    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {

    }

    private String getValue(String source, String defaultValue) {
        return NacosClientProperties.PROTOTYPE.getProperty(source, defaultValue);
    }
}