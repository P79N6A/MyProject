package com.sankuai.octo.sgnotify.notify;

import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.octo.config.model.ConfigNode;
import com.sankuai.octo.sgnotify.model.Constants;
import com.sankuai.octo.sgnotify.util.CommonUtil;
import com.sankuai.sgagent.thrift.model.ConfigUpdateRequest;
import com.sankuai.sgagent.thrift.model.SGAgent;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lhmily on 06/18/2017.
 */
@Component
public class DynamicNotify {
    private static Logger LOG = LoggerFactory.getLogger(DynamicNotify.class);

    public Map<String, Integer> batchNotify(Map<String, List<ConfigNode>> map) {
        Map<String, Integer> retMap = new HashMap<String, Integer>();

        for (Map.Entry<String, List<ConfigNode>> entry : map.entrySet()) {
            List<ConfigNode> configs = entry.getValue();
            if (null != configs && !configs.isEmpty()) {
                int code = notifyAgent(entry.getKey(), entry.getValue());
                retMap.put(entry.getKey(), code);
            } else {
                retMap.put(entry.getKey(), 400);
            }

        }
        return retMap;
    }

    private int notifyAgent(String ip, List<ConfigNode> nodes) {
        int ret = Constants.CODE_AGENT_CONNECT_ERROR;
        SGAgent.Iface agentClient;
        TSocket socket = new TSocket(ip, 5266, CommonUtil.longTimeOutInMills);
        try {
            socket.open();
            socket.setTimeout(700);
            TFramedTransport transport = new TFramedTransport(socket, Consts.defaltMaxResponseMessageBytes);
            TProtocol protocol = new TBinaryProtocol(transport);
            agentClient = new SGAgent.Client(protocol);
            ret = agentClient.updateConfig(new ConfigUpdateRequest(nodes));
        } catch (TTransportException e) {
            LOG.warn("connect agent {} failed", ip);
            ret = Constants.CODE_AGENT_CONNECT_ERROR;
        } catch (TException e) {
            LOG.error("notify agent {} update config failed", ip);
            ret = Constants.CODE_AGENT_INVOKE_ERROR;
        } catch (Exception e) {
            LOG.warn("notify agent " + ip + " update config  failed", e);
            ret = Constants.CODE_SGNOTIFY_INTERNAL_ERROR;
        } finally {
            socket.close();
        }
        return ret;
    }
}
