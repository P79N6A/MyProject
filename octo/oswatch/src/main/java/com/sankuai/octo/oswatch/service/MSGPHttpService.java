package com.sankuai.octo.oswatch.service;

import com.sankuai.octo.oswatch.Config;
import com.sankuai.octo.oswatch.model.AliveCount;
import com.sankuai.octo.oswatch.model.JsonAlarm;
import com.sankuai.octo.oswatch.thrift.data.ConsumerQuota;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import com.alibaba.fastjson.JSON;
import com.sankuai.octo.oswatch.thrift.data.ProviderQuota;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenxi on 7/6/15.
 */
public class MSGPHttpService {
    public Logger logger = LoggerFactory.getLogger(PerfService.class);
    public HttpClient httpClient = HttpClientBuilder.create().build();
    public Map<String, Long> alarmMap = new HashMap<String, Long>();

    public String msgpURL;

    public MSGPHttpService (String msgpURL) {
        this.msgpURL = msgpURL;
    }
    private static int alarmIntervalInMinutes = Integer.parseInt(Config.get("alarmIntervalInMinutes","30").trim());
    private int httpTimeout = Integer.parseInt(Config.get("httpTimeout","3000").trim());//ms
    private int continuousAlarmNum= Integer.parseInt(Config.get("continuousAlarmNum", "4").trim());

    //报警策略：连续3个监测周期报警后，停止报警30分钟，之后删除。
    public void alarm(ProviderQuota providerQuota, DegradeAction action, int nodeSize,int status) {
        String actionKey = makeKey(action);
        long currentTime = System.currentTimeMillis();

        if (alarmMap.containsKey(actionKey)) {
            Long lastTime = alarmMap.get(actionKey);
            int timeDiff = (int)(currentTime - lastTime);

            //大于30分钟
            if (timeDiff > TimeUnit.MINUTES.toMillis(alarmIntervalInMinutes))
                alarmMap.put(actionKey, action.getTimestamp());

            if (timeDiff < TimeUnit.MINUTES.toMillis(alarmIntervalInMinutes) &&
                    timeDiff >= TimeUnit.SECONDS.toMillis(providerQuota.watchPeriodInSeconds) *continuousAlarmNum )
                return;

        } else alarmMap.put(actionKey, action.getTimestamp());

        JsonAlarm alarm = action2Alarm(action, providerQuota, nodeSize,status);
        logger.info("a alarm will be post: "+ alarm);
        //报警开关打开：status==1 ||status==3
        if(status%2!=0)
            postAlarm(JSON.toJSONString(alarm));
        sendReport(JSON.toJSONString(alarm));
    }

    public int getAliveNode(ProviderQuota quota) {
        String queryURL = msgpURL + "/api/zk/service/alive" + "?env="+quota.getEnv()+"&appkey="+quota.getProviderAppkey();
        return JSON.parseObject(getJson(queryURL), AliveCount.class).getCount();
    }

    private JsonAlarm action2Alarm(DegradeAction action, ProviderQuota quota, int nodeSize,int status) {
        JsonAlarm ja = new JsonAlarm();
        ja.setId(action.getId());
        ja.setEnv(action.getEnv());
        ja.setProviderAppkey(action.getProviderAppkey());
        ja.setConsumerAppkey(action.getConsumerAppkey());
        ja.setProviderQPSCapacity(quota.getQPSCapacity());
        ja.setConsumerCurrentQPS(action.consumerQPS);
        ja.setConsumerQuotaQPS((int) (nodeSize * quota.QPSCapacity * getQuotaRatio(quota.getConsumerList(), action.getConsumerAppkey())));
        logger.info("nodeSize： " + nodeSize);
        logger.info("qpsCapacity: " + quota.QPSCapacity);
        logger.info("consumerAppkey: " + getQuotaRatio(quota.getConsumerList(), action.getConsumerAppkey()));

        ja.setMethod(action.getMethod());
        ja.setDegradeRatio(action.getDegradeRatio());
        ja.setDegradeEnd(quota.getDegradeEnd().getValue());
        ja.setDegradeStrategy(action.getDegradeStrategy().getValue());
        ja.setTimestamp(action.getTimestamp());
        ja.setStatus(status);
        return ja;
    }

    private double getQuotaRatio(List<ConsumerQuota> quotaList, String consumerAppkey) {
        double ratio = 0;

        for(ConsumerQuota cquota: quotaList) {
            if (cquota.consumerAppkey.equals(consumerAppkey)) return cquota.getQPSRatio();
        }

        return ratio;
    }

    private void postAlarm(String alarm) {
        String alarmURL = msgpURL + "/api/quota/alarmPost";
        postJson(alarmURL, alarm);
    }

    private String getJson(String url) {
        String rv = "";

        try {
            HttpGet httpGet=new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(httpTimeout)
                    .setConnectTimeout(httpTimeout)
                    .setSocketTimeout(httpTimeout).build();
            httpGet.setConfig(requestConfig);

            System.out.println(url);
            HttpResponse response = httpClient.execute(httpGet);

            if(response.getStatusLine().getStatusCode()==200) {
                rv = EntityUtils.toString(response.getEntity());
            }
        } catch (IOException ioe) {
            logger.error("http get error", ioe);
        } catch (Exception e) {
            logger.error("http get error", e);
        }

        logger.info("get json from msgp>>>>" + rv);
        return rv;
    }

    private void postJson(String url, String json) {
        try {
            logger.info("url======>>>>" + url);
            logger.info("json=====>>>>" + json);

            HttpPost request = new HttpPost(url);
            StringEntity params =new StringEntity(json);
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            logger.info("entity"+response.getEntity());
            logger.info("getStatusLine"+response.getStatusLine());

            if(response.getStatusLine().getStatusCode()==200) {
                logger.info("json alarm response>>>>>"+EntityUtils.toString(response.getEntity()));
            }
        } catch (UnsupportedEncodingException uee) {
            logger.error("post to "+url+ "exception", uee);
        } catch (IOException ioe) {
            logger.error("post to "+url+ "exception", ioe);
        } catch (Exception e) {
            logger.error("exception!!",e);
        }
    }

    private String makeKey(DegradeAction action) {
        return action.getEnv() + '|' + action.getConsumerAppkey() + '|' + action.getProviderAppkey() + '|' + action.getMethod();
    }

    public void sendReport(String report) {
        String sendReportURL = msgpURL + "/api/oswacth/report?";
        postJson(sendReportURL, report);
    }
}
