package com.meituan.service.mobile.mtthrift.auth;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.meituan.service.inf.kms.client.KmsAuthDataSource;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import com.meituan.service.mobile.mtthrift.util.MtConfigUtil;
import com.sankuai.inf.patriot.client.AuthResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.meituan.service.mobile.mtthrift.auth.OctoAuthResult.APPKEY_EMPTY_DENY;
import static com.meituan.service.mobile.mtthrift.auth.OctoAuthResult.NOT_AUTH_METHOD_PASS;
import static com.meituan.service.mobile.mtthrift.auth.OctoAuthResult.OLD_AUTH_PASS;
import static com.meituan.service.mobile.mtthrift.auth.OctoAuthResult.OTHER;
import static com.meituan.service.mobile.mtthrift.auth.OctoAuthResult.SIGN_NOT_EQUAL_DENY;
import static com.meituan.service.mobile.mtthrift.auth.OctoAuthResult.TOKEN_EMPTY_DENY;
import static com.meituan.service.mobile.mtthrift.auth.OctoAuthResult.TOKEN_MAP_EMPTY_DENY;
import static com.meituan.service.mobile.mtthrift.auth.OctoAuthResult.UNI_AUTH_PASS;
import static com.meituan.service.mobile.mtthrift.auth.OctoAuthResult.WHITELIST_PASS;

/**
 * 框架默认提供的连接层鉴权（服务粒度），可设置authType为请求鉴权（方法粒度）
 */
public class DefaultAuthHandler implements IAuthHandler, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAuthHandler.class);

    private static final String METHOD_GET_APPKEY_TOKENMAP = "getAppkeyTokenMap";
    private static final String METHOD_GET_APPKEY_WHITELIST = "getAppkeyWhitelist";
    private static final String METHOD_GET_METHOD_APPKEY_TOKENMAP = "getMethodAppkeyTokenMap";

    private static Map<String, Object> globalAuthDataSourceMap = new ConcurrentHashMap<String, Object>();
    private static Map<String, Map<String, String>> globalAppkeyTokenMap = new ConcurrentHashMap<String, Map<String, String>>();
    private static Map<String, Set<String>> globalAppkeyWhiteList = new ConcurrentHashMap<String, Set<String>>();
    private static Map<String, Map<String, Map<String, String>>> globalMethodAppkeyTokenMap = new ConcurrentHashMap<String, Map<String, Map<String, String>>>();

    private Map<String, String> appkeyTokenMap = new HashMap<String, String>();
    private Set<String> appkeyWhitelist = new HashSet<String>();
    private Map<String, Map<String, String>> methodAppkeyTokenMap = new HashMap<String, Map<String, String>>();

    private AuthType authType = AuthType.channelAuth;

    private Object authDataSource;
    private int period = -1;
    private String authDataSourceAppkey = "";

    // 标识自己鉴权域，从app.properties读取不安全
    private String namespace = MtConfigUtil.getAppName();

    private static ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool
            (1, new MTDefaultThreadFactory("DefaultAuthHandler"));

    private static final int DEFAULT_PERIOD = 60;

    static {
        scheduExec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ThriftServerGlobalConfig.isEnableAuth()) {
                        getGlobalAuthData();
                    }
                } catch (Exception e) {
                    logger.error("DefaultAuthHandler getAuthData error, {}:{}",
                            e.getClass().getName(), e.getMessage());
                }
            }
        }, 1, DEFAULT_PERIOD, TimeUnit.SECONDS);
    }

    public DefaultAuthHandler() {

    }

    @Override
    public boolean auth(AuthMetaData authMetaData) {
        if (globalAppkeyTokenMap.get(authDataSourceAppkey) != null) {
            this.appkeyTokenMap = globalAppkeyTokenMap.get(authDataSourceAppkey);
        }

        if (globalAppkeyWhiteList.get(authDataSourceAppkey) != null) {
            this.appkeyWhitelist = globalAppkeyWhiteList.get(authDataSourceAppkey);
        }

        if (globalMethodAppkeyTokenMap.get(authDataSourceAppkey) != null) {
            this.methodAppkeyTokenMap = globalMethodAppkeyTokenMap.get(authDataSourceAppkey);
        }

        String appkey = authMetaData.getClientAppkey();
        Transaction authTransaction = null;
        if (ThriftServerGlobalConfig.isEnableAuthDebugLog()) {
            authTransaction = Cat.newTransaction("OctoAuth", appkey);
        }
        catLogEventIfDebugEnabled("OctoAuth.authType", authType.toString());
        catLogEventIfDebugEnabled("OctoAuth.clientAppkey", appkey);
        catLogEventIfDebugEnabled("OctoAuth.clientIp", authMetaData.getClientIp());

        OctoAuthResult authResult = null;
        try {
            if (appkeyWhitelist != null && appkey != null && appkeyWhitelist.contains(appkey)) {
                authResult = WHITELIST_PASS;
                catLogEventIfDebugEnabled("OctoAuth.pass", authResult.toString());
                return true;
            }

            if (AuthType.channelAuth.equals(authType)) {
                if (StringUtils.isNotEmpty(authMetaData.getUniformSignInfo())) {
                    try {
                        AuthResponse identifyResponse = AuthInstanceFactory.singleInstance(namespace).authIdentity(authMetaData.getUniformSignInfo(), authMetaData.getClientAppkey());
                        if (identifyResponse != null && identifyResponse.isSuccess()) {
                            authResult = UNI_AUTH_PASS;
                            catLogEventIfDebugEnabled("OctoAuth.pass", authResult.toString());
                            return true;
                        }
                    } catch (Exception e) {
                        // kms异常不处理，继续走老鉴权
                    }
                }
                String signature = authMetaData.getSignature();
                if (appkeyTokenMap != null && appkeyTokenMap.size() > 0) {
                    String token = appkeyTokenMap.get(appkey);
                    if (token != null && appkey != null) {
                        String sign = AuthUtil.hmacSHA1(token, appkey);
                        if (sign != null && sign.equals(signature)) {
                            authResult = OLD_AUTH_PASS;
                            catLogEventIfDebugEnabled("OctoAuth.pass", authResult.toString());
                            return true;
                        } else {
                            authResult = SIGN_NOT_EQUAL_DENY;
                            catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                            catLogEventIfDebugEnabled("OctoAuth.clientSignature", signature);
                            catLogEventIfDebugEnabled("OctoAuth.serverSignature", sign);
                        }
                    } else if (token == null){
                        authResult = TOKEN_EMPTY_DENY;
                        catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                    } else {
                        authResult = APPKEY_EMPTY_DENY;
                        catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                    }
                } else {
                    authResult = TOKEN_MAP_EMPTY_DENY;
                    catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                }
            } else if (AuthType.requestAuth.equals(authType)) {
                // 先校验新鉴权是否通过，不通过再校验下老的鉴权逻辑，以便数据未做双向同步及迁移也可以发布版本
                if (StringUtils.isNotEmpty(authMetaData.getUniformSignInfo())) {
                    try {
                        AuthResponse identifyResponse = AuthInstanceFactory.singleInstance(namespace).authIdentity(authMetaData.getUniformSignInfo(), authMetaData.getClientAppkey());
                        if (identifyResponse != null && identifyResponse.isSuccess()) {
                            AuthResponse acResponseForServiceMethod = AuthInstanceFactory.singleInstance(namespace).authAccessControl(appkey, authMetaData.getClientIp(), authMetaData.getSimpleServiceName(), authMetaData.getMethodName());
                            if (acResponseForServiceMethod != null && acResponseForServiceMethod.isSuccess()) {
                                authResult = UNI_AUTH_PASS;
                                catLogEventIfDebugEnabled("OctoAuth.pass", authResult.toString());
                                return true;
                            }
                            // 历史原因，旧鉴权支持用户自己输入方法名，这里做下兼容
                            AuthResponse acResponseForMethod = AuthInstanceFactory.singleInstance(namespace).authAccessControl(appkey, authMetaData.getClientIp(), authMetaData.getMethodName());
                            if (acResponseForMethod != null && acResponseForMethod.isSuccess()) {
                                authResult = UNI_AUTH_PASS;
                                catLogEventIfDebugEnabled("OctoAuth.pass", authResult.toString());
                                return true;
                            }
                            if (acResponseForMethod != null && acResponseForServiceMethod != null) {
                                authMetaData.setAuthCode(acResponseForServiceMethod.getErrType() * 100 + acResponseForMethod.getErrType());
                            }
                            //                        走了两次新鉴权，暂时设置个特殊的错误码
                            //                        else if (accessControlResponse != null) {
                            //                            authMetaData.setAuthCode(accessControlResponse.getErrType());
                            //                            //todo 新鉴权不通过时校验下老的鉴权逻辑
                            //                            //return false;
                            //                        }
                        } else if (identifyResponse != null) {
                            // 设置身份认证失败错误码
                            authMetaData.setAuthCode(identifyResponse.getErrType());
                        }
                    } catch (Exception e) {
                        // kms异常不处理，继续走老鉴权
                    }
                }

                String signature = authMetaData.getSignature();
                String serviceName = authMetaData.getSimpleServiceName();
                String methodName = authMetaData.getMethodName();
                String spanName = serviceName + "." + methodName;

                catLogEventIfDebugEnabled("OctoAuth.span", spanName);
                catLogEventIfDebugEnabled("OctoAuth.service", serviceName);
                catLogEventIfDebugEnabled("OctoAuth.method", methodName);

                if (methodAppkeyTokenMap != null && methodAppkeyTokenMap.size() > 0) {
                    Map<String, String> tokenMap = methodAppkeyTokenMap.get(spanName);
                    if (tokenMap == null) {
                        tokenMap = methodAppkeyTokenMap.get(methodName);
                    }
                    if (tokenMap != null && tokenMap.size() > 0) {
                        String token = tokenMap.get(appkey);
                        if (token != null && appkey != null) {
                            String sign = AuthUtil.hmacSHA1(token, appkey);
                            if (sign != null && sign.equals(signature)) {
                                authResult = OLD_AUTH_PASS;
                                catLogEventIfDebugEnabled("OctoAuth.pass", authResult.toString());
                                return true;
                            } else {
                                authResult = SIGN_NOT_EQUAL_DENY;
                                catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                                catLogEventIfDebugEnabled("OctoAuth.clientSignature", signature);
                                catLogEventIfDebugEnabled("OctoAuth.serverSignature", sign);
                            }
                        } else if (token == null){
                            authResult = TOKEN_EMPTY_DENY;
                            catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                        } else {
                            authResult = APPKEY_EMPTY_DENY;
                            catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                        }
                    } else {
                        authResult = NOT_AUTH_METHOD_PASS;
                        catLogEventIfDebugEnabled("OctoAuth.pass", authResult.toString());
                        return true;
                    }
                } else if (appkeyTokenMap != null && appkeyTokenMap.size() > 0) {
                    String token = appkeyTokenMap.get(appkey);
                    if (token != null && appkey != null) {
                        String sign = AuthUtil.hmacSHA1(token, appkey);
                        if (sign != null && sign.equals(signature)) {
                            authResult = OLD_AUTH_PASS;
                            catLogEventIfDebugEnabled("OctoAuth.pass", authResult.toString());
                            return true;
                        } else {
                            authResult = SIGN_NOT_EQUAL_DENY;
                            catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                            catLogEventIfDebugEnabled("OctoAuth.clientSignature", signature);
                            catLogEventIfDebugEnabled("OctoAuth.serverSignature", sign);
                        }
                    } else if (token == null){
                        authResult = TOKEN_EMPTY_DENY;
                        catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                    } else {
                        authResult = APPKEY_EMPTY_DENY;
                        catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                    }
                } else {
                    authResult = TOKEN_MAP_EMPTY_DENY;
                    catLogEventIfDebugEnabled("OctoAuth.deny", authResult.toString());
                }
            }

            if (ThriftServerGlobalConfig.isEnableAuthDebugLog()) {
                logger.info("Auth-AuthMetaData: " + authMetaData.toString());
                logger.info("Auth-AuthType: " + authType.toString());
                logger.info("Auth-AppkeyWhitelist: " + appkeyWhitelist.toString());
                logger.info("Auth-AppkeyTokenMap: " + appkeyTokenMap.toString());
                logger.info("Auth-MethodAppkeyTokenMap: " + methodAppkeyTokenMap.toString());
                logger.info("Uniform-Auth-info: " + authMetaData.getUniformSignInfo());
                logger.info("Auth-Code: " + authMetaData.getAuthCode());

                catLogEventIfDebugEnabled("OctoAuth.pass", "denied");

                authTransaction.addData("Auth-AuthMetaData", authMetaData.toString());
                authTransaction.addData("Auth-AuthType", authType.toString());
                authTransaction.addData("Auth-AppkeyWhitelist", appkeyWhitelist.toString());
                authTransaction.addData("Auth-AppkeyTokenMap", appkeyTokenMap.toString());
                authTransaction.addData("Auth-MethodAppkeyTokenMap", methodAppkeyTokenMap.toString());
                authTransaction.addData("Uniform-Auth-info", authMetaData.getUniformSignInfo());
                authTransaction.addData("Auth-Code", authMetaData.getAuthCode());
            }
        } finally {
            if (authResult == null) {
                authResult = OTHER;
            }

            if (ThriftServerGlobalConfig.isEnableCat()) {
                StringBuilder authResultEvent = new StringBuilder();
                authResultEvent.append("result=").append(authResult);
                authResultEvent.append("&clientAppkey=").append(authMetaData.getClientAppkey());
                authResultEvent.append("&clientIp=").append(authMetaData.getClientIp());
                authResultEvent.append("&service=").append(authMetaData.getSimpleServiceName());
                authResultEvent.append("&method=").append(authMetaData.getMethodName());
                Cat.logEvent("OctoAuth.result", authResultEvent.toString());
            }

            if (authTransaction != null) {
                authTransaction.setSuccessStatus();
                authTransaction.complete();
            }

        }
        return false;
    }

    private static void catLogEventIfDebugEnabled(String type, String name) {
        if (ThriftServerGlobalConfig.isEnableAuthDebugLog()) {
            Cat.logEvent(type, name);
        }
    }

    @Override
    public AuthType getAuthType() {
        return authType;
    }

    public Map<String, String> getAppkeyTokenMap() {
        return appkeyTokenMap;
    }

    public void setAppkeyTokenMap(Map<String, String> appkeyTokenMap) {
        this.appkeyTokenMap = appkeyTokenMap;
    }

    public Set<String> getAppkeyWhitelist() {
        return appkeyWhitelist;
    }

    public void setAppkeyWhitelist(Set<String> appkeyWhitelist) {
        this.appkeyWhitelist = appkeyWhitelist;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public Map<String, Map<String, String>> getMethodAppkeyTokenMap() {
        return methodAppkeyTokenMap;
    }

    public void setMethodAppkeyTokenMap(Map<String, Map<String, String>> methodAppkeyTokenMap) {
        this.methodAppkeyTokenMap = methodAppkeyTokenMap;
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

    public void getAuthData() {
        try {
            if (authDataSource != null) {
                Method getAppkeyTokenMapMethod = authDataSource.getClass().getDeclaredMethod(METHOD_GET_APPKEY_TOKENMAP, new Class[]{});
                Map<String, String> tokenMap = (Map<String, String>) getAppkeyTokenMapMethod.invoke(authDataSource, new Object[]{});
                if (tokenMap != null) {
                    setAppkeyTokenMap(tokenMap);
                }
                Method getAppkeyWhitelistMethod = authDataSource.getClass().getDeclaredMethod(METHOD_GET_APPKEY_WHITELIST, new Class[]{});
                Set<String> whitelist = (Set<String>) getAppkeyWhitelistMethod.invoke(authDataSource, new Object[]{});
                if (whitelist != null) {
                    setAppkeyWhitelist(whitelist);
                }
                Method getMethodAppkeyTokenMapMethod = authDataSource.getClass().getDeclaredMethod(METHOD_GET_METHOD_APPKEY_TOKENMAP, new Class[]{});
                Map<String, Map<String, String>> methodTokenMap = (Map<String, Map<String, String>>) getMethodAppkeyTokenMapMethod.invoke(authDataSource, new Object[]{});
                if (methodTokenMap != null) {
                    setMethodAppkeyTokenMap(methodTokenMap);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void getGlobalAuthData() {
        try {
            for (Map.Entry<String, Object> entry : globalAuthDataSourceMap.entrySet()) {
                String appkey = entry.getKey();
                Object authDataSource = entry.getValue();
                if (authDataSource != null) {
                    Method getAppkeyTokenMapMethod = authDataSource.getClass().getDeclaredMethod(METHOD_GET_APPKEY_TOKENMAP, new Class[]{});
                    Map<String, String> tokenMap = (Map<String, String>) getAppkeyTokenMapMethod.invoke(authDataSource, new Object[]{});
                    if (tokenMap != null) {
                        globalAppkeyTokenMap.put(appkey, tokenMap);
                    }
                    Method getAppkeyWhitelistMethod = authDataSource.getClass().getDeclaredMethod(METHOD_GET_APPKEY_WHITELIST, new Class[]{});
                    Set<String> whitelist = (Set<String>) getAppkeyWhitelistMethod.invoke(authDataSource, new Object[]{});
                    if (whitelist != null) {
                        globalAppkeyWhiteList.put(appkey, whitelist);
                    }
                    Method getMethodAppkeyTokenMapMethod = authDataSource.getClass().getDeclaredMethod(METHOD_GET_METHOD_APPKEY_TOKENMAP, new Class[]{});
                    Map<String, Map<String, String>> methodTokenMap = (Map<String, Map<String, String>>) getMethodAppkeyTokenMapMethod.invoke(authDataSource, new Object[]{});
                    if (methodTokenMap != null) {
                        globalMethodAppkeyTokenMap.put(appkey, methodTokenMap);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("getStaticAuthData error, {}:{}",
                    e.getClass().getName(), e.getMessage());
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
                            if (ThriftServerGlobalConfig.isEnableAuth()) {
                                getAuthData();
                            }
                        } catch (Exception e) {
                            logger.error("DefaultAuthHandler getAuthData error, {}:{}",
                                    e.getClass().getName(), e.getMessage());
                        }
                    }
                }, 1, DEFAULT_PERIOD, TimeUnit.SECONDS);

                if (ThriftServerGlobalConfig.isEnableAuth()) {
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
