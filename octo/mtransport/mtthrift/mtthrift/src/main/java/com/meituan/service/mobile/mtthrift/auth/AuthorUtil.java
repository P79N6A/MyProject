package com.meituan.service.mobile.mtthrift.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankuai.inf.octo.mns.MnsInvoker;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Author: caojiguang@gmail.com
 * Date: 15/10/28
 * Description:
 */
public class AuthorUtil {
    private static Logger logger = LoggerFactory.getLogger(AuthorUtil.class);
    /**
     * json 串的格式:
     * {
     * "user" : "杨杰(yangjie17)",
     * "updateTime" : 1445308751,
     * "status" : 0,
     * "ips" : [ "10.4.243.208", "192.168.12.164", "10.4.242.103" ]
     * }
     *
     * @param providerAppkey
     * @return
     */
    public static boolean authoriseProvider(String providerAppkey,
            String providerIp) {
        String authorizedProviderInfo = MnsInvoker
                .getAuthorizedProviders(providerAppkey);

        return authorize(authorizedProviderInfo, providerIp);
    }

    public static boolean authoriseConsumer(String consumerAppkey,
            String providerAppkey, String consumerIp) {
        String authorizedProviderInfo = MnsInvoker.getAuthorizedConsumers(
                consumerAppkey, providerAppkey);

        return authorize(authorizedProviderInfo, consumerIp);
    }

    private static boolean authorize(String authorizedInfo, String ip) {
        if (StringUtils.isEmpty(authorizedInfo)) {
            //json为空无需验证，验证直接通过
            return true;
        }
        boolean success = true;
        ObjectMapper objectMapper = new ObjectMapper();
        Map authMap = null;
        try {
            authMap = objectMapper.readValue(authorizedInfo, Map.class);
        } catch (IOException e) {
            //json解析失败，验证不通过
            logger.error("authorize failed, exception caught", e);
            success = false;
        }

        Integer ONE = 1;
        if (authMap != null && ONE.equals(authMap.get("status")) && authMap.get("ips") != null) {
            try {
                List ipList = ((List) authMap.get("ips"));
                if (!ipList.contains(ip)) {
                    success = false;
                }
            } catch (Exception e) {
                //json解析失败，验证不通过
                logger.error("authorize failed, exception caught", e);
                success = false;
            }
        }
        return success;
    }
}