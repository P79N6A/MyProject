package com.sankuai.octo.msgp.service.portrait;

import com.sankuai.inf.hulk.portrait.thrift.service.GetTagRequest;
import com.sankuai.inf.hulk.portrait.thrift.service.TagResponse;
import com.sankuai.octo.msgp.utils.client.PortraitClient;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import play.libs.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zmz on 2017/9/20.
 * 服务画像资源相关数据
 * 主要包括负载的特征值与负载的基线数据
 */
@Service
public class PortraitLoadDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PortraitLoadDataService.class);
    private static final List<String> loadFeatureTags = new ArrayList<String>() {{
        add("thread_num_max");
        add("thread_num_min");
        add("thread_num_avg");
        add("thread_runnable_num_max");
        add("thread_runnable_num_min");
        add("thread_runnable_num_avg");
        add("net_total_max");
        add("net_total_min");
        add("net_total_avg");
        add("net_in_max");
        add("net_in_min");
        add("net_in_avg");
        add("net_out_max");
        add("net_out_min");
        add("net_out_avg");
        add("load1min_max");
        add("load1min_min");
        add("load1min_avg");
        add("load1min_per_cpu_max");
        add("load1min_per_cpu_min");
        add("load1min_per_cpu_avg");
    }};

    private static final List<String> loadPicData = new ArrayList<String>(){{
        add("all_load1min_series");
        add("dx_load1min_series");
        add("gh_load1min_series");
        add("yf_load1min_series");
        add("gq_load1min_series");
        add("cq_load1min_series");
    }};

    public Map<String, String> getLoadFeatureData(String appkey) {
        // 获取资源值Load的特征数据，如运行的线程数波峰波谷值等，可以维护变量loadFeatureTags来进行增删特征
        Map<String, String> featureData = new HashMap<>();
        for (String tag : loadFeatureTags) {
            // 做一遍请求操作
            GetTagRequest request = new GetTagRequest();
            request.setAppkey(appkey);
            request.setTagName(tag);
            TagResponse response = new TagResponse();

            try {
                response = PortraitClient.getInstance().getAppCommonTag(request);
            } catch (TException e) {
                LOGGER.error("getLoadFeatureData is ERROR, " + e);
            }
            featureData.put(tag, response.getValue());
        }
        return featureData;
    }

    public Map<String, List> getServiceResourceLoadPicData(String appkey) {
        // 获取各个机房资源值的时间序列，维护变量loadPicData可以修改机房
        Map<String, String> allPicData = new HashMap<>();
        TagResponse response = new TagResponse();
        for (String tag : loadPicData) {
            GetTagRequest request = new GetTagRequest();
            request.setAppkey(appkey);
            request.setTagName(tag);

            try {
                response = PortraitClient.getInstance().getAppCommonTag(request);
                if (0 == response.getStatus()) {
                    String getValue = response.getValue();
                    String commonTags = Json.parse(getValue).toString();
                    allPicData.put(tag, commonTags);
                }
            } catch (TException e) {
                LOGGER.error("getServiceResourceLoadPicData is ERROR, " + e);
            }
        }

        Map<String, List> rtnMap = new HashMap<>();
        List<String> mml = new ArrayList<>();
        List<List<String>> qpsl = new ArrayList<>();
        for (Map.Entry<String, String> entry : allPicData.entrySet()) {
            String picData = entry.getValue();
            qpsl.clear();
            mml.clear();
            JSONArray picDataArray = JSONArray.fromObject(picData);
            for (int i = 0; i < picDataArray.size(); i++) {
                JSONObject momentData = (JSONObject) picDataArray.get(i);
                List<String> data = new ArrayList<>();
                data.add(momentData.get("time").toString());
                data.add(momentData.get("load1min").toString());
                qpsl.add(data);
            }
            if ("all_load1min_series".equals(entry.getKey())) {
                for (int i = 0; i < picDataArray.size(); i++) {
                    JSONObject momentData = (JSONObject) picDataArray.get(i);
                    String time = (momentData.get("time").toString());
                    mml.add(time);
                }
                rtnMap.put("xAxis", new ArrayList<>(mml));
            }
            rtnMap.put(entry.getKey(), new ArrayList<>(qpsl));
        }
        return rtnMap;
    }

}
