/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaohe.nacos.client.env.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

/**
 * 类型转换器对象，这个类型转换器中持有了三个不同的类型转换器
 */
public class CompositeConverter {
    
    private final Map<Class<?>, AbstractPropertyConverter<?>> converterRegistry = new HashMap<>();
    
    public CompositeConverter() {
        //在这里添加了三个不同的类型转换器
        converterRegistry.put(Boolean.class, new BooleanConverter());
        converterRegistry.put(Integer.class, new IntegerConverter());
        converterRegistry.put(Long.class, new LongConverter());
    }
    

    /**
     * 转换类型的方法
     */
    public <T> T convert(String property, Class<T> targetClass) {
        final AbstractPropertyConverter<?> converter = converterRegistry.get(targetClass);
        if (converter == null) {
            throw new MissingFormatArgumentException("converter not found, can't convert from String to " + targetClass.getCanonicalName());
        }
        return (T) converter.convert(property);
    }
    
}
