package com.meituan.service.mobile.mtthrift.auth;

import com.sankuai.inf.patriot.client.Auth;
import com.sankuai.inf.patriot.client.AuthBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guojidong
 * @Description: Auth 客户端单例
 * @Date 2018/6/14
 */
public class AuthInstanceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInstanceFactory.class);

    private static Map<String, Auth> authMap = new ConcurrentHashMap<String, Auth>();

    public static Auth singleInstance(String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            throw new InvalidParameterException("IAuthHandler 必须指定appkey");
        }
        Auth currAuth = authMap.get(namespace);
        if (currAuth == null) {
            synchronized (AuthInstanceFactory.class) {
                currAuth = authMap.get(namespace);
                if (currAuth == null) {
                    try {
                        currAuth = new AuthBuilder(namespace).build();
                        authMap.put(namespace, currAuth);
                    } catch (Exception e) {
                        // todo 是否考虑添加一个默认全部通过的
                        LOGGER.error("Auth build err ", e);
                    }
                }
            }
        }
        return currAuth;
    }
}
