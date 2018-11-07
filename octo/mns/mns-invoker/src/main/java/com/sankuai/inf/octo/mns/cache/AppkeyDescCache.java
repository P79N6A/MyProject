package com.sankuai.inf.octo.mns.cache;

import com.sankuai.inf.octo.mns.InvokeProxy;
import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.inf.octo.mns.util.CommonUtil;
import com.sankuai.octo.appkey.model.AppkeyDescResponse;
import com.sankuai.sgagent.thrift.model.SGAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppkeyDescCache {
    private static final Logger LOG = LoggerFactory.getLogger(AppkeyDescCache.class);
    private Map<String, AppkeyDescResponse> appkeyDescCache = new ConcurrentHashMap<String, AppkeyDescResponse>();
    private SGAgent.Iface sgAgent = new InvokeProxy(SGAgentClient.ClientType.mns).getProxy();

    private static final int SUCCESS = 0;
    private static final int ERR_NODE_NOTFIND = -101;
    private static final int retrySleepTime = 500;
    private static final AppkeyDescResponse appkeyDesc404 = new AppkeyDescResponse();

    static {
        appkeyDesc404.setErrCode(404)
                .setMsg("appkey cannot be empty");
    }


    AppkeyDescResponse get(String appkey) {
        if (CommonUtil.isBlankString(appkey)) {
            return appkeyDesc404;
        }
        AppkeyDescResponse ret = appkeyDescCache.get(appkey);
        return (null != ret) ? ret : getDescFromAgentAndSave(appkey);
    }

    private AppkeyDescResponse getDescFromAgentAndSave(String appkey) {
        AppkeyDescResponse ret = null;
        for (int i = 0; i < 3; ++i) {
            try {
                ret = sgAgent.getAppkeyDesc(appkey);
                if (null != ret && SUCCESS == ret.getErrCode() && null != ret.getDesc()) {
                    // success to get appkey desc
                    break;
                } else if (null != ret && ERR_NODE_NOTFIND == ret.getErrCode()) {
                    LOG.warn("Fail to get appkey desc, the appkey = {} doesnot exist", appkey);
                    break;
                } else {
                    if (i < 2) {
                        LOG.info("Fail to get appkey desc, now try to get it again. time = {} and sleep = {}", i + 1, retrySleepTime);
                        Thread.sleep(retrySleepTime);
                    }
                }
            } catch (Exception e) {
                LOG.error("Fail to get appkey desc, now try to get it again. time = " + (i + 1), e);
            }
        }
        if (null == ret) {
            ret = new AppkeyDescResponse();
            ret.setErrCode(-1).setMsg("fail to get appkey desc of " + appkey + " from sg_agent");
        }
        appkeyDescCache.put(appkey, ret);
        return ret;
    }

    void updateAll() {
        for (Map.Entry<String, AppkeyDescResponse> item : appkeyDescCache.entrySet()) {
            AppkeyDescResponse ret = null;
            try {
                ret = sgAgent.getAppkeyDesc(item.getKey());
            } catch (Exception e) {
                LOG.error("Fail to get appkey desc = {} from sg_agent in periodic polling. mns-invoker already uses a cache to deal with this exception.", item.getKey(), e);
            }
            if (null != ret && SUCCESS == ret.getErrCode() && null != ret.getDesc()) {
                appkeyDescCache.put(item.getKey(), ret);
            }
        }
    }

}
