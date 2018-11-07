package com.meituan.service.mobile.mtthrift.client.pool;

import org.apache.thrift.transport.TSocket;

/**
 * @author YaoZhidong
 * @version 1.0
 * @created 12-9-20
 * @deprecated
 */
public interface IMTThriftPool {
    /**
     * 取链接池中的一个链接
     * 
     * @return
     */
    public TSocket getConnection();

    /**
     * 返回断开的或出现异常的链接
     * 
     * @param socket
     */
    public void returnBrokenConnection(TSocket socket);

    /**
     * 返回链接
     * 
     * @param socket
     */
    public void returnConnection(TSocket socket);
}
