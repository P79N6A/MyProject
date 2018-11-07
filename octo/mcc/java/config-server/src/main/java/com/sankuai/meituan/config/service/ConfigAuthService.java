package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.model.PropertyValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.sankuai.meituan.config.model.Setting.AUTH_TOKEN;
import static com.sankuai.meituan.config.model.Setting.ENABLE_AUTH;

/**
 * Created by liangchen on 2017/11/21.
 */
@Service
public class ConfigAuthService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigAuthService.class);

    @Resource
    ConfigNodeService configNodeService;

        private static final int FALSEVERSION = -200000;


    public boolean auth(String token, String path) {
        if (StringUtils.isNotEmpty(path)) {
            Stat stat = new Stat();
            stat.setVersion(FALSEVERSION);
            Map<String, PropertyValue> zkDataMap = new HashMap<String, PropertyValue>();
            try {
                zkDataMap = configNodeService.getSettingDataMap(path, stat);
            }catch (Exception e) {
                LOG.error("获取config_setting鉴权数据异常", e);
                return false;//get失败，不允许通过
            }
            if (FALSEVERSION == stat.getVersion()) {
                return false;//get失败，不允许通过
            }
            //检查是否开启鉴权
            boolean open = zkDataMap.containsKey(ENABLE_AUTH) ? Boolean.parseBoolean(zkDataMap.get(ENABLE_AUTH).getValue()) : false ;
            if (open) {
                String serverSignature = zkDataMap.containsKey(AUTH_TOKEN) ? zkDataMap.get(AUTH_TOKEN).getValue() : null;
                if (null == serverSignature || StringUtils.isEmpty(token)) {
                    LOG.warn("鉴权未通过，未传入token或未创建token, 创建token请进入octo 服务运营->配置管理->设置，token={}, path={}", token, path);
                    return false;
                } else {
                    if (StringUtils.equals(token, serverSignature)) {
                        return true;//开鉴权，认证通过
                    } else {
                        LOG.warn("鉴权未通过，不是服务负责人，token={}, path={}", token, path);
                        return false;
                    }

                }
            } else {
                return true; //未开鉴权直接通过
            }
        } else {
            LOG.warn("鉴权未通过，参数不合法，token={}, path={}", token, path);
            return false;
        }
    }

}
