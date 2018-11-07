package com.sankuai.inf.octo.mns.sentinel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.inf.octo.mns.model.AgentInfo;
import com.sankuai.inf.octo.mns.util.HttpUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SentinelManager {
    private static final Logger LOG = LoggerFactory.getLogger(SentinelManager.class);
    private static List<AgentInfo> sentinelAgentList = new ArrayList<AgentInfo>();
    private static final Object lock = new Object();

    private SentinelManager() {

    }

    public static void initSentinels() {
        String env = ProcessInfoUtil.getOctoEnv();
        String ip = ProcessInfoUtil.getLocalIpV4();
        StringBuilder sb = new StringBuilder(Consts.getServerListApi);
        sb.append("ip=" + ip)
                .append("&env=" + env)
                .append("&appkey=" + Consts.sg_sentinelAppkey)
                .append("&hostname=" + ProcessInfoUtil.getHostNameInfoByIp());
        try {
            sb.append("&host=" + URLEncoder.encode(ProcessInfoUtil.getHostInfoByIp(ip), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.debug("host encode exception", e);
        }
        String result = HttpUtil.get(sb.toString());
        if (StringUtils.isEmpty(result)) {
            LOG.warn("failed to get sg_sentinel from {}", Consts.getServerListApi);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(result);
            JsonNode serviceList = rootNode.path("data").path("serviceList");
            if (null != serviceList) {
                sentinelAgentList.clear();
                Iterator<JsonNode> iter = serviceList.elements();
                while (iter.hasNext()) {
                    JsonNode serviceNode = iter.next();
                    sentinelAgentList.add(new AgentInfo(serviceNode.path("ip").textValue(),
                            serviceNode.path("port").intValue(), Consts.sg_sentinelAppkey, env));
                }
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    public static List<AgentInfo> getSentinelAgentList() {
        if (sentinelAgentList.isEmpty()) {
            synchronized (lock) {
                if (sentinelAgentList.isEmpty()) {
                    initSentinels();
                }
            }
        }
        return sentinelAgentList;
    }
}
