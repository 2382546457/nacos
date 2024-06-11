package com.xiaohe.nacos.api.naming.utils;

import com.xiaohe.nacos.api.common.Constants;
import com.xiaohe.nacos.api.exception.NacosException;
import com.xiaohe.nacos.api.exception.api.NacosApiException;
import com.xiaohe.nacos.api.model.v2.ErrorCode;
import com.xiaohe.nacos.api.naming.pojo.Instance;
import com.xiaohe.nacos.api.utils.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.xiaohe.nacos.api.common.Constants.CLUSTER_NAME_PATTERN_STRING;
import static com.xiaohe.nacos.api.common.Constants.NUMBER_PATTERN_STRING;

public class NamingUtils {

    private static final Pattern CLUSTER_NAME_PATTERN = Pattern.compile(CLUSTER_NAME_PATTERN_STRING);

    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_PATTERN_STRING);

    public static String getGroupedName(final String serviceName, final String groupName) {
        if (StringUtils.isBlank(serviceName)) {
            throw new IllegalArgumentException("Param 'serviceName' is illegal, serviceName is blank");
        }
        if (StringUtils.isBlank(groupName)) {
            throw new IllegalArgumentException("Param 'groupName' is illegal, groupName is blank");
        }
        final String resultGroupedName = groupName + Constants.SERVICE_INFO_SPLITER + serviceName;
        return resultGroupedName.intern();
    }

    public static String getServiceName(final String serviceNameWithGroup) {
        if (StringUtils.isBlank(serviceNameWithGroup)) {
            return StringUtils.EMPTY;
        }
        if (!serviceNameWithGroup.contains(Constants.SERVICE_INFO_SPLITER)) {
            return serviceNameWithGroup;
        }
        return serviceNameWithGroup.split(Constants.SERVICE_INFO_SPLITER)[1];
    }

    public static String getGroupName(final String serviceNameWithGroup) {
        if (StringUtils.isBlank(serviceNameWithGroup)) {
            return StringUtils.EMPTY;
        }
        if (!serviceNameWithGroup.contains(Constants.SERVICE_INFO_SPLITER)) {
            return Constants.DEFAULT_GROUP;
        }
        return serviceNameWithGroup.split(Constants.SERVICE_INFO_SPLITER)[0];
    }

    /**
     * check combineServiceName format. the serviceName can't be blank.
     * <pre>
     * serviceName = "@@";                 the length = 0; illegal
     * serviceName = "group@@";            the length = 1; illegal
     * serviceName = "@@serviceName";      the length = 2; illegal
     * serviceName = "group@@serviceName"; the length = 2; legal
     * </pre>
     *
     * @param combineServiceName such as: groupName@@serviceName
     */
    public static void checkServiceNameFormat(String combineServiceName) {
        String[] split = combineServiceName.split(Constants.SERVICE_INFO_SPLITER);
        if (split.length <= 1) {
            throw new IllegalArgumentException(
                    "Param 'serviceName' is illegal, it should be format as 'groupName@@serviceName'");
        }
        if (split[0].isEmpty()) {
            throw new IllegalArgumentException("Param 'serviceName' is illegal, groupName can't be empty");
        }
    }


    public static String getGroupedNameOptional(final String serviceName, final String groupName) {
        return groupName + Constants.SERVICE_INFO_SPLITER + serviceName;
    }

    //检查服务实例中的各个属性的值是否非法
    public static void checkInstanceIsLegal(Instance instance) throws NacosException {
        //实例对象为null，抛出异常
        if (null == instance) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.INSTANCE_ERROR,
                    "Instance can not be null.");
        }
        //检查用户设定的服务实例的心跳超时时间和ip超时时间是否小于心跳间隔时间，如果小于肯定要报错
        //假如心跳每3秒发送一次，但心跳超时时间是1秒，那还发送个毛心跳啊，直接超时就行
        if (instance.getInstanceHeartBeatTimeOut() < instance.getInstanceHeartBeatInterval()
                || instance.getIpDeleteTimeout() < instance.getInstanceHeartBeatInterval()) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.INSTANCE_ERROR,
                    "Instance 'heart beat interval' must less than 'heart beat timeout' and 'ip delete timeout'.");
        }
        //检查服务实例所在集群名称是否为空，以及是否符合规范
        if (!StringUtils.isEmpty(instance.getClusterName()) && !CLUSTER_NAME_PATTERN.matcher(instance.getClusterName()).matches()) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.INSTANCE_ERROR,
                    String.format("Instance 'clusterName' should be characters with only 0-9a-zA-Z-. (current: %s)",
                            instance.getClusterName()));
        }
    }

    /**
     * check batch register is Ephemeral.
     * @param instance instance
     * @throws NacosException NacosException
     */
    public static void checkInstanceIsEphemeral(Instance instance) throws NacosException {
        if (!instance.isEphemeral()) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.INSTANCE_ERROR,
                    String.format("Batch registration does not allow persistent instance registration , Instance：%s", instance));
        }
    }

    /**
     * Batch verify the validity of instances.
     * @param instances List of instances to be registered
     * @throws NacosException Nacos
     */
    public static void batchCheckInstanceIsLegal(List<Instance> instances) throws NacosException {
        Set<Instance> newInstanceSet = new HashSet<>(instances);
        for (Instance instance : newInstanceSet) {
            checkInstanceIsEphemeral(instance);
            checkInstanceIsLegal(instance);
        }
    }

    /**
     * Check string is a number or not.
     *
     * @param str a string of digits
     * @return if it is a string of digits, return true
     */
    public static boolean isNumber(String str) {
        return !StringUtils.isEmpty(str) && NUMBER_PATTERN.matcher(str).matches();
    }
}
