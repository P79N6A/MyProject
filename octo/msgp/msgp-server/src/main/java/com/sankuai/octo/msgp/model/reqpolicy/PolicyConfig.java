package com.sankuai.octo.msgp.model.reqpolicy;

/**
 * Created by songjianjian on 2018/8/1.
 */
public class PolicyConfig {
    public static final String KAPI_URL = "http://kapiserver.hulk.vip.sankuai.com";
    public static final String IMAGE_URL_OFFLINE = "http://registryapi.inf.vip.sankuai.com";
    public static final String IMAGE_URL_ONLINE = "http://registryapionline.inf.vip.sankuai.com";
    public static final String BANNERAPI_URL = "http://bannerapi.inf.vip.sankuai.com";//"";http://localhost:8090
    public static final String KAPI_SERVER_TOKEN = "OZKOihNTh5N0qrXUGBRvtKtgExKTs6H8";
    public static final String CONTENT_TYPE = "application/json";
    public static final String BANNERAPI_TOKEN = "kcEySFUg5mwLIhp6tbYvBBSAX4mLGlvO";
    public static final String CELLAR_KEY_PREFIX = "cellar.hulk.octo.manual.";
    public static final String LOCAL_APPKEY = "com.sankuai.inf.hulk.bannerapi";
    public static final String REMOTE_APPKEY_ONLINE = "com.sankuai.tair.inf.public";
    public static final String REMOTE_APPKEY_OFFLINE = "om.sankuai.tair.qa.function";
    public static final String HOST_URL_OFFLINE = "http://bannerapi.inf.dev.sankuai.com";//"http://localhost:8090";
    public static final String HOST_URL_ONLINE = "http://bannerapi.inf.vip.sankuai.com";
    public final static String APPKEY_BELONG_SRV_URL = "http://ops.vip.sankuai.com/api/v0.2/appkeys/";
    public final static String SRV_BELONG_OWT_URL = "http://ops.vip.sankuai.com/api/v0.2/srvs/";
    public final static String AUTHORIZATION_TOKEN = "Bearer 18c0b5de2f3f9a7baf5a2c9775e486ddfc2deb94";

    public static final int CONNECT_TIMEOUT = 30000;
    public static final int SOCKET_TIMEOUT = 60000;
}
