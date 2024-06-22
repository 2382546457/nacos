package com.xiaohe.nacos.client.utils;


import com.xiaohe.nacos.api.PropertyKeyConst;
import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.client.env.NacosClientProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All parameter validation tools.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class ValidatorUtils {

    private static final Pattern CONTEXT_PATH_MATCH = Pattern.compile("(\\/)\\1+");

    //String CONTEXT_PATH = "contextPath";
    public static void checkInitParam(NacosClientProperties properties) throws NacosException {
        checkContextPath(properties.getProperty(PropertyKeyConst.CONTEXT_PATH));
    }

    /**
     * Check context path.
     *
     * @param contextPath context path
     */
    public static void checkContextPath(String contextPath) {
        if (contextPath == null) {
            return;
        }
        Matcher matcher = CONTEXT_PATH_MATCH.matcher(contextPath);
        if (matcher.find()) {
            throw new IllegalArgumentException("Illegal url path expression");
        }
    }

}