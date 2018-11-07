package com.sankuai.inf.octo.mns;


import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.inf.octo.mns.util.*;
import com.sankuai.sgagent.thrift.model.SGAgent;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistryManager {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryManager.class);
    private final SGAgent.Iface tempClient = new InvokeProxy(SGAgentClient.ClientType.temp).getProxy();
    private ExecutorService executors = Executors.newFixedThreadPool(1);

    private final String HLBVersion = getHLBVersion();


    public void registerService(final SGService sgService) throws TException {
        if (!isSGServiceValid(sgService)) {
            return;
        }
        executors.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (tempClient) {
                        correctSGService(sgService);
                        LOG.info("Octo service provider registration, param = {}", sgService.toString());
                        tempClient.registService(sgService);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
    }


    public void registerServiceWithCmd(final int cmd, final SGService sgService) throws TException {
        if (!isSGServiceValid(sgService)) {
            return;
        }
        executors.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (tempClient) {
                        correctSGService(sgService);
                        LOG.info("Octo service provider registration, cmd ={} | param = {}", cmd, sgService.toString());
                        tempClient.registServicewithCmd(cmd, sgService);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
    }

    public void unRegisterService(final SGService sgService) throws TException {
        if (!isSGServiceValid(sgService)) {
            return;
        }
        executors.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (tempClient) {
                        correctSGService(sgService);
                        LOG.info("Octo service provider unregistration, param = {}", sgService.toString());
                        tempClient.unRegistService(sgService);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
    }

    private void correctSGService(SGService service) {
        service.setSwimlane(com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getSwimlane())
                .setCell(com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getCell());
        if ("http".equalsIgnoreCase(service.getProtocol())) {
            service.setVersion(HLBVersion);
        }
    }


    private static boolean isSGServiceValid(SGService service) {
        if (null == service) {
            LOG.error("Cannot register null.");
            return false;
        }
        int port = service.getPort();
        String appkey = service.getAppkey();
        String ip = service.getIp();
        if (port < 1) {
            LOG.error("Invalid port: port|" + port + "|appkey:" + appkey);
            return false;
        }
        if (CommonUtil.isBlankString(appkey)) {
            LOG.error("Invalid appKey: appkey|" + appkey);
            return false;
        }

        if (!IpUtil.checkIP(ip)) {
            LOG.error("Invalid ip: ip|" + ip + "|appkey:" + appkey);
            return false;
        }
        return true;
    }

    private String getHLBVersion() {
        String mnsInvokerVersion = VersionUtil.getVersion();
        return CommonUtil.isBlankString(mnsInvokerVersion) ? "HLB" : "HLB-v" + mnsInvokerVersion;
    }
}
