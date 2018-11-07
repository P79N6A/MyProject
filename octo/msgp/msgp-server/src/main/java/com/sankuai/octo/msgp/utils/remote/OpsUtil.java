package com.sankuai.octo.msgp.utils.remote;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dianping.zebra.util.StringUtils;
import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.octo.msgp.utils.ResultData;
import com.sankuai.octo.msgp.utils.remote.dto.ServerTreeAppkeyRelation;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by emma on 2017/6/6.
 */
public class OpsUtil {
    private static Logger logger = LoggerFactory.getLogger(DomUtil.class);

    public static final String CORP = "corp";
    public static final String OWT = "owt";
    public static final String PDL = "pdl";
    public static final String SRV = "srv";

    private static final String getAllSvrTreeAppkeyRelationURL = "https://ops.sankuai.com/api/stree/octo/list";
    private static final String getServiceListURL = "http://ops.sankuai.com/api/stree/service/list/srv";
    private static final String APPKEY_BIND_SRV_URL = "http://ops.vip.sankuai.com/api/v0.2/appkeys/%s/srvs";

    public static final String JAVA_SVC_FLAG = "Code: Java";
    private static final String HEADER_NAME = "Authorization";
    private static final String HEADER_VALUE = "Bearer 6e0f033b45a278d2a6cad32940de88c9b4bd5725";

    // 这里获取全量的服务树和Appkey关系,请求一次时间较久, 故做一个简单的缓存, 一小时会清理一次
    private static ServerTreeAppkeyRelation _svrTreeAppkeyRelation = new ServerTreeAppkeyRelation();
    private static ReentrantReadWriteLock _svrTreeAppkeyPairRWLock = new ReentrantReadWriteLock();
    private static final ScheduledExecutorService svrTreeAppkeyExecutorService = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("SrvTreeAppkeyRelation-pool-%d").daemon(true).build());

    static {
        svrTreeAppkeyExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    _svrTreeAppkeyPairRWLock.writeLock().lock();
                    _svrTreeAppkeyRelation.clearData();
                } finally {
                    _svrTreeAppkeyPairRWLock.writeLock().unlock();
                }
            }
        }, 0, 3600, TimeUnit.SECONDS);
    }

    /**
     * 获取全量服务树和Appkey关系
     *
     * @return
     */
    public static ResultData<ServerTreeAppkeyRelation> getBJAllSvrTreeAppkeyRelation() {
        ResultData<ServerTreeAppkeyRelation> allSvrTreeAppkeyPairRet = new ResultData<>();

        try {
            _svrTreeAppkeyPairRWLock.readLock().lock();
            if (_svrTreeAppkeyRelation != null && _svrTreeAppkeyRelation.hasData()) {
                // 有数据则拷贝过来
                allSvrTreeAppkeyPairRet.setSuccResult(new ServerTreeAppkeyRelation(_svrTreeAppkeyRelation));
                return allSvrTreeAppkeyPairRet;
            }
        } finally {
            _svrTreeAppkeyPairRWLock.readLock().unlock();
        }
        try {
            _svrTreeAppkeyPairRWLock.writeLock().lock();
            if (_svrTreeAppkeyRelation != null && _svrTreeAppkeyRelation.hasData()) {
                allSvrTreeAppkeyPairRet.setSuccResult(new ServerTreeAppkeyRelation(_svrTreeAppkeyRelation));
                return allSvrTreeAppkeyPairRet;
            }

            ResultData<ServerTreeAppkeyRelation> reqResult = reqAllSvrTreeAppkeyRelation();
            if (!reqResult.isSuccess()) {
                return allSvrTreeAppkeyPairRet.failure(reqResult.getMsg());
            }

            ServerTreeAppkeyRelation tmpRelation = reqResult.getData();
            _svrTreeAppkeyRelation.clearData();
            _svrTreeAppkeyRelation = new ServerTreeAppkeyRelation(tmpRelation);
            allSvrTreeAppkeyPairRet.setSuccResult(new ServerTreeAppkeyRelation(_svrTreeAppkeyRelation));
            return allSvrTreeAppkeyPairRet;
        } finally {
            _svrTreeAppkeyPairRWLock.writeLock().unlock();
        }
    }

    /**
     * 获取Ops存在的服务列表
     *
     * @return
     */
    public static ResultData<JSONArray> getServiceList() {
        ResultData serviceResult = new ResultData();

        // 1. 查询Ops记录的所有服务
        String result = HttpUtil.httpGetRequest(getServiceListURL, getHeader(), null);
        JSONObject jsonResult;
        JSONArray data;
        int code = -1;
        if (StringUtils.isBlank(result) || (jsonResult = JSONObject.parseObject(result)) == null
                || (code = jsonResult.getInteger("code")) != 200
                || (data = jsonResult.getJSONArray("data")) == null) {
            logger.error("Ops get service list fail, code={}, result={}.", code, result);
            return serviceResult.failure("OPS获取服务list失败. code=" + code);
        }
        serviceResult.setSuccResult(data);
        return serviceResult;
    }

    public static String transSrvTreeKey2Path(String svrTreeKey) {
        String[] strs;
        if (StringUtils.isBlank(svrTreeKey) || (strs = svrTreeKey.split("\\.")).length < 4) {
            logger.error("service_tree_key={} from dom is illegal.", svrTreeKey);
            return svrTreeKey;
        }
        StringBuilder svrTreePath = new StringBuilder();
        svrTreePath.append(OpsUtil.CORP).append("=").append(strs[0]).append("&")
                .append(OpsUtil.OWT).append("=").append(strs[1]).append("&")
                .append(OpsUtil.PDL).append("=").append(strs[2]).append("&")
                .append(OpsUtil.SRV).append("=").append(strs[3]);

        return svrTreePath.toString();
    }

    public static ResultData<Boolean> hasBindSrvTree(String appkey) {
        ResultData<Boolean> resultData = new ResultData();
        try {
            String opsUrl = String.format(APPKEY_BIND_SRV_URL, appkey);

            String resultStr = HttpUtil.httpGetRequest(opsUrl, getHeader(), null);
            JSONObject dataObject = JSONObject.parseObject(resultStr);
            if (dataObject.getString("error") != null) {
                return resultData.failure(dataObject.getString("error"));
            } else if (dataObject.getJSONArray("srvs") == null || dataObject.getJSONArray("srvs").isEmpty()) {
                return resultData.failure(appkey + "在服务树没有绑定，请先绑定服务树");
            }
            return resultData.success(true);
        } catch (Exception e) {
            String errorMsg = e.getMessage() == null ? e.getClass().getSimpleName(): e.getMessage();
            return resultData.failure(appkey + "查询服务树绑定异常, " + errorMsg);
        }
    }

    private static ResultData<ServerTreeAppkeyRelation> reqAllSvrTreeAppkeyRelation() {
        ResultData<ServerTreeAppkeyRelation> ret = new ResultData<>();
        String result = HttpUtil.httpGetRequest(getAllSvrTreeAppkeyRelationURL, getHeader(), null);
        JSONObject jsonResult;
        if (StringUtils.isBlank(result) || (jsonResult = JSONObject.parseObject(result)) == null) {
            logger.error("Ops getBJAllSvrTreeAppkeyRelation return empty or parse fail, result={}.", result);
            return ret.failure("OPS获取所有服务树Appkey失败, 返回为空或解析失败");
        }
        Map<String, Set<String>> allSvrTreeAppkeyRelation = new HashMap<>();
        Map<String, String> allAppkeyOwtRelation = new HashMap<>();
        for (Map.Entry<String, Object> dataEntry : jsonResult.entrySet()) {
            String svrTreePath = dataEntry.getKey();
            String owt = parseOwt(svrTreePath);

            Set<String> appkeySet = new HashSet<>();
            JSONArray appkeys = (JSONArray) dataEntry.getValue();
            for (int i = 0; i < appkeys.size(); i++) {
                String appkey = appkeys.getString(i).toLowerCase();
                appkeySet.add(appkey);
                allAppkeyOwtRelation.put(appkey, owt);
            }
            allSvrTreeAppkeyRelation.put(svrTreePath, appkeySet);
        }
        ret.setSuccResult(new ServerTreeAppkeyRelation(allSvrTreeAppkeyRelation, allAppkeyOwtRelation));
        return ret;
    }

    private static String parseOwt(String svrTreePath) {
        String[] svrTreeItems = svrTreePath.split("&");
        if (svrTreeItems.length < 4) {
            return svrTreePath;
        }
        String owtPair = svrTreeItems[1];
        String[] strs = owtPair.split("=");
        if (strs.length < 2) {
            return owtPair;
        }
        return strs[1];
    }

    private static Map<String, String> getHeader() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(HEADER_NAME, HEADER_VALUE);
        return paramMap;
    }

    public static void main(String[] args) {
//        Set<Thread> threads = new HashSet<Thread>();
//        for (int i = 0; i < 100; i++) {
//            Thread t = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (true) {
//                        ResultData<Map<String, Set<String>>> allSvrTreeAppkeyRelationRet = getBJAllSvrTreeAppkeyRelation();
//                        Map<String, Set<String>> relation = allSvrTreeAppkeyRelationRet.getData();
//                        int seconds = ThreadLocalRandom.current().nextInt(3);
//                        try {
//                            Thread.sleep(seconds * 1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
//            threads.add(t);
//        }
//        for (Thread t : threads) {
//            t.start();
//        }
    }
}
