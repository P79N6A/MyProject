package com.sankuai.mtthrift.testSuite.hardCode;

import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig;
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.sankuai.mtthrift.testSuite.idl.TestService;
import org.apache.thrift.TException;

/**
 * Created by jiguang on 15/3/27.
 */
public class HardCodeClient {
    private ThriftClientProxy thriftClientProxy;
    private TestService.Iface iface;
    private String appkey = "clientAppkey";

    public HardCodeClient(Class<?> serviceClass) {
        this(serviceClass, 5000);
    }

    public HardCodeClient(Class<?> serviceClass, int timeout) {
        this(serviceClass, timeout, 12345);
    }

    public HardCodeClient(Class<?> serviceClass, int timeout, int port) {
        this(serviceClass, "clientAppkey", timeout, port);
    }
    public HardCodeClient(Class<?> serviceClass, String appkey, int timeout, int port) {

        MTThriftPoolConfig mtThriftPoolConfig = new MTThriftPoolConfig();
        mtThriftPoolConfig.setMaxActive(500);
        mtThriftPoolConfig.setMaxIdle(2);
        mtThriftPoolConfig.setMinIdle(1);
        mtThriftPoolConfig.setMaxWait(3000);
        mtThriftPoolConfig.setTestOnBorrow(false);

        thriftClientProxy = new ThriftClientProxy();
        thriftClientProxy.setMtThriftPoolConfig(mtThriftPoolConfig);
        thriftClientProxy.setServiceInterface(serviceClass);
        thriftClientProxy.setTimeout(timeout);
        thriftClientProxy.setServerIpPorts("127.0.0.1:" + port);
        thriftClientProxy.setAppKey(appkey);
        try {
            thriftClientProxy.afterPropertiesSet();
            iface = (TestService.Iface) thriftClientProxy.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public HardCodeClient(Class<?> serviceClass, String serverList) {

        MTThriftPoolConfig mtThriftPoolConfig = new MTThriftPoolConfig();
        mtThriftPoolConfig.setMaxActive(500);
        mtThriftPoolConfig.setMaxIdle(2);
        mtThriftPoolConfig.setMinIdle(1);
        mtThriftPoolConfig.setMaxWait(3000);
        mtThriftPoolConfig.setTestOnBorrow(false);

        thriftClientProxy = new ThriftClientProxy();
        thriftClientProxy.setMtThriftPoolConfig(mtThriftPoolConfig);
        thriftClientProxy.setServiceInterface(serviceClass);
        thriftClientProxy.setTimeout(5000);
        thriftClientProxy.setServerIpPorts(serverList);
        thriftClientProxy.setAppKey(appkey);
        try {
            thriftClientProxy.afterPropertiesSet();
            iface = (TestService.Iface) thriftClientProxy.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testNull() {
        try {
            iface.testNull();
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public long testLong(long n) {
        try {
           return iface.testLong(n);
        } catch (TException e) {
            e.printStackTrace();
        }
        return -1;

    }

    public void testFailover() {
        try {
            iface.testNull();
        } catch (TException e) {
            System.out.println(e.getMessage());
            assert (e.getMessage().contains("can't get valid connection") );
        }
    }

    public TestService.Iface getIface() {
        return iface;
    }


}
