package com.sankuai.mtthrift.testSuite.idl;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;

public class TestServiceImpl implements TestService.Iface {

    public String testNull() throws TException {
        String str1 = null;
        str1.length();
        return "";
    }

    public String testException() throws MyException, TException {
        System.out.println("testException!");
        throw new MyException("error");
    }

    @Override
    public int testBaseTypeException() throws MyException, TException {
        System.out.println("testBaseTypeException!");
        throw new MyException("error");
    }

    public String testMock(String str) throws TException {
        System.out.println("testMock!");
        return str;
    }

    public long testLong(long n) throws TException {
        return n;
    }

    public void testProtocolMisMatch() throws TException {
        throw new TProtocolException("testProtocolMisMatch");
    }

    public void testTransportException() throws TException {
        throw new TTransportException("testTransportException");
    }

    public void testTimeout() throws TException {
        try {
            Thread.sleep(1000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public String testReturnNull() throws TException {
        return null;
    }
}
