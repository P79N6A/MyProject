package com.sankuai.inf.octo.mns.hlb;

import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.util.CommonUtil;
import com.sankuai.sgagent.thrift.model.ConfigStatus;
import com.sankuai.sgagent.thrift.model.CustomizedStatus;
import com.sankuai.sgagent.thrift.model.SGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lhmily on 11/01/2016.
 */
public class HttpServerPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerPublisher.class);

    private String appKey;
    private int port = -1;
    private ConfigStatus configStatus;
    private static String localIP = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4();

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private boolean isValid(boolean isLog) {
        if (CommonUtil.isBlankString(appKey)) {
            if (isLog) {
                LOG.error("HLB publisher error, appKey cannot be empty.");
            }
            return false;
        }
        if (port < 1) {
            if (isLog) {
                LOG.error("HLB publisher error, invalid port {}.", port);
            }
            return false;
        }
        return true;
    }

    public void publish() {
        if (!isValid(true)) {
            return;
        }
        try {
            LOG.info("HLB registration appKey = {}, port = {}", appKey, port);
            MnsInvoker.registerService(getSGService());
        } catch (Exception e) {
            LOG.debug("HLB registration error.", e);
        }
    }

    public void destroy() {
        if (!isValid(false)) {
            return;
        }
        try {
            LOG.info("HLB unRegistration appKey = {}, port = {}", appKey, port);
            MnsInvoker.unRegisterHttpService(appKey, port);
            Thread.sleep(1000L);
        } catch (Exception e) {
            LOG.debug("HLB unRegistration error.", e);
        }
    }

    public ConfigStatus getConfigStatus() {
        return configStatus;
    }

    public void setConfigStatus(ConfigStatus configStatus) {
        this.configStatus = configStatus;
    }



    private SGService getSGService() {
        SGService service = new SGService();

        service.setAppkey(appKey)
                .setPort(port).setIp(localIP)
                .setVersion("HLB")
                .setLastUpdateTime((int) (System.currentTimeMillis() / 1000))
                .setServerType(1)
                .setWeight(10).setFweight(10.d)
                .setProtocol("http")
                .setExtend("OCTO|slowStartSeconds:180");

        if (null == configStatus) {
            configStatus = new ConfigStatus();
        }
        if (null == configStatus.initStatus) {
            //default value is DEAD
            configStatus.initStatus = CustomizedStatus.DEAD;
        }
        service.setStatus(configStatus.initStatus.getValue());
        return service;
    }
}
