package com.meituan.service.mobile.mtthrift.client.cluster;

import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig;
import com.meituan.service.mobile.mtthrift.netty.channel.NettyChannelPool;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: YangXuehua
 * Date: 13-8-8
 * Time: 上午11:21
 */
public class DirectlyCluster extends BaseCluster {
    private List<ServerConn> serverConns = new ArrayList<ServerConn>();

    public DirectlyCluster(Set<Server> servers, MTThriftPoolConfig poolConfig, int timeOut
            , boolean isImplFacebookService, boolean async, String serviceName, int connTimeout, boolean isNettyIO) {
        super(isImplFacebookService, async);
        this.serviceName = serviceName;
        for (Server server : servers) {
            ServerConn serverConn = new ServerConn();
            serverConn.setServer(server);

            String host = server.getIp();
            int port = server.getPort();

            if (isNettyIO) {
                InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
                boolean remoteUniProto = server.isUnifiedProto();
                serverConn.setChannelPool(new NettyChannelPool(poolConfig, this, serverConn, connTimeout));
            } else {
                serverConn.setObjectPool(createPool(host, port, timeOut, poolConfig, connTimeout));
            }
            serverConns.add(serverConn);
        }
    }

    @Override
    public List<ServerConn> getServerConnList() {
        return serverConns;
    }

    @Override
    public void destroy() {
        for (ServerConn serverConn : serverConns) {
            destroyPool(serverConn);
        }
        serverConns.clear();
    }


    @Override
    public void updateServerConn(ServerConn serverConn) {
        List<ServerConn> _serverConns = new ArrayList<ServerConn>();
        for (ServerConn conn : serverConns) {
            if (conn.getServer().getIp().equalsIgnoreCase(serverConn.getServer().getIp()) &&
                    conn.getServer().getPort() == serverConn.getServer().getPort()) {
                _serverConns.add(serverConn);
            } else {
                _serverConns.add(conn);
            }
        }
        serverConns = _serverConns;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
