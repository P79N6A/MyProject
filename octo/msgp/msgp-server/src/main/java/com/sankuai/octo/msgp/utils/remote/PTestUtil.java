package com.sankuai.octo.msgp.utils.remote;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dianping.zebra.util.StringUtils;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.octo.msgp.utils.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by emma on 2017/6/12.
 */
public class PTestUtil {
    private static Logger logger = LoggerFactory.getLogger(PTestUtil.class);

    private static final String getPTestSvcURL = "https://ptest.sankuai.com/api/service/appkey/list";

    public static ResultData<Set<String>> getPTestSvcAppkey() {
        ResultData<Set<String>> ptestSvcAppkeyRet = new ResultData<>();
        String result = HttpUtil.getResult(getPTestSvcURL);

        JSONObject jsonResult;
        JSONArray appkeys;
        String msg = null;
        if (StringUtils.isBlank(result) || (jsonResult = JSONObject.parseObject(result)) == null
                || jsonResult.getBoolean("isError") || (msg = jsonResult.getString("message")) != null
                || (appkeys = jsonResult.getJSONArray("data")) == null) {
            logger.error("PTest getPTestSvcAppkey return empty or parse fail, msg={} result={}.", msg, result);
            return ptestSvcAppkeyRet.failure("PTest获取服务Appkey失败");
        }
        Set<String> appkeySet = new HashSet<>();
        for (int i = 0; i < appkeys.size(); i++) {
            String appkey = appkeys.getString(i).toLowerCase();
            appkeySet.add(appkey);
        }
        ptestSvcAppkeyRet.setSuccResult(appkeySet);
        return ptestSvcAppkeyRet;
    }
}
