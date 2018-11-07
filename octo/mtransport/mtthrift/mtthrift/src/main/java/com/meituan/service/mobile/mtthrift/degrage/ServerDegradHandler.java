package com.meituan.service.mobile.mtthrift.degrage;

import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import com.sankuai.octo.oswatch.thrift.data.DegradeEnd;
import com.sankuai.octo.oswatch.thrift.data.DegradeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Author: caojiguang@gmail.com
 * Date: 15/8/14
 * Description:
 */
public class ServerDegradHandler {

    private final static Logger logger = LoggerFactory.getLogger(ServerDegradHandler.class);

    private Random rand = new Random(1);
    private String agentUrl = "";
    private String appKey = "";
    private volatile long degradeWarningTime = 0L;


    public ServerDegradHandler(final String agentUrl, final String appKey) {
        this.agentUrl = agentUrl;
        this.appKey = appKey;
    }


    public List<DegradeAction> getDegradeActions() {
        return getDegradeActionsByAgent(appKey);
    }

    public List<DegradeAction> getDegradeActionsByAgent(String appKey) {

        List<DegradeAction> degradeActions = new ArrayList<DegradeAction>();
        try {
            List<DegradeAction> degradeActionList = MnsInvoker.getDegradeActionListAtClient("", appKey);

            if(null != degradeActionList) {
                for(DegradeAction action : degradeActionList) {
                    if(!action.getProviderAppkey().equalsIgnoreCase(appKey))
                        continue;

                    degradeActions.add(action);
                    long curTime = System.currentTimeMillis();
                    if(curTime - degradeWarningTime > 5000L) {
                        logger.warn("Service degrade action: " + action.toString());
                        degradeWarningTime = curTime;
                    }

                }
            }
        } catch (Exception ex) {
            logger.error("getDegradeActions by agent exception:" + ex.getMessage());
        }
        return degradeActions;
    }

    public boolean checkDegradeEvent(String simpleServiceName, String methodName) {
        List<DegradeAction> degradeActions = getDegradeActionsByAgent(appKey);
        String clientAppKey = ClientInfoUtil.getClientAppKey();
        // 方法名格式应为 服务名.方法名
        String fullMethodName = simpleServiceName + "." + methodName;
        boolean degrade = false;

        MtraceServerTBinaryProtocol.requestDegraded.set(false);

        if(null == degradeActions || degradeActions.isEmpty()) {
            return degrade;
        }

        DegradeAction action = null;
        for(DegradeAction degradeAction : degradeActions) {
            if (!degradeAction.getConsumerAppkey().equalsIgnoreCase(clientAppKey) )
                continue;
            String degradeMethod = degradeAction.getMethod();
            if(degradeAction.getDegradeEnd().equals(DegradeEnd.SERVER)
                    && degradeMethod.equalsIgnoreCase(fullMethodName)) {
                action = degradeAction;
                break;
            }
            if(degradeAction.getDegradeEnd().equals(DegradeEnd.SERVER)
                    && degradeMethod.equalsIgnoreCase("ALL")) {
                action = degradeAction;
                break;
            }
        }

        if (null != action) {
            double degradeRation = action.getDegradeRatio();
            double randomDouble = rand.nextDouble();
            if (Double.compare(randomDouble, degradeRation) < 0) {
                if(action.getDegradeStrategy().equals(DegradeStrategy.DROP))
                    degrade = true;
                else if(action.getDegradeStrategy().equals(DegradeStrategy.CUSTOMIZE))
                    MtraceServerTBinaryProtocol.requestDegraded.set(true);

            }
        }
        return degrade;
    }

}
