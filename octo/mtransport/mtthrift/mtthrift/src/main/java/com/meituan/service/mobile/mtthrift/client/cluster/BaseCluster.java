package com.meituan.service.mobile.mtthrift.client.cluster;

import com.meituan.service.mobile.mtthrift.client.cell.RouterMetaData;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.client.pool.ThriftPoolableObjectFactory;
import com.meituan.service.mobile.mtthrift.netty.channel.IChannelPool;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.sankuai.inf.octo.mns.ProcessInfoUtil;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * User: YangXuehua
 * Date: 13-8-8
 * Time: 下午6:56
 */
public abstract class BaseCluster implements ICluster {
    private final static Logger logger = LoggerFactory.getLogger(BaseCluster.class);

    private boolean isImplFacebookService = true;
    private boolean async = false;// 是否异步
    protected String serverAppKey = "";
    protected String serviceName = "";
    protected int connTimeout = Consts.connectTimeout;

    public BaseCluster(boolean isImplFacebookService, boolean async) {
        this.isImplFacebookService = isImplFacebookService;
        this.async = async;
    }

    public boolean isAsync() {
        return async;
    }

    protected ObjectPool createPool(String host, int port, int timeOut, GenericObjectPool.Config poolConfig, int connTimeout) {
        if (host.equals("127.0.0.1") || host.equals("localhost")) {
            host = ProcessInfoUtil.getLocalIpV4();
        }
        GenericObjectPool genericObjectPool = new GenericObjectPool(
                new ThriftPoolableObjectFactory(serverAppKey, host, port,
                        timeOut, isImplFacebookService, async, connTimeout), poolConfig);
        if(0 == poolConfig.minIdle)
            genericObjectPool.setMinEvictableIdleTimeMillis(poolConfig.timeBetweenEvictionRunsMillis);
        return genericObjectPool;
    }

    protected void destroyPool(ServerConn serverConn) {
        ObjectPool pool = serverConn.getObjectPool();
        if (pool != null) {
            try {
                pool.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        serverConn.setObjectPool(null);

        IChannelPool channelPool = serverConn.getChannelPool();
        if (channelPool != null) {
            try {
                channelPool.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        serverConn.setChannelPool(null);
    }

    @Override
    public abstract List<ServerConn> getServerConnList();

    @Override
    public void destroy() {
        List<ServerConn> serverConnList = getServerConnList();
        if (serverConnList != null) {
            for (ServerConn serverConn : serverConnList)
                try {
                    serverConn.getObjectPool().close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
        }
    }

    @Override
    public abstract void updateServerConn(ServerConn serverConn) ;

    @Override
    public List<ServerConn> getServerConnList(RouterMetaData routerMetaData) {
        return getServerConnList();
    }
}
