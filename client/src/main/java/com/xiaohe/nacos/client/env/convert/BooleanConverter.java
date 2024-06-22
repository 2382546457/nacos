

package com.xiaohe.nacos.client.env.convert;




import com.xiaohe.nacos.common.utils.StringUtils;

import java.util.HashSet;
import java.util.Set;


/**
 * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
 * @author：陈清风扬，个人微信号：chenqingfengyangjj。
 * @date:2024/4/10
 * @方法描述：这个类型转换器就是用来把String转换为Boolean的
 */
class BooleanConverter extends AbstractPropertyConverter<Boolean> {
    
    private static final Set<String> TRUE_VALUES = new HashSet<>(8);
    
    private static final Set<String> FALSE_VALUES = new HashSet<>(8);
    
    static {
        TRUE_VALUES.add("true");
        TRUE_VALUES.add("on");
        TRUE_VALUES.add("yes");
        TRUE_VALUES.add("1");
        
        FALSE_VALUES.add("false");
        FALSE_VALUES.add("off");
        FALSE_VALUES.add("no");
        FALSE_VALUES.add("0");
    }
    
    @Override
    Boolean convert(String property) {
        if (StringUtils.isEmpty(property)) {
            return null;
        }
        property = property.toLowerCase();
        if (TRUE_VALUES.contains(property)) {
            return Boolean.TRUE;
        } else if (FALSE_VALUES.contains(property)) {
            return Boolean.FALSE;
        } else {
            throw new IllegalArgumentException("Invalid boolean value '" + property + "'");
        }
    }
}
