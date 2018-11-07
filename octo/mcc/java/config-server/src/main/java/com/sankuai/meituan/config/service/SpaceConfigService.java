package com.sankuai.meituan.config.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.sankuai.meituan.config.constant.ParamName;
import com.sankuai.meituan.config.model.ConfigNode;
import com.sankuai.meituan.config.model.PropertyValue;
import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.meituan.config.util.ZKPathBuilder;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sankuai.meituan.config.model.Setting.*;

@Service
public class SpaceConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceConfigService.class);

    @Resource
    private ZookeeperService zookeeperService;

    @Resource
    private ConfigNodeService configNodeService;

    @Resource
    private PropertySerializeService propertySerializeService;

    public void init(String spacePath) {
        if (!hadSpaceConfig(spacePath)) {
            NodeNameUtil.checkSpacePath(spacePath);
            create(spacePath);
            List<PropertyValue> data = new ArrayList<PropertyValue>();
            data.add(getPropertyValue(ConfigKey.STRUCTVERSION, CONFIG_V2_VERSION));
            data.add(getPropertyValue(ConfigKey.CANUSETHIRDLEVEL, Boolean.FALSE.toString()));
            data.add(getPropertyValue(ConfigKey.ENABLEXMALERT, Boolean.TRUE.toString()));
            data.add(getPropertyValue(ConfigKey.ENABLECHECKVERSION, Boolean.TRUE.toString()));
            this.setConfig(spacePath, data);
        }
    }

    private ConfigKey string2ConfigKey(String key) {
        String tempKey = StringUtils.trim(key);
        if (STRUCT_VERSION.equalsIgnoreCase(tempKey)) {
            return ConfigKey.STRUCTVERSION;
        } else if (CAN_USE_THIRD_LEVEL.equalsIgnoreCase(tempKey)) {
            return ConfigKey.CANUSETHIRDLEVEL;
        } else if (ENABLE_XM_ALERT.equalsIgnoreCase(tempKey)) {
            return ConfigKey.ENABLEXMALERT;
        } else if (ENABLE_CHECK_VERSION.equalsIgnoreCase(tempKey)) {
            return ConfigKey.ENABLECHECKVERSION;
        } else if (ENABLE_AUTH.equalsIgnoreCase(tempKey)) {
            return ConfigKey.ENABLEAUTH;
        } else if (AUTH_TOKEN.equalsIgnoreCase(tempKey)) {
            return ConfigKey.AUTHTOKEN;
        } else if (ORIGIN_TOKEN.equalsIgnoreCase(tempKey)) {
            return ConfigKey.ORIGINTOKEN;
        }
        return ConfigKey.OTHER;
    }

    private PropertyValue getPropertyValue(ConfigKey type, String value) {
        String key = "";
        String comment = "";
        switch (type) {
            case STRUCTVERSION:
                key = STRUCT_VERSION;
                comment = "App配置的结构版本,结构版本的标识";
                break;
            case CANUSETHIRDLEVEL:
                key = CAN_USE_THIRD_LEVEL;
                comment = "改应用是否可以自定义节点结构";
                break;
            case ENABLEXMALERT:
                key = ENABLE_XM_ALERT;
                comment = "配置更改是否大象提醒";
                break;
            case ENABLECHECKVERSION:
                key = ENABLE_CHECK_VERSION;
                comment = "配置变更时,是否开启版本检测";
                break;
            case ENABLEAUTH:
                key = ENABLE_AUTH;
                comment = "通过API修改配置,是否开启鉴权";
                break;
            case AUTHTOKEN:
                key = AUTH_TOKEN;
                comment = "鉴权token密文";
                break;
            case ORIGINTOKEN:
                key = ORIGIN_TOKEN;
                comment = "鉴权token明文";
                break;
            default:
                break;
        }
        return new PropertyValue(key, value, comment);
    }

    protected void create(String spacePath) {
        zookeeperService.create(this.getConfigNodePath(spacePath),new byte[0]);
    }


    protected void setData(String spacePath, ConfigNode configNode, int version) throws Exception{
        zookeeperService.setData(this.getConfigNodePath(spacePath), configNode, version);
    }

    public void delete(String spacePath) {
        if (hadSpaceConfig(spacePath)) {
            zookeeperService.delRecurse(this.getConfigNodePath(spacePath));
        }
    }

    public boolean canAddNode(final String spacePath) {
        return checkRulesInMtConfig2(spacePath, new Function<Map<String, String>, Boolean>() {
            @Override
            public Boolean apply(Map<String, String> nodeData) {
                return !isSecondLevel(spacePath) && canUseThirdLevel(nodeData);
            }
        });
    }

    private static boolean isSecondLevel(String spacePath) {
        return StringUtils.split(spacePath, "/").length == 2;
    }

    public boolean canAddNodeInV2(final String spacePath, final boolean isSwimlaneGroup) {
        return checkRulesInMtConfig2(spacePath, new Function<Map<String, String>, Boolean>() {
            @Override
            public Boolean apply(Map<String, String> nodeData) {
                String parentPath = configNodeService.getParentPath(spacePath);
                //上层是泳道，不允许；上层是普通分组，可建立子泳道
                return (configNodeService.isSwimlaneGroup(parentPath) ? false : isFourthLevel(spacePath, isSwimlaneGroup)) || canUseThirdLevel(nodeData);
            }
        });
    }

    //   /appkey/env/gA/ga1/ga2/ga3/ga4/ga5
    public static boolean isFourthLevel(String spacePath, boolean isSwimlaneGroup) {
        return isSwimlaneGroup || StringUtils.split(spacePath, "/").length != 8;
    }

    //v1是表示是否可以使用三级结构，v2表示是否启用五级结构，即appkey/env/gA/s1下是否还能建立新节点，暂时是不能
    private static boolean canUseThirdLevel(Map<String, String> nodeData) {
        String canUseThirdLevel = nodeData.get(CAN_USE_THIRD_LEVEL);
        return StringUtils.isNotEmpty(canUseThirdLevel) && Boolean.valueOf(canUseThirdLevel).equals(Boolean.TRUE);
    }

    public boolean canDeleteNode(final String spacePath) {
        return canAddNode(spacePath);
    }

    /*public boolean canDeleteNodeInV2(final String spacePath) {
        return canAddNodeInV2(spacePath);
    }*/

    private boolean checkRulesInMtConfig2(String spacePath, Function<Map<String, String>, Boolean> function) {
        if (hadSpaceConfig(spacePath)) {
            Map<String, String> nodeData = zookeeperService.getDataMap(this.getConfigNodePath(spacePath));
            String structVersion = nodeData.get(STRUCT_VERSION);
            if (CONFIG_V2_VERSION.equals(structVersion)) {
                return BooleanUtils.toBooleanDefaultIfNull(function.apply(nodeData), true);
            }
        }
        return true;
    }

    public boolean hadSpaceConfig(String spacePath) {
        return zookeeperService.exist(this.getConfigNodePath(spacePath));
    }

    public Map<String, String> getAll(String spacePath) {
        if (!this.hadSpaceConfig(spacePath)) {
            return ImmutableMap.<String, String>builder().put(STRUCT_VERSION, CONFIG_ORI_VERSION).build();
        } else {
            return zookeeperService.getDataMap(this.getConfigNodePath(spacePath));
        }
    }

    public void setCanUseThirdLevel(String spacePath, boolean canUseThirdLevel) {
        List<PropertyValue> data = new ArrayList<PropertyValue>();
        data.add(getPropertyValue(ConfigKey.CANUSETHIRDLEVEL, Boolean.valueOf(canUseThirdLevel).toString()));
        this.setConfig(spacePath, data);
    }

    public List<PropertyValue> getData(String spacePath) {
        byte[] byteValues = zookeeperService.getData(this.getConfigNodePath(spacePath));
        return propertySerializeService.deSerializePropertyValueAsList(byteValues);
    }

    public boolean setConfig(String spacePath, String json) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        List<PropertyValue> newKVs = new ArrayList<PropertyValue>();
        JSONObject jsonObject = JSON.parseObject(json);
 //       String enableAuth = (null == jsonObject.get(ENABLE_AUTH)) ? "false" : String.valueOf(jsonObject.get(ENABLE_AUTH));
//        if (StringUtils.equals("true", enableAuth) && null != jsonObject.get(AUTH_TOKEN)) {
//            String authToken = String.valueOf(jsonObject.get(AUTH_TOKEN));
//            jsonObject.put(SECRETE_AUTH_TOKEN, AuthUtil.hmacSHA1(authToken, spacePath));
//        }
        for (Map.Entry<String, Object> item : jsonObject.entrySet()) {
            if (StringUtils.isEmpty(item.getKey())) {
                continue;
            }
            ConfigKey keyType = this.string2ConfigKey(item.getKey());
            PropertyValue pv = this.getPropertyValue(keyType, String.valueOf(item.getValue()));
            if (ConfigKey.OTHER == keyType) {
                pv.setKey(item.getKey());
            }
            newKVs.add(pv);
        }
        return setConfig(spacePath, newKVs);
    }

    public boolean getBoolSetting(String spacePath, String key, boolean defaultValue) {
        PropertyValue xmZkData = this.getSetting(spacePath, key);
        return null != xmZkData ? Boolean.parseBoolean(xmZkData.getValue()) : defaultValue;
    }

    public String getStringSetting(String spacePath, String key, String defaultValue) {
        PropertyValue xmZkData = this.getSetting(spacePath, key);
        return null == xmZkData ? defaultValue : xmZkData.getValue();
    }

    private PropertyValue getSetting(String spacePath, String key) {
        try {
            List<PropertyValue> list = this.getData(spacePath);
            for (PropertyValue item : list) {
                if (StringUtils.equals(key, item.getKey())) {
                    return item;
                }
            }
        } catch (Exception e) {
            LOGGER.error("cannot get MCC settings.", e);
        }
        return null;
    }

    private List<PropertyValue> merge2List(List<PropertyValue> A, List<PropertyValue> B) {
        List<PropertyValue> ret = new ArrayList<PropertyValue>();
        ret.addAll(A);
        boolean isExist = false;
        for (PropertyValue item : B) {
            isExist = false;
            for (PropertyValue itemA : ret) {
                if (StringUtils.equals(item.getKey(), itemA.getKey())) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                ret.add(item);
            }
        }

        return ret;
    }

    private boolean setConfig(String spacePath, List<PropertyValue> kvcs) {
        if (!this.hadSpaceConfig(spacePath)) {
            return false;
        }
        boolean isExist = false;
        List<PropertyValue> originData = this.getData(spacePath);
        List<PropertyValue> mergeData = new ArrayList<PropertyValue>();

        List<PropertyValue> unitData = merge2List(originData, kvcs);


        for (PropertyValue itemA : unitData) {
            isExist = false;
            PropertyValue tempItem = null;
            for (PropertyValue itemB : kvcs) {
                if (StringUtils.equals(itemA.getKey(), itemB.getKey())) {
                    isExist = true;
                    tempItem = itemB;
                    break;
                }
            }
            mergeData.add(isExist ? tempItem : itemA);
        }
        ConfigNode configNode = new ConfigNode();
        configNode.setData(mergeData);
        try{
            this.setData(spacePath, configNode, -1);
        }catch(Exception e){
            LOGGER.error("fail to update settings.", e);
            return false;
        }
        return true;
    }

    protected String getConfigNodePath(String spacePath) {
        String appkey = NodeNameUtil.getAppkey(spacePath);
        return ZKPathBuilder.newBuilder(ParamName.SETTING_SPACE).appendSpace(appkey).toPath();
    }

}
