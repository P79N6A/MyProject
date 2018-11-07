package com.sankuai.octo.msgp.utils.remote;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dianping.zebra.util.StringUtils;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.octo.msgp.utils.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by emma on 2017/6/9.
 */
public class SHCmdbUtil {
    private static Logger logger = LoggerFactory.getLogger(SHCmdbUtil.class);

    private final static String getBuOwtURL = "https://api-cmdb.sankuai.com/api/v0.1/bu?count=20000";
    private final static String getBuProducts = "https://api-cmdb.sankuai.com/api/v0.1/bu/{bg_name}/products?count=20000";
    private final static String getProductProjects = "https://api-cmdb.sankuai.com/api/v0.1/products/{product_name}/projects?count=20000";

    private static Map<String, String> _appkeyOwtPair;
    private static final ReentrantReadWriteLock _appkeyOwtPairRWLock = new ReentrantReadWriteLock();
    private static final Timer _appkeyOwtPairTimer = new Timer();

    static {
        _appkeyOwtPairTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    _appkeyOwtPairRWLock.writeLock().lock();
                    _appkeyOwtPair = null;
                } finally {
                    _appkeyOwtPairRWLock.writeLock().unlock();
                }
            }
        }, 0l, 3600 * 1000l);
    }

    public static ResultData<Map<String, String>> getSHAppkeyOwtPair() {
        ResultData<Map<String, String>> appkeyOwtResult = new ResultData<>();

        try {
            _appkeyOwtPairRWLock.readLock().lock();
            if (copyExistAppkeyOwt(appkeyOwtResult)) {
                return appkeyOwtResult;
            }
        } finally {
            _appkeyOwtPairRWLock.readLock().unlock();
        }

        try {
            _appkeyOwtPairRWLock.writeLock().lock();
            if (copyExistAppkeyOwt(appkeyOwtResult)) {
                return appkeyOwtResult;
            }
            ResultData<Map<String, String>> reqResult = reqSHAppkeyOwtPair();
            if (!reqResult.isSuccess()) {
                return appkeyOwtResult.failure(reqResult.getMsg());
            }
            _appkeyOwtPair = new HashMap<>();
            _appkeyOwtPair.putAll(reqResult.getData());
            copyExistAppkeyOwt(appkeyOwtResult);
            Map<String, String> appkeyOwtPair = new HashMap<>();
            appkeyOwtPair.putAll(_appkeyOwtPair);
            return appkeyOwtResult;
        } finally {
            _appkeyOwtPairRWLock.writeLock().unlock();
        }
    }

    private static ResultData<Map<String, String>> reqSHAppkeyOwtPair() {
        ResultData<Map<String, String>> appkeyOwtResult = new ResultData<>();

        Map<String, String> appkeyOwtPair = new HashMap<>();
        String buOwtResult = HttpUtil.getResult(getBuOwtURL);
        JSONObject buOwtData;
        if (StringUtils.isBlank(buOwtResult) || (buOwtData = JSONObject.parseObject(buOwtResult)) == null) {
            logger.error("Get bu owt from ShangHai cmdb fail, url={}", getBuOwtURL);
            return appkeyOwtResult.failure("上海cmdb获取bg和owt失败");
        }
        JSONArray buObjs = buOwtData.getJSONArray("bu");
        for (int i = 0; i < buObjs.size(); i++) {
            JSONObject bgObj = buObjs.getJSONObject(i);
            String owt = bgObj.getString("ci_eng_name");
            String bu = bgObj.getString("bu_name");

            // 请求 bu的product
            String productUrl = getBuProducts.replaceAll("\\{\\w*\\}", bu);
            String productResult = HttpUtil.getResult(productUrl);
            JSONObject productData;
            if (StringUtils.isBlank(productResult) || (productData = JSONObject.parseObject(productResult)) == null) {
                logger.error("Get products from ShangHai cmdb fail, url={}", productUrl);
                return appkeyOwtResult.failure("上海cmdb获取产品线失败, bu=" + bu);
            }
            JSONArray productObjs = productData.getJSONArray("products");
            for (int j = 0; j < productObjs.size(); j++) {
                JSONObject productObj = productObjs.getJSONObject(j);
                String productName = productObj.getString("product_name");

                String projectUrl = getProductProjects.replaceAll("\\{\\w*\\}", productName);
                String projectResult = HttpUtil.getResult(projectUrl);
                JSONObject projectsData;
                if (StringUtils.isBlank(projectResult) || (projectsData = JSONObject.parseObject(projectResult)) == null) {
                    logger.error("Get projects from ShangHai cmdb fail, url={}", projectUrl);
                    return appkeyOwtResult.failure("上海cmdb获取项目失败, product=" + productName);
                }
                JSONArray projectObjs = projectsData.getJSONArray("projects");
                for (int k = 0; k < projectObjs.size(); k++) {
                    String appkey = projectObjs.getJSONObject(k).getString("project_name");
                    appkeyOwtPair.put(appkey, owt);
                }
            }
        }
        appkeyOwtResult.setSuccResult(appkeyOwtPair);
        return appkeyOwtResult;
    }


    private static boolean copyExistAppkeyOwt(ResultData<Map<String, String>> appkeyOwtResult) {
        if (_appkeyOwtPair != null && !_appkeyOwtPair.isEmpty()) {
            Map<String, String> appkeyOwtPair = new HashMap<>();
            appkeyOwtPair.putAll(_appkeyOwtPair);
            appkeyOwtResult.setSuccResult(appkeyOwtPair);
            return true;
        } else {
            return false;
        }
    }
}
