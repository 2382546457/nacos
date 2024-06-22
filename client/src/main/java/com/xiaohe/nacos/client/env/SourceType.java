package com.xiaohe.nacos.client.env;

/**
 * 数据来源
 */
public enum SourceType {


    // 表明数据是从properties配置文件来的
    PROPERTIES,
    // 来源于jvm配置参数
    JVM,
    // 来源于系统环境
    ENV
}