package com.xiaohe.nacos.client.env;

import java.util.Properties;


/**
 * JVM属性源
 */
class JvmArgsPropertySource extends AbstractPropertySource {
    
    private final Properties properties;
    
    JvmArgsPropertySource() {
        //在这里jvm属性源信息已经收集好了
        this.properties = System.getProperties();
    }
    
    @Override
    SourceType getType() {
        return SourceType.JVM;
    }
    
    @Override
    String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    @Override
    boolean containsKey(String key) {
        return properties.containsKey(key);
    }
    
    @Override
    Properties asProperties() {
        Properties properties = new Properties();
        properties.putAll(this.properties);
        return properties;
    }
}
