package com.xiaohe.nacos.api;

/**
 * Support for reading the value of the specified variable from the -D parameter.
 *
 * <p>Properties that are preferred to which in {@link PropertyKeyConst}
 *
 * @author pbting
 */
public interface SystemPropertyKeyConst {

    String NAMING_SERVER_PORT = "nacos.naming.exposed.port";

    /**
     * In the cloud (Alibaba Cloud or other cloud vendors) environment, whether to enable namespace resolution in the
     * cloud environment.
     * <p>
     * The default is on.
     * </p>
     */
    String IS_USE_CLOUD_NAMESPACE_PARSING = "nacos.use.cloud.namespace.parsing";

    /**
     * In the cloud environment, if the process level requires a globally uniform namespace, it can be specified with
     * the -D parameter.
     */
    String ANS_NAMESPACE = "ans.namespace";

    /**
     * It is also supported by the -D parameter.
     */
    String IS_USE_ENDPOINT_PARSING_RULE = "nacos.use.endpoint.parsing.rule";
}