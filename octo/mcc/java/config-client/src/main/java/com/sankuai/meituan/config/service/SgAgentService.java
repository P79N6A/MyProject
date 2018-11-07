package com.sankuai.meituan.config.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sankuai.meituan.config.annotation.MtConfig;
import com.sankuai.meituan.config.client.SGAgent;
import com.sankuai.meituan.config.exception.SgAgentServiceException;
import com.sankuai.meituan.config.pojo.ConfigData;
import com.sankuai.meituan.config.pojo.ConfigDataResponse;
import com.sankuai.meituan.config.util.MtConfigNameUtil;
import com.sankuai.meituan.config.util.RuntimeUtil;
import com.sankuai.sgagent.thrift.model.proc_conf_param_t;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;

public class SgAgentService {
    private static final Logger LOG = LoggerFactory.getLogger(SgAgentService.class);
    private final String appkey, env, path;
    private RetryService retryService = new RetryService();

    public SgAgentService(String appkey, String env, String path) {
        MtConfigNameUtil.checkAppkey(appkey);
        MtConfigNameUtil.checkEnv(env);
        MtConfigNameUtil.checkPath(path);
        this.appkey = appkey;
        this.env = env;
        this.path = path;
    }

    public ConfigData getConfig() {
        ConfigDataResponse response = retryService.doWithRetry(new Supplier<ConfigDataResponse>() {
            @Override
            public ConfigDataResponse get() {
                String config = SGAgent.get(appkey, env, path);
                LOG.debug("从SG Agent获取配置:[{}]", config);

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(config, ConfigDataResponse.class);
                } catch (JsonParseException e) {
                    LOG.debug("Json parse exception.", e);
                } catch (JsonMappingException e) {
                    LOG.debug("Json mapping exception.", e);
                } catch (IOException e) {
                    LOG.debug("Json io exception.", e);
                }

                return null;
            }
        }, new Predicate<ConfigDataResponse>() {
            @Override
            public boolean apply(ConfigDataResponse response) {
                return response != null && Integer.valueOf(0).equals(response.getRet());
            }
        }, new Function<ConfigDataResponse, RuntimeException>() {
            @Nullable
            @Override
            public RuntimeException apply(ConfigDataResponse response) {
                return new SgAgentServiceException(MessageFormatter.arrayFormat("无法获取配置信息, appkey:[{}], env:[{}], path:[{}], resultCode:{}, reason:{}", new Object[]{appkey, env, path, response.getRet(), response.getMsg()}).getMessage(), response.getRet(), response.getMsg());
            }
        });
        return new ConfigData(appkey, env, path, response.getData(), response.getVersion());
    }

    public void setConfig(String key, String value, String token) {
        setConfig(ImmutableMap.<String, String>builder().put(key, value).build(), token);
    }

    public void setConfig(final Map<String, String> config, String token) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String configJson = mapper.writeValueAsString(config);
            proc_conf_param_t confParam = new proc_conf_param_t();
            confParam.setAppkey(appkey)
                    .setEnv(env)
                    .setPath(path)
                    .setConf(configJson);
            if (StringUtils.isNotEmpty(token)) {
                confParam.setToken(token);
            }
            int result = SGAgent.setConfig(confParam);
            if (0 != result) {
                if (- 201004 == result) {
                    throw new SgAgentServiceException(MessageFormatter.arrayFormat("设置配置超时,有可能是网络延迟, appkey:[{}], env:[{}], path:[{}], config:[{}]", new Object[]{appkey, env, path, config}).getMessage(), result, null);
                } else {
                    throw new SgAgentServiceException(MessageFormatter.arrayFormat("无法设置配置信息, appkey:[{}], env:[{}], path:[{}], config:[{}], reason:{}", new Object[]{appkey, env, path, config, result}).getMessage(), result, null);
                }
            }
        } catch (JsonProcessingException e) {
            LOG.debug("Json processing exception.", e);
        }
    }

    public void setConfig(final Map<String, String> config) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String configJson = mapper.writeValueAsString(config);
            int result = SGAgent.setConfig(appkey, env, path, configJson);
            if (0 != result) {
                if (- 201004 == result) {
                    throw new SgAgentServiceException(MessageFormatter.arrayFormat("设置配置超时,有可能是网络延迟, appkey:[{}], env:[{}], path:[{}], config:[{}]", new Object[]{appkey, env, path, config}).getMessage(), result, null);
                } else {
                    throw new SgAgentServiceException(MessageFormatter.arrayFormat("无法设置配置信息, appkey:[{}], env:[{}], path:[{}], config:[{}], reason:{}", new Object[]{appkey, env, path, config, result}).getMessage(), result, null);
                }
            }
        } catch (JsonProcessingException e) {
            LOG.debug("Json processing exception.", e);
        }
    }

    public String getAppkey() {
        return appkey;
    }

    public String getEnv() {
        return env;
    }

    public String getPath() {
        return path;
    }

    public static long getDefaultPullPeriod() {
        return RuntimeUtil.isOnlineIp()? 500 : 2000;
    }

    public static ImmutableSet<String> getAllEvn() {
        final String ENV_PROD = "prod", ENV_STAGE = "stage", ENV_TEST = "test";
        return ImmutableSet.of(ENV_PROD, ENV_STAGE, ENV_TEST);
    }
}
