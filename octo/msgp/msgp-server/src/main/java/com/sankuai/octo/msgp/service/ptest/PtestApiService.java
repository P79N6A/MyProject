package com.sankuai.octo.msgp.service.ptest;

import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.CapacityResult;

/**
 * 与ptest合作
 */
public class PtestApiService {

    private final static String host_url = CommonHelper.isOffline() ? "https://ptest-staging.sankuai.com" : "https://ptest.sankuai.com";


    public static CapacityResult getCapacity(String appkey) {
        String param_url = "/api/service/capacity?appkey="+appkey;
        String str_result = HttpUtil.getResult(host_url + param_url);
        CapacityResult result = JsonHelper.toObject(str_result, CapacityResult.class);
        return result;
    }
}
