package com.xiaohe.nacos.common.remote.client;

import java.util.List;

public interface ServerListFactory {

    String genNextServer();

    String getCurrentServer();

    List<String> getServerList();
}
