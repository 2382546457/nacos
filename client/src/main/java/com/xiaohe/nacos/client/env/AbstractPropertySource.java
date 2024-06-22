package com.xiaohe.nacos.client.env;

import java.util.Properties;

abstract class AbstractPropertySource {
    
    /**
     * get property's type.
     * @return name
     */
    abstract SourceType getType();
    
    /**
     * get property, if the value can not be got by the special key, the null will be returned.
     * @param key special key
     * @return value or null
     */
    abstract String getProperty(String key);
    
    /**
     * Tests if the specified object is a key in this propertySource.
     * @param key key – possible key
     * @return true if and only if the specified object is a key in this propertySource, false otherwise.
     */
    abstract boolean containsKey(String key);
    
    /**
     * to properties.
     * @return properties
     */
    abstract Properties asProperties();
    
}