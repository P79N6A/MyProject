package com.meituan.service.mobile.mtthrift.client.model;

import com.meituan.service.mobile.mtthrift.netty.channel.IChannelPool;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.io.Serializable;

/**
 * User: YangXuehua
 * Date: 13-8-8
 * Time: 上午11:31
 */
public class ServerConn implements Serializable {

    private static final long serialVersionUID = 1852506157407273889L;

    private Server server;
    private GenericObjectPool.Config connPoolConf;
    private ObjectPool objectPool;
    private IChannelPool channelPool;
    private String swimlane;
    private String cell;

    public ServerConn(Server server) {
        this.server = server;
    }

    public ServerConn() {

    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public GenericObjectPool.Config getConnPoolConf() {
        return connPoolConf;
    }

    public void setConnPoolConf(GenericObjectPool.Config connPoolConf) {
        this.connPoolConf = connPoolConf;
    }

    public ObjectPool getObjectPool() {
        return objectPool;
    }

    public void setObjectPool(ObjectPool objectPool) {
        this.objectPool = objectPool;
    }

    public IChannelPool getChannelPool() {
        return channelPool;
    }

    public void setChannelPool(IChannelPool channelPool) {
        this.channelPool = channelPool;
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    @Override
    public String toString() {
        return "ServerConn{" + "server=" + server +
                ", swimlane=\'" + swimlane + "\'" +
                ", cell=\'" + cell + "\'" +
                '}';
    }
}
