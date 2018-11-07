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
public class HulkUtil {
    private static Logger logger = LoggerFactory.getLogger(HulkUtil.class);

    private static final String getHulkSvcURL = "http://hulk.sankuai.com/api/hulk/service/query";

    public static ResultData<Set<String>> getHulkSvcAppkey() {
        ResultData<Set<String>> hulkSvcAppkeyRet = new ResultData<>();
        String result = HttpUtil.getResult(getHulkSvcURL);

        JSONObject jsonResult;
        JSONArray appkeys;
        if (StringUtils.isBlank(result) || (jsonResult = JSONObject.parseObject(result)) == null ||
                (appkeys = jsonResult.getJSONArray("appkeys")) == null) {
            logger.error("Hulk getHulkSvcAppkey return empty or parse fail, result={}.", result);
            return hulkSvcAppkeyRet.failure("Hulk获取服务Appkey, 返回为空或解析失败");
        }
        Set<String> appkeySet = new HashSet<>();
        for (int i = 0; i < appkeys.size(); i++) {
            String appkey = appkeys.getString(i).toLowerCase();
            appkeySet.add(appkey);
        }
        hulkSvcAppkeyRet.setSuccResult(appkeySet);
        return hulkSvcAppkeyRet;
    }
}
