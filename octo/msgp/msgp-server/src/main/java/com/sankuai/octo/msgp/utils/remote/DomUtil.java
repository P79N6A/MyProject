package com.sankuai.octo.msgp.utils.remote;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dianping.zebra.util.StringUtils;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.octo.msgp.utils.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SRE dom.sankuai.com 获取的数据
 * Created by emma on 2017/6/6.
 */
public class DomUtil {

    private static Logger logger = LoggerFactory.getLogger(DomUtil.class);

    private static final String getBJHttpTypeSvcURL = "https://dom.sankuai.com/api/data/atomic?app=http&category=call";

    public static ResultData<List<String>> getSvrTreeOfBJHttpService(String date) {
        ResultData<List<String>> svrTreeListResult = new ResultData<>();
        String url = getBJHttpTypeSvcURL + "&begin=" + date + "&end=" + date;
        String result = HttpUtil.getResult(url);
        JSONObject jsonResult;
        if (StringUtils.isBlank(result) || (jsonResult = JSONObject.parseObject(result)) == null) {
            logger.error("Dom getSvrTreeOfBJHttpService return empty or parse fail, result={}, url={}.", result, url);
            return svrTreeListResult.failure("Dom获取Http服务失败，返回为空或解析失败");
        }
        int code = jsonResult.getInteger("code");
        if (code != 200) {
            logger.error("Dom getSvrTreeOfBJHttpService fail, code={}, url={}.", code, url);
            return svrTreeListResult.failure("Dom获取Http服务失败 code=" + code);
        }

        List<String> srvTreeList = new ArrayList<>();
        JSONObject dataResult = jsonResult.getJSONObject("data").getJSONObject(date);
        for (Map.Entry<String, Object> dataEntry : dataResult.entrySet()) {
            JSONArray bgServicesInfo = (JSONArray) dataEntry.getValue();
            for (int i = 0; i < bgServicesInfo.size(); i++) {
                JSONObject bgServiceInfo = bgServicesInfo.getJSONObject(i);
                String svrTreeKey = bgServiceInfo.getString("key");

                String svrTreePath = OpsUtil.transSrvTreeKey2Path(svrTreeKey);
                srvTreeList.add(svrTreePath);
            }
        }
        svrTreeListResult.setSuccResult(srvTreeList);
        return svrTreeListResult;
    }
}
