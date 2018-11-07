package com.meituan.service.mobile.mtthrift.util;

import com.dianping.cat.Cat;
import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.server.flow.FlowCopyTask;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/5/19
 * Time: 10:52
 */
public class MtConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(MtConfigUtil.class);
    private static String appName;
    private static MtConfigClient mtConfigClient;
    private static volatile boolean mtConfigClientInitiated = false;
    private static volatile boolean serverCfgInitiated = false;

    //mcc 动态配置
    public static final String OCTO_INVOKER_TIMEOUT = "octo.invoker.timeout";
    public static final String OCTO_INVOKER_CAT_ENABLE = "octo.invoker.cat.enable";
    public static final String OCTO_INVOKER_MTRACE_ENABLE = "octo.invoker.mtrace.enable";
    public static final String OCTO_INVOKER_METHODTIMEOUTRETRY = "octo.invoker.timeoutRetry";
    public static final String OCTO_INVOKER_FAULTINJECT_ENABLE = "octo.invoker.faultInject.enable";
    public static final String OCTO_INVOKER_AUTH_ENABLE = "octo.invoker.auth.enable";

    public static final String OCTO_PROVIDER_AUTH_ENABLE = "octo.provider.auth.enable";
    public static final String OCTO_PROVIDER_CAT_ENABLE = "octo.provider.cat.enable";
    public static final String OCTO_PROVIDER_MTRACE_ENABLE = "octo.provider.mtrace.enable";
    public static final String OCTO_PROVIDER_FLOWCOPY = "octo.provider.flowcopy";
    public static final String OCTO_PROVIDER_GRAY_AUTH = "octo.provider.auth.gray";
    public static final String OCTO_PROVIDER_AUTH_DEBUG_LOG_ENABLE = "octo.provider.auth.log.enable";
    public static final String OCTO_PROVIDER_LIMIT_ENABLE = "octo.provider.limit.enable";
    public static final String OCTO_PROVIDER_AUTH_ERROR_ENABLE = "octo.provider.auth.error.log.enable";

    public static final String CONFIG_FILE_NAME = "META-INF/app.properties";

    static {
        try {
            Properties props = new Properties();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = null;
            if (cl != null) {
                is = cl.getResourceAsStream(CONFIG_FILE_NAME);
            }
            if (is == null) {
                cl = MtConfigUtil.class.getClassLoader();
                is = cl.getResourceAsStream(CONFIG_FILE_NAME);
            }
            if (is != null) {
                try {
                    try {
                        props.load(is);
                    } finally {
                        is.close();
                    }
                } catch (IOException e) {
                    logger.debug("Load {} IO Exception ", CONFIG_FILE_NAME, e);
                }
            }
            logger.info("props loaded:{}", props);

            appName = props.getProperty("app.name");

            mtConfigClient = new MtConfigClient();
            //1.0.0及后面版本使用
            mtConfigClient.setModel("v2");

            //octo上申请的appkey
            mtConfigClient.setAppkey(appName);
            // 配置实例的标识(id),必须在服务进程内全局唯一
            mtConfigClient.setId("mtthrift-mtConfigClient");
            //可选，扫描注解的根目录，默认全部扫描, jar包里面的也会扫描
            mtConfigClient.setScanBasePackage("com.meituan.service.mobile.mtthrift.util");

            if (appName == null) {
                logger.warn("app.name is null, MtConfigUtil init failed");
            } else {
                try {
                    // 初始化client
                    mtConfigClient.init();
                    mtConfigClientInitiated = true;
                } catch (Exception e) {
                    String message = new StringBuilder("mtConfig client init failed，cause by ")
                            .append(e.getClass().getName()).append(":")
                            .append(e.getMessage()).append("appkey:")
                            .append(appName).toString();
                    logger.warn(message);
                    Cat.logEvent("MtConfigInitializeError", message);
                }
            }

            if (mtConfigClientInitiated) {
                ThriftClientGlobalConfig thriftClientGlobalConfig = new ThriftClientGlobalConfig();
                getClientCatEnabledFromMcc(thriftClientGlobalConfig);
                getClientMtraceEnabledFromMcc(thriftClientGlobalConfig);
                getClientFaultInjectEnabledFromMcc(thriftClientGlobalConfig);
                getClientAuthEnableFromMcc(thriftClientGlobalConfig);

                addClientCatEnabledListener(thriftClientGlobalConfig);
                addClientMtraceEnabledListener(thriftClientGlobalConfig);
                addClientFaultInjectEnabledListener(thriftClientGlobalConfig);
                addClientAuthEnableListener(thriftClientGlobalConfig);

                ThriftServerGlobalConfig thriftServerGlobalConfig = new ThriftServerGlobalConfig();

                getServerCatEnabledFromMcc(thriftServerGlobalConfig);
                getServerMtraceEnabledFromMcc(thriftServerGlobalConfig);
                getServerAuthEnabledFromMcc(thriftServerGlobalConfig);
                getServerGrayAuthEnabledFromMcc(thriftServerGlobalConfig);
                getServerLimitEnabledFromMcc(thriftServerGlobalConfig);
                getServerAuthErrorLogEnabledFromMcc(thriftServerGlobalConfig);
                getServerAuthDebugLogEnabledFromMcc(thriftServerGlobalConfig);

                addServerLimitEnabledListener(thriftServerGlobalConfig);
                addServerCatEnabledListener(thriftServerGlobalConfig);
                addServerMtraceEnabledListener(thriftServerGlobalConfig);
                addServerAuthEnabledListener(thriftServerGlobalConfig);
                addServerGrayAuthEnabledListener(thriftServerGlobalConfig);
                addServerAuthErrorEnabledListener(thriftServerGlobalConfig);
                addServerAuthDebugLogEnabledListener(thriftServerGlobalConfig);
            }
        } catch (Exception e) {
            logger.warn("MtConfigUtil init failed", e);
        }
    }

    private static void getServerAuthErrorLogEnabledFromMcc(ThriftServerGlobalConfig thriftServerGlobalConfig) {
        String value = mtConfigClient.getValue(OCTO_PROVIDER_AUTH_ERROR_ENABLE);
        if (value != null) {
            thriftServerGlobalConfig.setEnableAuthErrorLog(!"false".equals(value));
        }
    }

    private static void addServerAuthErrorEnabledListener(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        mtConfigClient.addListener(OCTO_PROVIDER_AUTH_ERROR_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftServerGlobalConfig.setEnableAuthErrorLog(!"false".equals(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void addClientAuthEnableListener(final ThriftClientGlobalConfig thriftClientGlobalConfig) {
        mtConfigClient.addListener(OCTO_INVOKER_AUTH_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftClientGlobalConfig.setEnableAuthByMcc("true".equals(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void getClientAuthEnableFromMcc(ThriftClientGlobalConfig thriftClientGlobalConfig) {
        String value = mtConfigClient.getValue(OCTO_INVOKER_AUTH_ENABLE);
        if (StringUtils.isNotEmpty(value)) {
            thriftClientGlobalConfig.setEnableAuthByMcc("true".equals(value));
        }
    }

    private static void addServerGrayAuthEnabledListener(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        mtConfigClient.addListener(OCTO_PROVIDER_GRAY_AUTH, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftServerGlobalConfig.setEnableGrayAuthByMcc("true".equals(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void getServerGrayAuthEnabledFromMcc(ThriftServerGlobalConfig thriftServerGlobalConfig) {
        String value = mtConfigClient.getValue(OCTO_PROVIDER_GRAY_AUTH);
        if (value != null) {
            thriftServerGlobalConfig.setEnableGrayAuthByMcc("true".equals(value));
        }
    }

    private static void addServerAuthDebugLogEnabledListener(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        mtConfigClient.addListener(OCTO_PROVIDER_AUTH_DEBUG_LOG_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftServerGlobalConfig.setEnableAuthDebugLog("true".equals(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void getServerAuthDebugLogEnabledFromMcc(ThriftServerGlobalConfig thriftServerGlobalConfig) {
        String value = mtConfigClient.getValue(OCTO_PROVIDER_AUTH_DEBUG_LOG_ENABLE);
        if (value != null) {
            thriftServerGlobalConfig.setEnableAuthDebugLog("true".equals(value));
        }
    }

    public static void initServerConfigListener() {
        if (mtConfigClientInitiated && !serverCfgInitiated) {
            addFlowCopyConfigListener();
            // 多服务场景下, 避免重复注册监听
            serverCfgInitiated = true;
        }
    }

    private static void addClientFaultInjectEnabledListener(final ThriftClientGlobalConfig thriftClientGlobalConfig) {
        mtConfigClient.addListener(OCTO_INVOKER_FAULTINJECT_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftClientGlobalConfig.setEnableFaultInject("true".equals(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void getClientFaultInjectEnabledFromMcc(ThriftClientGlobalConfig thriftClientGlobalConfig) {
        String faultInjectEnabled = mtConfigClient.getValue(OCTO_INVOKER_FAULTINJECT_ENABLE);
        if (faultInjectEnabled != null) {
            thriftClientGlobalConfig.setEnableFaultInject("true".equals(faultInjectEnabled));
        }
    }

    private static void addServerAuthEnabledListener(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        mtConfigClient.addListener(OCTO_PROVIDER_AUTH_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftServerGlobalConfig.setEnableAuth(Boolean.valueOf(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void addServerMtraceEnabledListener(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        mtConfigClient.addListener(OCTO_PROVIDER_MTRACE_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftServerGlobalConfig.setEnableMtrace(Boolean.valueOf(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void addServerCatEnabledListener(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        mtConfigClient.addListener(OCTO_PROVIDER_CAT_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftServerGlobalConfig.setEnableCat(Boolean.valueOf(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void addServerLimitEnabledListener(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        mtConfigClient.addListener(OCTO_PROVIDER_LIMIT_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftServerGlobalConfig.setEnableLimit(Boolean.valueOf(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void getServerAuthEnabledFromMcc(ThriftServerGlobalConfig thriftServerGlobalConfig) {
        String serverAuthEnabled = mtConfigClient.getValue(OCTO_PROVIDER_AUTH_ENABLE);
        if (serverAuthEnabled != null) {
            thriftServerGlobalConfig.setEnableAuth(Boolean.valueOf(serverAuthEnabled));
            logger.info("read config[key:{}, value:{}]", OCTO_PROVIDER_AUTH_ENABLE, serverAuthEnabled);
        }
    }

    private static void getServerMtraceEnabledFromMcc(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        String serverMtraceEnabled = mtConfigClient.getValue(OCTO_PROVIDER_MTRACE_ENABLE);
        if (serverMtraceEnabled != null) {
            thriftServerGlobalConfig.setEnableMtrace(Boolean.valueOf(serverMtraceEnabled));
            logger.info("read config[key:{}, value:{}]", OCTO_PROVIDER_MTRACE_ENABLE, serverMtraceEnabled);
        }
    }

    private static void getServerCatEnabledFromMcc(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        String serverCatEnabled = mtConfigClient.getValue(OCTO_PROVIDER_CAT_ENABLE);
        if (serverCatEnabled != null) {
            thriftServerGlobalConfig.setEnableCat(Boolean.valueOf(serverCatEnabled));
            logger.info("read config[key:{}, value:{}]", OCTO_PROVIDER_CAT_ENABLE, serverCatEnabled);
        }
    }

    private static void addClientMtraceEnabledListener(final ThriftClientGlobalConfig thriftClientGlobalConfig) {
        mtConfigClient.addListener(OCTO_INVOKER_MTRACE_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftClientGlobalConfig.setEnableMtrace(Boolean.valueOf(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void addClientCatEnabledListener(final ThriftClientGlobalConfig thriftClientGlobalConfig) {
        mtConfigClient.addListener(OCTO_INVOKER_CAT_ENABLE, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (newValue != null) {
                    thriftClientGlobalConfig.setEnableCat(Boolean.valueOf(newValue));
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                }
            }
        });
    }

    private static void getClientMtraceEnabledFromMcc(ThriftClientGlobalConfig thriftClientGlobalConfig) {
        String clientMtraceEnabled = mtConfigClient.getValue(OCTO_INVOKER_MTRACE_ENABLE);
        if (clientMtraceEnabled != null) {
            thriftClientGlobalConfig.setEnableMtrace(Boolean.valueOf(clientMtraceEnabled));
            logger.info("read config[key:{}, value:{}]", OCTO_INVOKER_MTRACE_ENABLE, clientMtraceEnabled);
        }
    }

    private static void getClientCatEnabledFromMcc(ThriftClientGlobalConfig thriftClientGlobalConfig) {
        String clientCatEnabled = mtConfigClient.getValue(OCTO_INVOKER_CAT_ENABLE);
        if (clientCatEnabled != null) {
            thriftClientGlobalConfig.setEnableCat(Boolean.valueOf(clientCatEnabled));
            logger.info("read config[key:{}, value:{}]", OCTO_INVOKER_CAT_ENABLE, clientCatEnabled);
        }
    }

    private static void addFlowCopyConfigListener() {
        String flowCopyCfgStr = MtConfigUtil.getMtConfigValue(OCTO_PROVIDER_FLOWCOPY);
        FlowCopyTask.initFlowCopyCfg(flowCopyCfgStr);

        MtConfigUtil.addMtConfigListener(MtConfigUtil.OCTO_PROVIDER_FLOWCOPY, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                if (oldValue == null || !StringUtils.deleteWhitespace(oldValue).equals(StringUtils.deleteWhitespace(newValue))) {
                    logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                    FlowCopyTask.changeFlowCopyCfg(newValue);
                } else {
                    logger.info("config[key:{}] changed from {} to {}, but no diff after delete whitespace", key, oldValue, newValue);
                }
            }
        });
    }

    private static void getServerLimitEnabledFromMcc(final ThriftServerGlobalConfig thriftServerGlobalConfig) {
        String serverLimitEnabled = mtConfigClient.getValue(OCTO_PROVIDER_LIMIT_ENABLE);
        if (serverLimitEnabled != null) {
            thriftServerGlobalConfig.setEnableLimit(Boolean.valueOf(serverLimitEnabled));
            logger.info("read config[key:{}, value:{}]", OCTO_PROVIDER_LIMIT_ENABLE, serverLimitEnabled);
        }
    }

    public static MtConfigClient getMtConfigClient() {
        return mtConfigClient;
    }

    public static String getMtConfigValue(String key) {
        return mtConfigClient.getValue(key);
    }

    public static boolean isMtConfigClientInitiated() {
        return mtConfigClientInitiated;
    }

    public static void addMtConfigListener(String key, IConfigChangeListener configChangeListener) {
        mtConfigClient.addListener(key, configChangeListener);
    }

    public static void removeMtConfigListener(String key, IConfigChangeListener configChangeListener) {
        mtConfigClient.removeListener(key, configChangeListener);
    }

    public static String getAppName() {
        return appName;
    }
}
