package com.sankuai.inf.octo.mns;

import junit.framework.Assert;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/3/11
 * Description:
 */
public class SocketClientPortTests {

    private int localPort = 40000;
    @Test
    public void clientPortConfig() throws IOException {
        TSocket socket = new TSocket("10.4.241.125", 5266, Consts.connectTimeout);
        socket.getSocket().bind(new InetSocketAddress(localPort));
        try {
            socket.open();
            socket.setTimeout(Consts.defaultTimeoutInMills);
            System.out.println(socket.getSocket());
            Assert.assertTrue(socket.getSocket().toString().contains(localPort + ""));
        } catch (TTransportException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }

    }
}
