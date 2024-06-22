package com.xiaohe.nacos.client.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.CoreConstants;

import com.xiaohe.nacos.client.logging.AbstractNacosLogging;
import com.xiaohe.nacos.common.log.NacosLogbackConfigurator;
import com.xiaohe.nacos.common.spi.NacosServiceLoader;
import com.xiaohe.nacos.common.utils.ResourceUtils;
import com.xiaohe.nacos.common.utils.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Support for Logback version 1.0.8 or higher
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @author <a href="mailto:hujun3@xiaomi.com">hujun</a>
 * @since 0.9.0
 */
public class LogbackNacosLogging extends AbstractNacosLogging {

    private static final String NACOS_LOGBACK_LOCATION = "classpath:nacos-logback.xml";

    private Integer userVersion = 2;

    /**
     * logback use 'ch.qos.logback.core.model.Model' since 1.3.0, set logback version during initialization.
     */
    public LogbackNacosLogging() {
        try {
            Class.forName("ch.qos.logback.core.model.Model");
        } catch (ClassNotFoundException e) {
            userVersion = 1;
        }
    }

    @Override
    public void loadConfiguration() {
        LoggerContext loggerContext = loadConfigurationOnStart();
        if (loggerContext.getObject(CoreConstants.RECONFIGURE_ON_CHANGE_TASK) != null && !hasListener(loggerContext)) {
            addListener(loggerContext);
        }
    }

    private boolean hasListener(LoggerContext loggerContext) {
        for (LoggerContextListener loggerContextListener : loggerContext.getCopyOfListenerList()) {
            if (loggerContextListener instanceof NacosLoggerContextListener) {
                return true;
            }
        }
        return false;
    }

    private LoggerContext loadConfigurationOnStart() {
        String location = getLocation(NACOS_LOGBACK_LOCATION);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Collection<NacosLogbackConfigurator> nacosLogbackConfigurators = NacosServiceLoader.load(
                NacosLogbackConfigurator.class);
        nacosLogbackConfigurators.stream().filter(c -> c.getVersion() == userVersion).findFirst()
                .ifPresent(nacosLogbackConfigurator -> {
                    nacosLogbackConfigurator.setContext(loggerContext);
                    if (StringUtils.isNotBlank(location)) {
                        try {
                            nacosLogbackConfigurator.configure(ResourceUtils.getResourceUrl(location));
                        } catch (Exception e) {
                            throw new IllegalStateException(
                                    "Could not initialize Logback Nacos logging from " + location, e);
                        }
                    }
                });
        return loggerContext;
    }

    class NacosLoggerContextListener implements LoggerContextListener {

        @Override
        public boolean isResetResistant() {
            return true;
        }

        @Override
        public void onReset(LoggerContext context) {
            loadConfigurationOnStart();
        }

        @Override
        public void onStart(LoggerContext context) {

        }

        @Override
        public void onStop(LoggerContext context) {

        }

        @Override
        public void onLevelChange(Logger logger, Level level) {

        }
    }

    private void addListener(LoggerContext loggerContext) {
        loggerContext.addListener(new NacosLoggerContextListener());
    }

}
