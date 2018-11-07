package com.sankuai.inf.octo.mns.model;

import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.sgagent.thrift.model.SGAgent;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SGAgentClient {
    private static final Logger LOG = LoggerFactory.getLogger(SGAgentClient.class);
    private TSocket tSocket;
    private SGAgent.Iface iface;
    private ClientType type;

    public SGAgentClient() {
        this(ClientType.temp);
    }

    public SGAgentClient(ClientType type) {
        this.type = type;
    }

    public ClientType getType() {
        return type;
    }

    public void setType(ClientType type) {
        this.type = type;
    }

    public TSocket getTSocket() {
        return tSocket;
    }

    public void setTSocket(TSocket tSocket) {
        this.tSocket = tSocket;
    }

    public SGAgent.Iface getIface() {
        return iface;
    }

    public void setIface(SGAgent.Iface iface) {
        this.iface = iface;
    }

    public void destory() {

        if (null == this.tSocket) {
            return;
        }
        LOG.debug("destroy current tSocket:" + tSocket.getSocket().toString());
        tSocket.close();
        this.tSocket = null;
    }

    public boolean isLocal() {
        boolean local = false;
        try {
            String remoteSocketAddress = this.getTSocket().getSocket().getRemoteSocketAddress().toString();
            if (remoteSocketAddress.contains(Consts.LOCALHOST)) {
                local = true;
            }
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        return local;
    }

    public enum ClientType {
        temp, mns, mcc, trace, multiProto
    }

}
