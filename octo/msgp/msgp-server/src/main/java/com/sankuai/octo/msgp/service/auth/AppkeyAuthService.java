package com.sankuai.octo.msgp.service.auth;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.auth.AccessConfig;
import com.sankuai.octo.msgp.domain.auth.AuthorizationConfig;
import com.sankuai.octo.msgp.domain.auth.PatriotAuthenticationRequest;
import com.sankuai.octo.msgp.domain.auth.PatriotAuthenticationResponse;
import com.sankuai.octo.msgp.domain.auth.PatriotAuthorizationRequest;
import com.sankuai.octo.msgp.domain.auth.PatriotAuthorizationResponse;
import com.sankuai.octo.msgp.serivce.service.ServiceAuth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppkeyAuthService {
    private static Logger LOGGER = LoggerFactory.getLogger(AppkeyAuthService.class);

    private static final Map<String, String> PATRIOT_HEADER_MAP;

    private static final String PATRIOT_HOST_OFFLINE = "http://patriot.inf.test.sankuai.com";
    private static final String PATRIOT_HOTS_ONLINE = "http://patriot.sankuai.com";
    private static final String PATRIOT_AUTH_KEY = "octo";
    private static final String PATRIOT_AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjbGllbnQiOiJvY3RvIn0.TUHVkvBrB8dLpZjKt7bvgcGqGIQa4HcpDoMYNzdXzOo";
    private static final String MSG_NAMESPACE_NOT_EXIST = "namespace not exist";

    private static final boolean IS_OFFLINE = CommonHelper.isOffline();
    private static final String PATRIOT_HOST = IS_OFFLINE ? PATRIOT_HOST_OFFLINE : PATRIOT_HOTS_ONLINE;

    static {
        PATRIOT_HEADER_MAP = new HashMap<>();
        PATRIOT_HEADER_MAP.put("auth-key", PATRIOT_AUTH_KEY);
        PATRIOT_HEADER_MAP.put("auth-token", PATRIOT_AUTH_TOKEN);
        PATRIOT_HEADER_MAP.put("Content-Type", "application/json;charset=UTF-8");
    }

    /**
     * 将服务授权列表更新到统一鉴权数据源中
     * @param appkey
     * @param env
     * @param clientAppkeys
     */
    public static void updateAppkeyAuthList(String appkey, String env, List<String> clientAppkeys) {
        Transaction transaction = Cat.newTransaction("UpdateAppkeyAuthList", appkey+"#"+env);
        Cat.logEvent("UpdateAppkeyAuthList.allAuthList", String.valueOf(clientAppkeys));
        try {
            Set<String> authenticatedClientAppkeys = new HashSet<>(clientAppkeys);
            for (List<String> apps : ServiceAuth.getSpanAuthMap(appkey, env).values()) {
                authenticatedClientAppkeys.addAll(apps);
            }

            if (updateAppkeyAuthentication(appkey, env, new ArrayList<String>(authenticatedClientAppkeys))) {
                transaction.setStatus(Message.SUCCESS);
            } else {
                transaction.setStatus("error");
            }
        } catch (Throwable e) {
            LOGGER.error("updateAppkeyAuthList", e);
        } finally {
            transaction.complete();
        }
    }

    public static void updateSpanAuth(String appkey, String env, Map<String, List<String>> authMap) {
        Transaction transaction = Cat.newTransaction("UpdateAppkeyAuthorization", appkey+"#"+env);
        try {
            Set<String> authenticatedClientAppkeys = new HashSet<>();
            for (List<String> appkeys : authMap.values()) {
                authenticatedClientAppkeys.addAll(appkeys);
            }

            List<String> authorizedClientAppkeys = new ArrayList<>(authenticatedClientAppkeys);

            List<String> originalClients = ServiceAuth.getAuthenticatedClientAppkey(appkey, env);
            authenticatedClientAppkeys.addAll(originalClients);

            updateAppkeyAuthentication(appkey, env, authenticatedClientAppkeys);

            AuthorizationConfig authorizationConfig = buildNewAuthorization(appkey, env, authorizedClientAppkeys, authMap);

            updateAppkeyAuthorization(appkey, env, authorizationConfig);
            transaction.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            transaction.setStatus(e);
            LOGGER.error("UpdateAppkeyAuthorization", e);
        } finally {
            transaction.complete();
        }
    }

    private static AuthorizationConfig buildNewAuthorization(String appkey, String env,
            List<String> clientAppkeys, Map<String, List<String>> authMap) {
        AuthorizationConfig authorizationConfig = getPatriotAuthorization(appkey, env);
        AccessConfig globalConfig = buildGlobalConfig(authMap);
        Map<String, AccessConfig> remoteAppConfig = buildRemoteAppConfig(clientAppkeys, authMap);
        authorizationConfig.setGlobalAccessControl(globalConfig);
        authorizationConfig.setRemoteAppAccessControl(remoteAppConfig);

        return authorizationConfig;
    }

    private static void updateAppkeyAuthorization(String appkey, String env, AuthorizationConfig authorizationConfig)
            throws UnsupportedEncodingException {
        String getUrl = PATRIOT_HOST + "/api/authorization/set";

        PatriotAuthorizationRequest request = new PatriotAuthorizationRequest();
        request.setNamespace(appkey);
        request.setEnv(envStr(env));
        request.setAuthorization(authorizationConfig);

        String getResult = HttpUtil.httpPostRequest(getUrl, PATRIOT_HEADER_MAP, JsonHelper.jsonStr(request));
        PatriotAuthorizationResponse getResponse = JsonHelper.toObject(getResult, PatriotAuthorizationResponse.class);
        if (!getResponse.getSuccess()) {
            throw new RuntimeException(getResponse.getMessage());
        }
    }

    private static AuthorizationConfig getPatriotAuthorization(String appkey, String env) {
        String getUrl = PATRIOT_HOST + "/api/authorization/get";

        Map<String, String> params = new HashMap<>();
        params.put("namespace", appkey);
        params.put("env", envStr(env));

        String getResult = HttpUtil.httpGetRequest(getUrl, PATRIOT_HEADER_MAP, params);
        PatriotAuthorizationResponse getResponse = JsonHelper.toObject(getResult, PatriotAuthorizationResponse.class);
        if (getResponse.getSuccess()) {
            if (getResponse.getData() != null && getResponse.getData().getAuthorization() != null) {
                return getResponse.getData().getAuthorization();
            } else {
                return new AuthorizationConfig();
            }
        } else if (MSG_NAMESPACE_NOT_EXIST.equals(getResponse.getMessage())) {
            return new AuthorizationConfig();
        } else {
            throw new RuntimeException(getResponse.getMessage());
        }
    }

    private static AccessConfig buildGlobalConfig(Map<String, List<String>> authMap) {
        AccessConfig accessConfig = new AccessConfig(1);
        for (String span : authMap.keySet()) {
            String[] levels = span.split("\\.");
            List<String> trimmedLevels = new LinkedList<>();
            for (String level : levels) {
                if (StringUtils.isNotBlank(level)) {
                    trimmedLevels.add(level);
                }
            }
            if (trimmedLevels.size() > 0) {
                String levelName = trimmedLevels.get(0);
                if (!accessConfig.getConf().containsKey(levelName)) {
                    int ac = trimmedLevels.size() > 1 ? 1 : 0;
                    accessConfig.getConf().put(levelName, new AccessConfig(ac));
                }
                AccessConfig local = accessConfig.getConf().get(levelName);
                for (int i = 1; i < trimmedLevels.size(); i++) {
                    levelName = trimmedLevels.get(i);
                    if (!local.getConf().containsKey(levelName)) {
                        local.getConf().put(levelName, new AccessConfig(1));
                    }
                    if (i == trimmedLevels.size() - 1) {
                        local.getConf().get(levelName).setAc(0);
                    }
                    local = local.getConf().get(levelName);
                }
            }
        }
        return accessConfig;
    }

    private static Map<String, AccessConfig> buildRemoteAppConfig(List<String> clientAppkeys, Map<String, List<String>> authMap) {
        Map<String, AccessConfig> remoteAppConfig = new HashMap<>();

        for(String client: clientAppkeys) {
            if (!remoteAppConfig.containsKey(client)) {
                remoteAppConfig.put(client, new AccessConfig(1));
            }
            for (Map.Entry<String, List<String>> entry : authMap.entrySet()) {
                AccessConfig appAccessConfig = remoteAppConfig.get(client);
                String span = entry.getKey();
                List<String> spanClients = entry.getValue();
                if (spanClients.contains(client)) {
                    String[] levels = span.split("\\.");
                    List<String> trimmedLevels = new LinkedList<>();
                    for (String level : levels) {
                        if (StringUtils.isNotBlank(level)) {
                            trimmedLevels.add(level);
                        }
                    }
                    if (trimmedLevels.size() > 0) {
                        String levelName = trimmedLevels.get(0);
                        if (!appAccessConfig.getConf().containsKey(levelName)) {
                            appAccessConfig.getConf().put(levelName, new AccessConfig(1));
                        }
                        appAccessConfig = appAccessConfig.getConf().get(levelName);
                        for (int i = 1; i < trimmedLevels.size(); i++) {
                            levelName = trimmedLevels.get(0);
                            if (!appAccessConfig.getConf().containsKey(levelName)) {
                                appAccessConfig.getConf().put(levelName, new AccessConfig(1));
                            }
                            if (i == trimmedLevels.size() - 1) {
                                appAccessConfig.getConf().get(levelName).setAc(1);
                            }
                            appAccessConfig = appAccessConfig.getConf().get(levelName);
                        }
                    }
                }
            }
        }
        return remoteAppConfig;
    }

    private static boolean updateAppkeyAuthentication(String appkey, String env, Collection<String> clientAppkeys) {
        String envStr = envStr(env);
        Set<String> originalAuthentications = getPatriotAuthentications(appkey, envStr);
        LOGGER.info("original authentication: " + originalAuthentications);
        Set<String> auth2Add = new HashSet<>(clientAppkeys);
        auth2Add.removeAll(originalAuthentications);
        LOGGER.info("auth2Add:" + auth2Add);
        try {
            if (!auth2Add.isEmpty()) {
                addAuthentication(appkey, env, auth2Add);
            }
        } catch (Exception e) {
            LOGGER.error("add authentications failed", e);
            return false;
        }

        Set<String> auth2Remove = new HashSet<>(originalAuthentications);
        auth2Remove.removeAll(clientAppkeys);
        LOGGER.info("auth2Remove:" + auth2Remove);
        try {
            if (!auth2Remove.isEmpty()) {
                Set<String> authAfterUpdate = removeAuthentication(appkey, env, auth2Remove);
                if (authAfterUpdate.size() != clientAppkeys.size()
                        || !authAfterUpdate.containsAll(clientAppkeys)) {
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("remove authentications failed", e);
            return false;
        }
        return true;
    }

    /**
     * 参考：https://123.sankuai.com/km/page/46720614#id-%E5%88%A0%E9%99%A4%E8%AE%A4%E8%AF%81%E5%85%B3%E7%B3%BB
     * @param appkey
     * @param env
     * @param auth2Remove
     * @return
     * @throws Exception
     */
    private static Set<String> removeAuthentication(String appkey, String env, Set<String> auth2Remove)
            throws Exception {
        PatriotAuthenticationRequest deleteAuthenticationRequest = new PatriotAuthenticationRequest();
        deleteAuthenticationRequest.setNamespace(appkey);
        deleteAuthenticationRequest.setEnv(envStr(env));
        deleteAuthenticationRequest.setAuthentication(auth2Remove);
        String deleteResult = HttpUtil.httpPostRequest(PATRIOT_HOST + "/api/authentication/delete",
                PATRIOT_HEADER_MAP, JsonHelper.jsonStr(deleteAuthenticationRequest));
        PatriotAuthenticationResponse deleteResponse = JsonHelper.toObject(deleteResult, PatriotAuthenticationResponse.class);
        LOGGER.info("delete auth response: " + deleteResponse);

        if (!deleteResponse.getSuccess()) {
            LOGGER.warn("delete authentications failed, appkey: " + appkey + ", env: " + env + ", param: " + deleteResult);
            throw new RuntimeException(deleteResponse.getMessage());
        } else {
            if (deleteResponse.getData() != null && deleteResponse.getData().getAuthentication() != null) {
                return deleteResponse.getData().getAuthentication();
            } else {
                return Collections.emptySet();
            }
        }
    }

    /**
     * 参考： https://123.sankuai.com/km/page/46720614#id-%E6%B7%BB%E5%8A%A0%E8%AE%A4%E8%AF%81%E5%85%B3%E7%B3%BB
     * @param appkey
     * @param env
     * @param auth2Add
     * @return
     * @throws Exception
     */
    private static Set<String> addAuthentication(String appkey, String env, Set<String> auth2Add)
            throws Exception {
        PatriotAuthenticationRequest addAuthenticationRequest = new PatriotAuthenticationRequest();
        addAuthenticationRequest.setNamespace(appkey);
        addAuthenticationRequest.setEnv(envStr(env));
        addAuthenticationRequest.setAuthentication(auth2Add);
        String addResult = HttpUtil.httpPostRequest(PATRIOT_HOST + "/api/authentication/add",
                PATRIOT_HEADER_MAP, JsonHelper.jsonStr(addAuthenticationRequest));
        PatriotAuthenticationResponse addResponse = JsonHelper.toObject(addResult, PatriotAuthenticationResponse.class);
        LOGGER.info("add auth response: " + addResponse);

        if (!addResponse.getSuccess()) {
            LOGGER.warn("add authentications failed, appkey: " + appkey + ", env: " + env + ", param: " + addResult);
            throw new RuntimeException(addResponse.getMessage());
        } else {
            if (addResponse.getData() != null && addResponse.getData().getAuthentication() != null) {
                return addResponse.getData().getAuthentication();
            } else {
                return Collections.emptySet();
            }
        }
    }

    /**
     * 参考：https://123.sankuai.com/km/page/46720614#id-%E8%8E%B7%E5%8F%96%E8%AE%A4%E8%AF%81%E5%85%B3%E7%B3%BB
     * @param appkey
     * @param envStr
     * @return
     */
    private static Set<String> getPatriotAuthentications(String appkey, String envStr) {
        String getUrl = PATRIOT_HOST + "/api/authentication/get";

        Map<String, String> params = new HashMap<>();
        params.put("namespace", appkey);
        params.put("env", envStr);

        String getResult = HttpUtil.httpGetRequest(getUrl, PATRIOT_HEADER_MAP, params);
        PatriotAuthenticationResponse getResponse = JsonHelper.toObject(getResult, PatriotAuthenticationResponse.class);
        if (getResponse.getSuccess()) {
            if (getResponse.getData() != null && getResponse.getData().getAuthentication() != null) {
                return getResponse.getData().getAuthentication();
            } else {
                return Collections.emptySet();
            }
        } else if (MSG_NAMESPACE_NOT_EXIST.equals(getResponse.getMessage())) {
            return Collections.emptySet();
        } else {
            throw new RuntimeException(getResponse.getMessage());
        }
    }

    private static String envStr(String env) {
        switch(env){
        case "1":
            return "test";
        case "2":
            if (IS_OFFLINE) {
                return "ppe";
            } else {
                return "staging";
            }
        case "3":
            if (IS_OFFLINE) {
                return "dev";
            } else {
                return "prod";
            }
        default:
            throw new IllegalArgumentException("unknown env value:" + env);
        }
    }
}
