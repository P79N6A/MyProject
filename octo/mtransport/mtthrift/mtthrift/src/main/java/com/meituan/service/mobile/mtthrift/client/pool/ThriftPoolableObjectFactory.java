package com.meituan.service.mobile.mtthrift.client.pool;

import com.meituan.service.mobile.mtthrift.util.Consts;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;

/**
 * @author YaoZhidong
 * @version 1.0
 * @created 12-9-20
 */
public class ThriftPoolableObjectFactory implements PoolableObjectFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftPoolableObjectFactory.class);

    private String host;
    private int port;
    private int timeOut;
    private int connTimeOut = Consts.connectTimeout;
    private boolean isImplFacebookService = true;
    private boolean async = false;
    private String serverAppKey;

    public ThriftPoolableObjectFactory(String host, int port, int timeOut, int connTimeOut) {
        this("", host, port, timeOut, true, false, connTimeOut);
    }

    public ThriftPoolableObjectFactory(String serverAppKey, String host, int port, int timeOut
            , boolean isImplFacebookService, boolean async, int connTimeOut) {
        this.serverAppKey = serverAppKey;
        this.host = host;
        this.port = port;
        this.timeOut = timeOut;
        this.isImplFacebookService = isImplFacebookService;
        this.async = async;
        this.connTimeOut = connTimeOut;
    }

    @Override
    public void destroyObject(Object tTransport) throws Exception {
        if(null == tTransport)
            return;

        if (tTransport instanceof TSocket) {
            TSocket socket = (TSocket) tTransport;
            if (socket.isOpen()) {
                LOG.debug("destroyObject() host:" + host + ",port:" + port
                        + ",socket:" + socket.getSocket() + ",isOpen:" + socket
                        .isOpen());
                socket.close();
            }
        } else if(tTransport instanceof TNonblockingSocket) {
            TNonblockingSocket socket = (TNonblockingSocket) tTransport;
            if (socket.getSocketChannel().isOpen()) {
                LOG.debug("destroyObject() host:" + host + ",port:" + port
                        + ",isOpen:" + socket.isOpen());
                socket.close();
            }
        }
    }

    /**
     *
     */
    @Override
    public Object makeObject() throws Exception {
        // 丑陋的三次重试
        int count = 3;
        TTransportException exception = null;
        while (count-- > 0) {
            long start = System.currentTimeMillis();
            TTransport transport = null;
            boolean connectSuccess = false;
            try {
                if(async) {//异步
                    transport = new TNonblockingSocket(host, port, this.connTimeOut);
                    LOG.debug("makeObject() {}", ((TNonblockingSocket) transport)
                                    .getSocketChannel().socket());
                } else {//同步
                    transport = new TSocket(host, port,
                            this.connTimeOut);
                    transport.open();
                    ((TSocket)transport).setTimeout(timeOut);
                    LOG.debug("makeObject() {}", ((TSocket) transport).getSocket());
                }
                connectSuccess = true;
                return transport;
            } catch (TTransportException te) {
                exception = te;
                LOG.warn(new StringBuilder("makeObject() ").append(te.getLocalizedMessage()).append(":").append(te.getType()).append("/")
                        .append(host).append(":").append(port).append("/").append(System.currentTimeMillis() - start).toString());
                // 连接超时时返回SocketTimeoutException
                if (!(te.getCause() instanceof SocketTimeoutException))
                    break;
            } catch (Exception e) {
                LOG.warn("makeObject()", e);
                throw new RuntimeException(e);
            } finally {
                if (transport != null && connectSuccess == false) {
                    try {
                        transport.close();
                    } catch (Exception e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            }
        }
        throw new RuntimeException(exception);
    }

    @Override
    public boolean validateObject(Object arg0) {
        try {
            if (arg0 instanceof TSocket) {
                TSocket thriftSocket = (TSocket) arg0;
                if (thriftSocket.isOpen()) {
                    return true;
                } else {
                    LOG.warn("validateObject() failed " + thriftSocket.getSocket());
                    return false;
                }
            } else if(arg0 instanceof TNonblockingSocket) {
                TNonblockingSocket socket = (TNonblockingSocket) arg0;
                if (socket.getSocketChannel().isOpen()) {
                    return true;
                } else {
                    LOG.warn("validateObject() failed " + socket.getSocketChannel().socket());
                    return false;
                }
            } else {
                LOG.warn("validateObject() failed unkown Object:" + arg0);
                return false;
            }
        } catch (Exception e) {
            LOG.warn("validateObject() failed " + e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public void passivateObject(Object arg0) throws Exception {
        // DO NOTHING
    }

    @Override
    public void activateObject(Object arg0) throws Exception {
        // DO NOTHING
    }

}
