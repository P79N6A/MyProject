package com.sankuai.meituan.config.model;

/**
 * Created by lhmily on 10/13/2016.
 */
public class Setting {
    public static final String STRUCT_VERSION = "structVersion";
    public static final String CONFIG_ORI_VERSION = "1";
    public static final String CONFIG_V2_VERSION = "2";
    public static final String CAN_USE_THIRD_LEVEL = "canUseThirdLevel";
    public static final String ENABLE_XM_ALERT = "enableXMAlert";
    public static final String ENABLE_CHECK_VERSION = "enableCheckVersion";
    public static final String ENABLE_SSO = "enableSSO";
    public static final String TAIR_MASTER = "tairMaster";
    public static final String ENABLE_AUTH = "enableAuth";
    public static final String AUTH_TOKEN = "authToken";
    public static final String ORIGIN_TOKEN = "originToken";

    public enum ConfigKey {
        STRUCTVERSION, CANUSETHIRDLEVEL, ENABLEXMALERT, ENABLECHECKVERSION, ENABLEAUTH, AUTHTOKEN, ORIGINTOKEN, OTHER
    }

    public enum TairMaster {
        DX, YF, GQ, OTHER
    }
}
