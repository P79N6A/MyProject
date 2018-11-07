package com.sankuai.octo.msgp.service.hulk;

import com.sankuai.meituan.config.MtConfigClient;

/**
 * Created by songjianjian on 2018/7/29.
 */
public class MccHulkService {

    private static final MtConfigClient manuConfigMccClient;
    private static final MtConfigClient manuConfigMccClientBannerApi;
    static{
        manuConfigMccClient = new MtConfigClient();
        manuConfigMccClient.setModel("v2");
        manuConfigMccClient.setAppkey("com.sankuai.inf.msgp");
        manuConfigMccClient.setId("manuConfigForHulk");
        manuConfigMccClient.init();

        manuConfigMccClientBannerApi = new MtConfigClient();
        manuConfigMccClientBannerApi.setModel("v2");
        manuConfigMccClientBannerApi.setAppkey("com.sankuai.inf.hulk.bannerapi");
        manuConfigMccClientBannerApi.setId("rightConfigForHulk");
        manuConfigMccClientBannerApi.init();
    }

    public static MtConfigClient getMtConfigClient() {
        return manuConfigMccClient;
    }

    public static MtConfigClient getMtConfigClientBannerApi() {
        return manuConfigMccClientBannerApi;
    }
}
