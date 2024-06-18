package com.xiaohe.nacos.common.remote.client;

import com.xiaohe.nacos.api.remote.request.Request;
import com.xiaohe.nacos.api.remote.response.Response;

/**
 * client 用于处理来自 Server 端的 Request
 */
public interface ServerRequestHandler {
    Response requestReply(Request request, Connection connection);
}
