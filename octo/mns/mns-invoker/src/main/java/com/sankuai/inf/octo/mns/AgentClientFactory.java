package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.model.AgentInfo;
import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.inf.octo.mns.sentinel.SentinelManager;
import com.sankuai.inf.octo.mns.util.CommonUtil;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.sgagent.thrift.model.SGAgent;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class AgentClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AgentClientFactory.class);
    private static SGAgentClient mnsClient = null;
    private static SGAgentClient mccClient = null;
    private static SGAgentClient multiProtoClient = null;
    private static ThreadLocal<SGAgentClient> traceClient = new ThreadLocal<SGAgentClient>();

    private static final int SGAgentPort = 5266;

    private AgentClientFactory() {

    }

    public static SGAgentClient borrowClient(SGAgentClient.ClientType type) {
        SGAgentClient client = null;
        switch (type) {
            case temp:
                client = getSGAgentClient(type);
                break;
            case mns:
                if (null == mnsClient) {
                    mnsClient = getSGAgentClient(type);
                }
                client = mnsClient;
                break;
            case mcc:
                if (null == mccClient) {
                    mccClient = getSGAgentClient(type);
                }
                client = mccClient;
                break;
            case trace:
                if (null != traceClient.get()) {
                    client = traceClient.get();
                } else {
                    client = getSGAgentClient(type);
                    traceClient.set(client);
                }
                break;
            case multiProto:
                if (null == multiProtoClient) {
                    multiProtoClient = getSGAgentClient(type);
                }
                client = multiProtoClient;
                break;
            default:
                LOG.debug("");

        }
        return client;

    }

    public static void returnClient(SGAgentClient client) {
        switch (client.getType()) {
            case temp:
                client.destory();
                break;
            case mns:
                mnsClient = checkHealthLocality(client);
                break;
            case mcc:
                mccClient = checkHealthLocality(client);
                break;
            case multiProto:
                multiProtoClient = checkHealthLocality(client);
                break;
            case trace:
                traceClient.set(checkHealthLocality(client));
                break;
            default:
                break;

        }

    }

    public static SGAgentClient getSGAgentClient(SGAgentClient.ClientType type) {
        SGAgentClient client = null;
        List<AgentInfo> customizedList = CustomizedManager.getCustomizedAgentList();

        if (customizedList.isEmpty()) {
            client = createAgentClient(Consts.LOCALHOST, SGAgentPort);
            if (null != client) {
                client.setType(type);
                return client;
            }
            List<AgentInfo> sentinelList = SentinelManager.getSentinelAgentList();

            Collections.shuffle(sentinelList);
            for (AgentInfo agentInfo : sentinelList) {
                client = createAgentClient(agentInfo.getIp(), agentInfo.getPort());
                if (null != client) {
                    client.setType(type);
                    break;
                }
            }
            if (null == client) {
                LOG.warn("SgAgent not available, check the network."
                        + " ip: " + ProcessInfoUtil.getLocalIpV4()
                        + "|env: " + ProcessInfoUtil.getOctoEnv()
                        + "|sentinels:" + sentinelList.toString());
            }
        } else {
            for (AgentInfo agentInfo : customizedList) {
                client = createAgentClient(agentInfo.getIp(), agentInfo.getPort());
                if (null != client) {
                    client.setType(type);
                    break;
                }
            }
            if (null == client) {
                LOG.warn("SgAgent not available, check the network. The customized agent list is {}", customizedList.toString());
            }
        }
        return client;
    }

    private static SGAgentClient createAgentClient(String ip, int port) {
        if (ProcessInfoUtil.isMac() && CommonUtil.containsIgnoreCase(Consts.LOCALHOST, ip)) {
            //prohibit trying to create connection to the local sg_agent, while the localhost is mac.
            return null;
        }
        SGAgentClient client = null;
        TSocket socket = new TSocket(ip, port, Consts.connectTimeout);
        try {
            socket.setTimeout(Consts.defaultTimeoutInMills);
            socket.open();
        } catch (TTransportException e) {
            //no log. because the log may make the RDs confused
            return null;
        }

        TFramedTransport transport = new TFramedTransport(socket, Consts.defaltMaxResponseMessageBytes);
        TProtocol protocol = new TBinaryProtocol(transport);
        SGAgent.Client iface = new SGAgent.Client(protocol);
        client = new SGAgentClient();
        client.setIface(iface);
        client.setTSocket(socket);

        return client;

    }

    /**
     * check health & locality; if not, try to recreate client
     */
    private static SGAgentClient checkHealthLocality(SGAgentClient client) {

        SGAgentClient newClient = client;
        if (null == client) {
            // TODO 这种情况下需要如何处理?
        } else if (null == client.getTSocket()) {
            newClient = getSGAgentClient(client.getType());
        } else if (!client.isLocal() && CustomizedManager.getCustomizedAgentList().isEmpty()) {
            //If local sg_agent is OK, using it to replace sg_sentinel and destorying the connection of sg_sentinel.
            SGAgentClient localClient = createAgentClient(Consts.LOCALHOST, SGAgentPort);
            if ((null != localClient) && (null != localClient.getTSocket())) {
                client.destory();
                newClient = localClient;
                newClient.setType(client.getType());
            }
        }
        return newClient;
    }

    public static String getMnsAgentIp() {
        String ip = "unknow";
        try {
            if (null != mnsClient) {
                ip = mnsClient.getTSocket().getSocket().getInetAddress().getHostName();
            }
        } catch (Exception e) {
            LOG.debug("getMnsAgentIp:", e);
        }
        return ip;
    }
}
