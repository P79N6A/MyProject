package com.sankuai.inf.octo.mns.sentinel;

import com.sankuai.inf.octo.mns.model.AgentInfo;
import com.sankuai.inf.octo.mns.util.CommonUtil;
import com.sankuai.inf.octo.mns.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lhmily on 08/05/2016.
 */
public class CustomizedManager {
    private static final Logger LOG = LoggerFactory.getLogger(CustomizedManager.class);
    private static List<AgentInfo> customizedAgentList = new ArrayList<AgentInfo>();

    private CustomizedManager() {
    }

    public static List<AgentInfo> getCustomizedAgentList() {
        return customizedAgentList;
    }

    //not recommended to use
    public static void setCustomizedSGAgents(String customizedAgentStr) {
        customizedAgentList.clear();
        if (CommonUtil.isBlankString(customizedAgentStr)) {
            LOG.warn("The customized agent list is empty. customizedAgentStr={}", customizedAgentStr);
            return;
        }
        String[] strArray = customizedAgentStr.split(",");
        List<String> errorAgentList = new ArrayList<String>();
        for (String item : strArray) {
            handleAgentIpPort(item, errorAgentList);
        }
        if (!errorAgentList.isEmpty()) {
            LOG.error("Invalid customized agent list, {}", errorAgentList.toString());
        }

        Collections.shuffle(customizedAgentList);
    }

    private static void handleAgentIpPort(String ipPortStr, List<String> errorList) {
        if (!ipPortStr.contains(":")) {
            errorList.add(ipPortStr);
            return;
        }
        String[] ipPort = ipPortStr.split(":");
        if (2 != ipPort.length) {
            errorList.add(ipPortStr);
            return;
        }
        String cusIp = ipPort[0];
        if (!IpUtil.checkIP(cusIp)) {
            errorList.add(ipPortStr);
            return;
        }
        int port = Integer.parseInt(ipPort[1]);
        if (port < 1024 || port > 65536) {
            errorList.add(ipPortStr);
            return;
        }
        boolean isExist = false;
        for (AgentInfo info : customizedAgentList) {
            if (info.getIp().equals(cusIp) && info.getPort() == port) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            AgentInfo item = new AgentInfo(cusIp, port, "com.sankuai.inf.sg_agent", "");
            customizedAgentList.add(item);
        }
    }
}
