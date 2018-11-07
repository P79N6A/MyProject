package com.sankuai.octo.msgp.utils.remote;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dianping.zebra.util.StringUtils;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.octo.msgp.utils.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by emma on 2017/6/8.
 */
public class SlbUtil {
    private static Logger logger = LoggerFactory.getLogger(SlbUtil.class);

    private final static String getSHHttpTypeSvcURL = "http://slb.dp/api/pool";

    private final static String HEADER_NAME = "Authorization";
    private final static String HEADER_VALUE = "yTe<9+={}]:m_aHY";


    public static ResultData<List<String>> getAppkeyOfSHHttpService() {
        ResultData<List<String>> appNameListResult = new ResultData<>();
        Map<String, String> param = new HashMap<>();
        param.put("page_size", String.valueOf(20000));
        String result = HttpUtil.httpGetRequest(getSHHttpTypeSvcURL, getHeader(),param);
        JSONObject jsonResult;
        if (StringUtils.isBlank(result) || (jsonResult = JSONObject.parseObject(result)) == null) {
            logger.error("Slb getAppkeyOfSHHttpService return empty or parse fail, result={}, url={}.", result);
            return appNameListResult.failure("Slb获取Http服务失败，返回为空或解析失败");
        }
        int code = jsonResult.getInteger("code");
        String msg = jsonResult.getString("msg");
        if (code != 200) {
            logger.error("Slb getAppkeyOfSHHttpService fail, code={}, msg={}", code, msg);
            return appNameListResult.failure("Slb获取Http服务失败 code=" + code + ", msg=" + msg);
        }

        List<String> appNameList = new ArrayList<>();
        JSONArray resultData = jsonResult.getJSONObject("result").getJSONArray("objects");
        for (int i = 0; i < resultData.size(); i++) {
            JSONObject obj = resultData.getJSONObject(i);
            String appName = obj.getString("pool_name");
            appNameList.add(appName);
        }
        appNameListResult.setSuccResult(appNameList);
        return appNameListResult;
    }

    private static Map<String, String> getHeader() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(HEADER_NAME, HEADER_VALUE);
        return paramMap;
    }

}
