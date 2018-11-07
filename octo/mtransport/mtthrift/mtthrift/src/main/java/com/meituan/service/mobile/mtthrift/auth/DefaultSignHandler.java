package com.meituan.service.mobile.mtthrift.auth;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.meituan.service.inf.kms.client.KmsAuthDataSource;
import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import com.meituan.service.mobile.mtthrift.util.MtConfigUtil;
import com.sankuai.inf.patriot.token.SignatureType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 框架默认提供的签名处理类
 * 为了做框架鉴权兼容，需发送旧信息与新鉴权信息
 */
public class DefaultSignHandler implements ISignHandler, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSignHandler.class);

    private static final String METHOD_GET_LOCAL_TOKENMAP = "getLocalTokenMap";

    private static Map<String, Map<String, String>> globalLocalAppkeyTokenMap = new ConcurrentHashMap<String, Map<String, String>>();
    private static Map<String, Object> globalAuthDataSourceMap = new ConcurrentHashMap<String, Object>();

    private Map<String, String> localAppkeyTokenMap = new HashMap<String, String>();

    private Object authDataSource;

    private static ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool
            (1, new MTDefaultThreadFactory("DefaultSignHandler"));

    private int period = -1;

    private static final int DEFAULT_PERIOD = 60;

    // 标识自己鉴权域，从app.properties读取不安全
    private String namespace = MtConfigUtil.getAppName();

    private String authDataSourceAppkey = "";

    static {
        scheduExec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ThriftClientGlobalConfig.isEnableAuth()) {
                        getGlobalAuthData();
                    }
                } catch (Exception e) {
                    logger.error("DefaultSignHandler getAuthData error, {}:{}",
                            e.getClass().getName(), e.getMessage());
                }
            }
        }, 1, DEFAULT_PERIOD, TimeUnit.SECONDS);
    }

    public DefaultSignHandler() {
    }

    public DefaultSignHandler(Map<String, String> localAppkeyTokenMap) {
        this.localAppkeyTokenMap = localAppkeyTokenMap;
    }

    @Override
    public SignMetaData sign(SignMetaData signMetaData) {
        if (globalLocalAppkeyTokenMap.get(authDataSourceAppkey) != null) {
            this.localAppkeyTokenMap = globalLocalAppkeyTokenMap.get(authDataSourceAppkey);
        }

        String appkey = signMetaData.getAppkey();

        String uniformSignInfo = null;
        if (AuthInstanceFactory.singleInstance(namespace) != null) {
            // 这样是否可以与Pegion框架兼容，是否需要添加上下文
            try {
                uniformSignInfo = AuthInstanceFactory.singleInstance(namespace).getTokenSignature(signMetaData.getRemoteAppkey(), SignatureType.HMAX_SHA1);
                if (StringUtils.isNotEmpty(uniformSignInfo)) {
                    AuthUtil.setUniformSignContext(uniformSignInfo);
                }
            } catch (Exception e) {
                // kms异常不处理，不发新签名
            }
        }

        if (localAppkeyTokenMap != null && localAppkeyTokenMap.size() > 0) {
            String token = localAppkeyTokenMap.get(appkey);
            if (appkey != null && token != null) {
                String signature = AuthUtil.hmacSHA1(token, appkey);
                signMetaData.setSignature(signature);
                AuthUtil.setRequestContext(signMetaData);
            }
        }

        if (ThriftServerGlobalConfig.isEnableAuthDebugLog()) {
            logger.info("Sign-SignMetaData: {}", signMetaData.toString());
            logger.info("Sign-LocalAppkeyTokenMap: {}", localAppkeyTokenMap.toString());
            logger.info("Uniform-Sign-info: {}", uniformSignInfo);
            logger.info("Sign-SignMetaData: " + signMetaData.toString());
            logger.info("Sign-LocalAppkeyTokenMap: " + localAppkeyTokenMap.toString());
            logger.info("Uniform-Sign-info: " + uniformSignInfo);

            Transaction signTransaction = Cat.newTransaction("OctoSign", signMetaData.getRemoteAppkey());
            Cat.logEvent("OctoSign.metaData", signMetaData.toString());
            Cat.logEvent("OctoSign.tokenMap", localAppkeyTokenMap.toString());
            Cat.logEvent("OctoSign.uniformSignInfo", uniformSignInfo);
            signTransaction.setSuccessStatus();
            signTransaction.complete();
        }
        return signMetaData;
    }

    public Map<String, String> getLocalAppkeyTokenMap() {
        return localAppkeyTokenMap;
    }

    public void setLocalAppkeyTokenMap(Map<String, String> localAppkeyTokenMap) {
        this.localAppkeyTokenMap = localAppkeyTokenMap;
    }

    public Object getAuthDataSource() {
        return authDataSource;
    }

    public void setAuthDataSource(Object authDataSource) {
        this.authDataSource = authDataSource;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public static void getGlobalAuthData() {
        try {
            for (Map.Entry<String, Object> entry : globalAuthDataSourceMap.entrySet()) {
                String appkey = entry.getKey();
                Object authDataSource = entry.getValue();

                if (authDataSource != null) {
                    Method getLocalTokenMapMethod = authDataSource.getClass().getDeclaredMethod(METHOD_GET_LOCAL_TOKENMAP, new Class[]{});
                    Map<String, String> map = (Map<String, String>) getLocalTokenMapMethod.invoke(authDataSource, new Object[]{});
                    if (map != null) {
                        globalLocalAppkeyTokenMap.put(appkey, map);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("getStaticAuthData error, {}:{}",
                    e.getClass().getName(), e.getMessage());
        }
    }

    public void getAuthData() {
        try {
            if (authDataSource != null) {
                Method getLocalTokenMapMethod = authDataSource.getClass().getDeclaredMethod(METHOD_GET_LOCAL_TOKENMAP, new Class[]{});
                Map<String, String> map = (Map<String, String>) getLocalTokenMapMethod.invoke(authDataSource, new Object[]{});
                if (map != null) {
                    setLocalAppkeyTokenMap(map);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (period <= 0) {
            period = DEFAULT_PERIOD;
        }

        if (authDataSource != null) {
            if (authDataSource instanceof KmsAuthDataSource) {
                String appkey = ((KmsAuthDataSource) authDataSource).getAppkey();
                if (appkey != null) {
                    globalAuthDataSourceMap.put(appkey, authDataSource);
                    this.authDataSourceAppkey = appkey;
                }
                if (ThriftServerGlobalConfig.isEnableAuth()) {
                    getGlobalAuthData();
                }
            } else {
                //为了兼容用户自己实现的AuthDataSource，后续会直接删掉
                scheduExec.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (ThriftClientGlobalConfig.isEnableAuth()) {
                                getAuthData();
                            }
                        } catch (Exception e) {
                            logger.error("DefaultSignHandler getAuthData error, {}:{}",
                                    e.getClass().getName(), e.getMessage());
                        }
                    }
                }, 1, DEFAULT_PERIOD, TimeUnit.SECONDS);

                if (ThriftClientGlobalConfig.isEnableAuth()) {
                    getAuthData();
                }
            }
        }

        long before = System.nanoTime();
        AuthInstanceFactory.singleInstance(namespace);
        long cost = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - before);
        logger.info("统一鉴权初始化耗时{}ms, namespace:{}", cost, namespace);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
