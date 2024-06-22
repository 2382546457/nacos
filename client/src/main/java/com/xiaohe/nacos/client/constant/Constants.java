package com.xiaohe.nacos.client.constant;

import java.util.concurrent.TimeUnit;

public class Constants {

    public static class SysEnv {

        public static final String USER_HOME = "user.home";

        public static final String PROJECT_NAME = "project.name";

        public static final String JM_LOG_PATH = "JM.LOG.PATH";

        public static final String JM_SNAPSHOT_PATH = "JM.SNAPSHOT.PATH";

        public static final String NACOS_ENV_FIRST = "nacos.env.first";

    }

    public static class Disk {

        public static final String READ_ONLY = "r";

        public static final String READ_WRITE = "rw";
    }

    public static class HealthCheck {

        public static final String UP = "UP";

        public static final String DOWN = "DOWN";
    }

    public static class Security {

        public static final long SECURITY_INFO_REFRESH_INTERVAL_MILLS = TimeUnit.SECONDS.toMillis(5);

    }

}