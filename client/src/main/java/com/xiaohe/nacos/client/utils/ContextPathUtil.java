package com.xiaohe.nacos.client.utils;


import com.xiaohe.nacos.common.utils.StringUtils;

/**
 * Context path Util.
 *
 * @author Wei.Wang
 */
public class ContextPathUtil {

    private static final String ROOT_WEB_CONTEXT_PATH = "/";

    /**
     * normalize context path.
     *
     * @param contextPath origin context path
     * @return normalized context path
     */
    public static String normalizeContextPath(String contextPath) {
        if (StringUtils.isBlank(contextPath) || ROOT_WEB_CONTEXT_PATH.equals(contextPath)) {
            return StringUtils.EMPTY;
        }
        return contextPath.startsWith(ROOT_WEB_CONTEXT_PATH) ? contextPath : ROOT_WEB_CONTEXT_PATH + contextPath;
    }
}