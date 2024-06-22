package com.xiaohe.nacos.client.logging;

import com.xiaohe.nacos.client.logging.logback.LogbackNacosLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * nacos logging.
 *
 * @author mai.jh
 */
public class NacosLogging {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosLogging.class);

    private AbstractNacosLogging nacosLogging;

    private boolean isLogback = false;

    private NacosLogging() {
        try {
            Class.forName("ch.qos.logback.classic.Logger");
            nacosLogging = new LogbackNacosLogging();
            isLogback = true;
        } catch (ClassNotFoundException e) {

        }
    }

    private static class NacosLoggingInstance {

        private static final NacosLogging INSTANCE = new NacosLogging();
    }

    public static NacosLogging getInstance() {
        return NacosLoggingInstance.INSTANCE;
    }

    /**
     * Load logging Configuration.
     */
    public void loadConfiguration() {
        try {
            nacosLogging.loadConfiguration();
        } catch (Throwable t) {
            if (isLogback) {
                LOGGER.warn("Load Logback Configuration of Nacos fail, message: {}", t.getMessage());
            } else {
                LOGGER.warn("Load Log4j Configuration of Nacos fail, message: {}", t.getMessage());
            }
        }
    }
}
