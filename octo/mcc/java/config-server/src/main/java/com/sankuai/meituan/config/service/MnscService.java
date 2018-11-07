package com.sankuai.meituan.config.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meituan.mtrace.http.client.DefaultHttpClient;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.meituan.config.model.AppkeysResponse;
import com.sankuai.meituan.config.util.Common;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by zhangcan on 17/6/2.
 */
@Component
public class MnscService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MnscService.class);

    @Resource
    private ConfigTairClient configTairClient;

    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    private static volatile List<String> appkeyList;

    private static final String mnsUrl = getMnsUrl();
    private static String getMnsUrl(){
        return Common.isOnline()? "http://mns.sankuai.com": "http://mns.inf.test.sankuai.com";
    }

    public List<String> getAllAppkeys() {
        List<String> appkeys = new ArrayList<String>();
        String url = String.format("%s/api/allappkeys", getMnsUrl());
        ObjectMapper mapper = new ObjectMapper();

        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String res = EntityUtils.toString(response.getEntity());
                AppkeysResponse appkeysResponse = mapper.readValue(res, AppkeysResponse.class);
                if (200 == appkeysResponse.getRet()) {
                    appkeys = appkeysResponse.getData();
                }
            } else {
                LOGGER.error("Failed to get appkeys from msnc.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get appkeys from msnc.", e);
        }
        if(null != appkeys && !appkeys.isEmpty()){
            appkeyList = appkeys;
        }
        return appkeyList;
    }

    public void asynDeleteOfflineNode(final String ip) {
        final List<String> envs = new ArrayList<String>();
        envs.add("prod");
        envs.add("stage");
        if(!ProcessInfoUtil.isLocalHostOnline()){
            envs.add("test");
        }
        final List<String> appkeys =  getAllAppkeys();
        FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean ret = false;
                for (String appkey : appkeys) {
                    for(String env: envs) {
                        try {
                            ret = configTairClient.deleteIpFromGroups(env, appkey, ip);
                            if (ret) {
                                LOGGER.info("删除下线机器节点: appkey={}, env={}, ip={}", appkey, env, ip);
                                break;
                            }
                        } catch (Exception e) {
                            LOGGER.warn("删除下线机器节点失败: appkey={}, env={}, ip={}, exception={}", appkey, env, ip, e.getMessage());
                        }
                    }
                }
                return ret;
            }
        });
        singleThreadExecutor.submit(futureTask);
    }



}
