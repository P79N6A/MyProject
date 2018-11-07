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
 * Created by zmz on 2017/8/17.
 * 获取服务画像性能相关的数据
 * 包括基线数据的方法和服务性能特征值的方法
 */
@Service
public class PortraitQPSDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PortraitQPSDataService.class);
    private static final List<String> qpsFeatureTags = new ArrayList<String>(){{
        add("qps_max");
        add("qps_min");
        add("peak_max");
        add("peak_min");
        add("peak_hour");
        add("qps_avg");
    }};

    private static final List<String> qpsPicData = new ArrayList<String>(){{
        add("all_qps5min_series");
        add("dx_qps5min_series");
        add("gh_qps5min_series");
        add("yf_qps5min_series");
        add("gq_qps5min_series");
        add("cq_qps5min_series");
    }};

    public Map<String, String> getQPSFeatureData(String appkey){
         // 获取性能相关特征数值，如qps波峰波谷值，平均qps等。后期维护可修改变量qpsFeatureTags进行增添新特征
        Map<String, String> featureData = new HashMap<>();
        for(String tag: qpsFeatureTags){
            // 做一遍请求操作
            GetTagRequest request = new GetTagRequest();
            request.setAppkey(appkey);
            request.setTagName(tag);
            TagResponse response = new TagResponse();

            try {
                response = PortraitClient.getInstance().getAppCommonTag(request);
            } catch (TException e) {
                LOGGER.error("getQPSFeatureData is ERROR, {}" + e);
            } finally {
                featureData.put(tag, response.getValue());
            }
        }
        return featureData;
    }

    public Map<String, List> getQPSPicData(String appkey) {
         // 获取各个机房qps时间序列，通过维护变量qpsPicData进行增删各机房
        Map<String, String> allPicData = new HashMap<>();
        TagResponse response = new TagResponse();
        for(String tag : qpsPicData){
            GetTagRequest request = new GetTagRequest();
            request.setAppkey(appkey);
            request.setTagName(tag);

            try {
                response = PortraitClient.getInstance().getAppCommonTag(request);
                // 当获取数据成功的时候，对数据进行解析
                if (0 == response.getStatus()) {
                    String getValue = response.getValue();
                    String commonTags = Json.parse(getValue).toString();
                    allPicData.put(tag, commonTags);
                }
            } catch (TException e) {
                LOGGER.error("getQPSPicData is ERROR, {}" + e);
            }
        }

        Map<String, List> rtnMap = new HashMap<>();
        List<Double> mml = new ArrayList<>();
        List<Double> qpsl = new ArrayList<>();
        for(Map.Entry<String, String> entry: allPicData.entrySet()){
            String picData = entry.getValue();
            qpsl.clear();
            mml.clear();
            JSONArray picDataArray = JSONArray.fromObject(picData);
            for(int i=0; i<picDataArray.size(); i++){
                JSONObject momentData = (JSONObject) picDataArray.get(i);
                qpsl.add(Double.valueOf(momentData.get("qps").toString()));
            }
            if("all_qps5min_series".equals(entry.getKey())){
                for(int i=0; i<picDataArray.size(); i++){
                    JSONObject momentData = (JSONObject) picDataArray.get(i);
                    int time = Integer.valueOf(momentData.get("mm").toString());
                    mml.add((double) time);
                }
                rtnMap.put("xAxis", new ArrayList<>(mml));
            }
            rtnMap.put(entry.getKey(), new ArrayList<>(qpsl));
        }
        return rtnMap;
    }
}
