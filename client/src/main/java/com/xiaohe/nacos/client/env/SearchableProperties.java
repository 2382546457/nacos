package com.xiaohe.nacos.client.env;


import com.xiaohe.nacos.client.constant.Constants;
import com.xiaohe.nacos.client.env.convert.CompositeConverter;
import com.xiaohe.nacos.common.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

class SearchableProperties implements NacosClientProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchableProperties.class);

    //jvm属性源对象
    private static final JvmArgsPropertySource JVM_ARGS_PROPERTY_SOURCE = new JvmArgsPropertySource();

    //系统环境变量属性源对象
    private static final SystemEnvPropertySource SYSTEM_ENV_PROPERTY_SOURCE = new SystemEnvPropertySource();

    //这个集合中定义了搜索配置信息时的顺序，当SEARCH_ORDER对象被创建完毕之后，正确的搜索顺序应该为先从PROPERTIES配置文件中寻找
    //再从jvm属性源中寻找，再从系统环境变量中寻找
    private static final List<SourceType> SEARCH_ORDER;

    //类型转换器，还记得之前更新的spring第一版本代码吗？spring第四篇文章就要更新类型转换器
    //但是一直没有给大家更新，不过在nacos中的类型转换器非常简单，需要转换的类型也非常少，所以nacos中定义的类型转换器非常简单
    private static final CompositeConverter CONVERTER = new CompositeConverter();

    //在静态代码块中执行属性源的排序操作
    static {
        //因为枚举对象底层其实是可以排序的，定义在前面的枚举对象对应的int值比较小，这里先提供了一个默认排序
        //那就是配置类中的属性源排在前面，也就是优先从配置类中查找对应的信息
        //排在第二的是jvm属性源，最后是系统环境变量属性源
        List<SourceType> initOrder = Arrays.asList(SourceType.PROPERTIES, SourceType.JVM, SourceType.ENV);

        //上面只是一个默认的排序，也可能用户在jvm属性源中自定义了属性源优先级，所以要在jvm属性源中根据key查找对应的value
        //如果对应的value不为空，说明用户自定义了属性源优先级
        String firstEnv = JVM_ARGS_PROPERTY_SOURCE.getProperty(Constants.SysEnv.NACOS_ENV_FIRST);
        //如果jvm属性源中没有对应的value，那就从系统环境变量中继续查找
        if (StringUtils.isBlank(firstEnv)) {
            firstEnv = SYSTEM_ENV_PROPERTY_SOURCE.getProperty(Constants.SysEnv.NACOS_ENV_FIRST);
        }
        //执行到这里会再次会firstEnv进行过一次判断
        //如果为空，就意味着用户没有自定义属性源优先级，那就使用默认的即可，如果不为空意味着用户自定义了优先级，需要更新initOrder集合中属性源的顺序
        if (StringUtils.isNotBlank(firstEnv)) {
            try {
                //先将字符串转换为具体的枚举对象
                final SourceType sourceType = SourceType.valueOf(firstEnv.toUpperCase());
                //判断枚举对象是否为SourceType.PROPERTIES
                if (!sourceType.equals(SourceType.PROPERTIES)) {
                    //如果不为SourceType.PROPERTIES，则意味着是新的属性源优先级
                    //从initOrder集合中，根据具体的枚举对象获取枚举对象所在位置的索引
                    final int index = initOrder.indexOf(sourceType);
                    //把用户自己定义的最高级属性源对象放在集合第一位
                    final SourceType replacedSourceType = initOrder.set(0, sourceType);
                    //把要被替换的属性源放在靠后的位置
                    initOrder.set(index, replacedSourceType);
                }
            } catch (Exception e) {
                LOGGER.warn("first source type parse error, it will be used default order!", e);
            }
        }
        //在这里给SEARCH_ORDER集合赋值成功
        SEARCH_ORDER = initOrder;
        //下面就是记录属性源优先级日志信息的操作了，简单看看就行
        StringBuilder orderInfo = new StringBuilder("properties search order:");
        for (int i = 0; i < SEARCH_ORDER.size(); i++) {
            orderInfo.append(SEARCH_ORDER.get(i).toString());
            if (i < SEARCH_ORDER.size() - 1) {
                orderInfo.append("->");
            }
        }
        LOGGER.debug(orderInfo.toString());
    }

    //单例模式
    static final SearchableProperties INSTANCE = new SearchableProperties();

    //这里成员变量中存放了所有的属性源对象，查找配置信息的时候，就是从这个成员变量中查找的
    private final List<AbstractPropertySource> propertySources;

    //这个成员变量主要是为了将用户自己定义的配置类属性源的数据添加到propertySources中，具体逻辑可以通过derive方法查看
    private final PropertiesPropertySource propertiesPropertySource;

    //下面都是非常简单的操作了，大家自己看看代码即可，像这种属性源的组件，在很多框架中都已经见过了吧？大家应该对这个组件很熟悉了
    //应该看一眼就知道里面的结构和运行流程
    private SearchableProperties() {
        this(new PropertiesPropertySource());
    }

    private SearchableProperties(PropertiesPropertySource propertiesPropertySource) {
        this.propertiesPropertySource = propertiesPropertySource;
        this.propertySources = build(propertiesPropertySource, JVM_ARGS_PROPERTY_SOURCE, SYSTEM_ENV_PROPERTY_SOURCE);
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, null);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return this.search(key, String.class).orElse(defaultValue);
    }

    @Override
    public String getPropertyFrom(SourceType source, String key) {
        if (source == null) {
            return this.getProperty(key);
        }
        switch (source) {
            case JVM:
                return JVM_ARGS_PROPERTY_SOURCE.getProperty(key);
            case ENV:
                return SYSTEM_ENV_PROPERTY_SOURCE.getProperty(key);
            case PROPERTIES:
                return this.propertiesPropertySource.getProperty(key);
            default:
                return this.getProperty(key);
        }
    }

    @Override
    public Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return this.search(key, Boolean.class).orElse(defaultValue);
    }

    @Override
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        return this.search(key, Integer.class).orElse(defaultValue);
    }

    @Override
    public Long getLong(String key) {
        return getLong(key, null);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        return this.search(key, Long.class).orElse(defaultValue);
    }

    @Override
    public void setProperty(String key, String value) {
        propertiesPropertySource.setProperty(key, value);
    }

    @Override
    public void addProperties(Properties properties) {
        propertiesPropertySource.addProperties(properties);
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/4/10
     * @方法描述：将propertySources中所有属性源对象转换为Properties对象的方法
     */
    @Override
    public Properties asProperties() {
        //创建一个Properties对象
        Properties properties = new Properties();
        //得到属性源集合的迭代器，然后在循环中把每一个属性源对象存放到properties中
        final ListIterator<AbstractPropertySource> iterator = propertySources.listIterator(propertySources.size());
        while (iterator.hasPrevious()) {
            final AbstractPropertySource previous = iterator.previous();
            properties.putAll(previous.asProperties());
        }
        return properties;
    }

    @Override
    public boolean containsKey(String key) {
        for (AbstractPropertySource propertySource : propertySources) {
            final boolean containing = propertySource.containsKey(key);
            if (containing) {
                return true;
            }
        }
        return false;
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/4/10
     * @方法描述：这个方法就是根据key从属性源集合中寻找对应value的方法，这里之所以传入了一个targetType参数，是要把获得的value转换为targetType的类型
     * 在该方法中就用到了类型转换器
     */
    private <T> Optional<T> search(String key, Class<T> targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("target type must not be null!");
        }//下面就是遍历propertySources成员变量，优先从PROPERTIES类型的属性源对象中根据key查找value
        for (AbstractPropertySource propertySource : propertySources) {
            final String value = propertySource.getProperty(key);
            if (value != null) {
                if (String.class.isAssignableFrom(targetType)) {
                    try {
                        return (Optional<T>) Optional.of(value);
                    } catch (Exception e) {
                        LOGGER.error("target type convert error", e);
                        return Optional.empty();
                    }
                }//找到对应的value之后，还要把value转换为用户指定的类型
                return Optional.ofNullable(CONVERTER.convert(value, targetType));
            }
        }
        return Optional.empty();
    }


    /**
     * @课程描述:从零带你写框架系列中的课程，整个系列包含netty，xxl-job，rocketmq，nacos，sofajraft，spring，springboot，disruptor，编译器，虚拟机等等。
     * @author：陈清风扬，个人微信号：chenqingfengyangjj。
     * @date:2024/4/10
     * @方法描述：唯一需要解释一下的就是这个方法，请大家看看下面的注释
     */
    private List<AbstractPropertySource> build(AbstractPropertySource... propertySources) {
        //因为每一个属性源对象中都可以通过getType()方法，表明自己所属的类型，所以就可以根据这个把三个属性源对象转换位一个key为SourceType，value为AbstractPropertySource的map对象
        final Map<SourceType, AbstractPropertySource> sourceMap = Arrays.stream(propertySources)
                .collect(Collectors.toMap(AbstractPropertySource::getType, propertySource -> propertySource));
        //然后再根据属性源优先级，返回一个存放了AbstractPropertySource对象的结合，这个集合最终会被赋值给本类的propertySources成员变量
        //这样一来，当通过本类的search方法查找对应的数据时，会遍历propertySources成员变量，首先遍历到的就是PROPERTIES类型的属性源对象，由此实现了属性源提供信息的优先级
        return SEARCH_ORDER.stream().map(sourceMap::get).collect(Collectors.toList());
    }


    @Override
    public NacosClientProperties derive() {
        return new SearchableProperties(new PropertiesPropertySource(this.propertiesPropertySource));
    }

    @Override
    public NacosClientProperties derive(Properties properties) {
        final NacosClientProperties nacosClientProperties = this.derive();
        nacosClientProperties.addProperties(properties);
        return nacosClientProperties;
    }
}

