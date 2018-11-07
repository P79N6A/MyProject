package com.meituan.service.mobile.mtthrift.client.rhino.faultInject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.rhino.Rhino;
import com.dianping.rhino.fault.FaultInject;
import com.dianping.rhino.fault.FaultInjectContext;
import com.meituan.service.mobile.mtthrift.client.rhino.RhinoContext;
import com.meituan.service.mobile.mtthrift.client.rhino.RhinoHandler;
import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.service.mobile.mtthrift.netty.exception.RequestTimeoutException;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.MtConfigUtil;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaultInjectHandler implements RhinoHandler {
    private static final Logger logger = LoggerFactory.getLogger(FaultInjectHandler.class);
    private static FaultInject faultInject;

    private String appkey;
    private String serviceName;

    static {
        try {
            if (StringUtils.isBlank(MtConfigUtil.getAppName())) {
                logger.error("app.name 为空，故障注入不会生效，如果没有故障注入功能则可以忽略。"
                        + "app.name的配置参考：http://docs.sankuai.com/doc/arch_dp/cat/integration/java/#22");
            } else {
                faultInject = Rhino.newFaultInject(Consts.mtraceInfra);
            }
        } catch (Throwable e) {
            logger.error("故障注入初始化失败，故障注入不会生效，如果没有故障注入功能则可以忽略", e);
        }
    }

    public FaultInjectHandler(String appkey, String serviceName) {
        this.appkey = appkey;
        this.serviceName = serviceName;

        if (appkey == null || appkey.isEmpty()) {
            logger.warn("appkey is null or empty, faultInject may not work properly.");
        }

        if (serviceName == null || serviceName.isEmpty()) {
            logger.warn("serviceName is null or empty, faultInject may not work properly.");
        }

    }

    private void reportFaultInjectToCat(RhinoContext rhinoContext) {
        String methodName = rhinoContext.getMethodInvocation().getMethod().getName();

        Transaction transaction = Cat.newTransaction("OctoFaultInject", serviceName + "." + methodName);
        try {
            Cat.logEvent("OctoFaultInject.appkey", appkey);
            Cat.logEvent("OctoFaultInject.remoteAppkey", rhinoContext.getRemoteAppkey());
            Cat.logEvent("OctoFaultInject.remoteServer",
                    rhinoContext.getRemoteServer().getIp() + ":" + rhinoContext.getRemoteServer().getPort());
            if (rhinoContext.isAsync()) {
                Cat.logEvent("OctoFaultInject.callType", "async");
            } else {
                Cat.logEvent("OctoFaultInject.callType", "sync");
            }
            if (rhinoContext.isDelay()) {
                Cat.logEvent("OctoFaultInject.faultInjectType", "delay");
                Cat.logEvent("OctoFaultInject.delayTime", String.valueOf(rhinoContext.getDelayTime()));
                Cat.logEvent("OctoFaultInject.originalTimeout", String.valueOf(rhinoContext.getOriginalTimeout()));
                Cat.logEvent("OctoFaultInject.currentTimeout", String.valueOf(rhinoContext.getCurrentTimeout()));
            } else {
                Cat.logEvent("OctoFaultInject.faultInjectType", "exception");
            }

            Exception exception = rhinoContext.getInjectedException();
            if (exception != null) {
                Cat.logEvent("OctoFaultInject.exception", String.valueOf(exception.getClass().getName()));
            }
            transaction.setSuccessStatus();
        } catch (Exception e) {
            transaction.setStatus(e);
        } finally {
            transaction.complete();
        }
    }

    @Override
    public void handle(RhinoContext rhinoContext) throws Exception {
        if (!ThriftClientGlobalConfig.isEnableFaultInject() || faultInject == null) {
            return;
        }

        MethodInvocation methodInvocation = rhinoContext.getMethodInvocation();
        String methodName = methodInvocation.getMethod().getName();

        FaultInjectContext faultInjectContext = faultInject.getContext();
        faultInjectContext.put("serviceName", serviceName);
        faultInjectContext.put("methodName", methodName);
        faultInjectContext.put("sourceAppKey", appkey);
        faultInjectContext.put("targetAppKey", rhinoContext.getRemoteAppkey());
        faultInjectContext.put("clientIp", ProcessInfoUtil.getLocalIpV4());
        faultInjectContext.put("targetAddress", rhinoContext.getRemoteServer().getIp());

        int originalTimeout = rhinoContext.getOriginalTimeout();

        if (faultInject.isActive(faultInjectContext)) {
            if (faultInjectContext.isDelay()) {
                rhinoContext.setDelay(true);
                int sleepTime = Math.min(originalTimeout, faultInjectContext.getDelayTime());
                try {
                    rhinoContext.setDelayTime(sleepTime);
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    //ignore the exception
                }
                if (sleepTime == originalTimeout) {
                    rhinoContext.setInjectedException(new RequestTimeoutException());
                } else {
                    rhinoContext.setCurrentTimeout(originalTimeout - faultInjectContext.getDelayTime());
                }
            } else {
                try {
                    faultInject.inject(faultInjectContext);
                } catch (Exception e) {
                    rhinoContext.setInjectedException(e);
                }
            }
        } else {
            rhinoContext.setCurrentTimeout(originalTimeout);
        }

        if (ThriftClientGlobalConfig.isEnableCat()) {
            reportFaultInjectToCat(rhinoContext);
        }
    }
}
