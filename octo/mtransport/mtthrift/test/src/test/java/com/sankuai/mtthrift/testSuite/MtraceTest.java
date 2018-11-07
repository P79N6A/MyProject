package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.mtrace.RequestHeader;
import com.sankuai.mtthrift.testSuite.hardCode.HardCodeClient;
import com.sankuai.mtthrift.testSuite.hardCode.HardCodeServer;
import com.sankuai.mtthrift.testSuite.idl.MyException;
import com.sankuai.mtthrift.testSuite.idl.TestService;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/2/3
 * Description:
 */
public class MtraceTest {

    @Test
    public void checkMtraceInfo() {
        System.out.println("\n\n**************** Mtrace ********************");
        HardCodeServer server = new HardCodeServer(new ServerImplMtrace() );
        server.run();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        HardCodeClient client = new HardCodeClient(TestService.class);
        client.testNull();
        server.destroy();
    }

    private class MockTTransport extends TTransport {

        @Override public boolean isOpen() {
            return false;
        }

        @Override public void open() throws TTransportException {

        }

        @Override public void close() {

        }

        @Override public int read(byte[] bytes, int i, int i1)
                throws TTransportException {
            return 0;
        }

        @Override public void write(byte[] bytes, int i, int i1)
                throws TTransportException {

        }
    }

    @Test
    public void testWriteNullString() {
        MtraceServerTBinaryProtocol mtraceServerTBinaryProtocol =
                new MtraceServerTBinaryProtocol(new MockTTransport());
        mtraceServerTBinaryProtocol.setSerializeNullStringAsBlank(false);
        String nullString = null;
        try {
            mtraceServerTBinaryProtocol.writeString(nullString);
        } catch (NullPointerException e) {
            StackTraceElement element = e.getStackTrace()[0];
            assert (element.getMethodName().equals("writeString"));
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    public class ServerImplMtrace implements TestService.Iface {

        public String testNull() throws TException {
            RequestHeader rh = MtraceServerTBinaryProtocol.requestHeaderInfo.get();
            System.out.println(rh.toString());
            assert(rh.clientIp.length() > 0);
            assert(rh.getSpanName().length() > 0);
            assert(rh.getSpanId().length() > 0);
            return "";
        }

        public String testException() throws MyException, TException {
            return null;
        }

        @Override
        public int testBaseTypeException() throws MyException, TException {
            return 0;
        }

        public String testMock(String str) throws TException {
            return null;
        }

        public long testLong(long n) throws TException {
            return 0;
        }

        public void testProtocolMisMatch() throws TException {

        }

        public void testTransportException() throws TException {

        }

        public void testTimeout() throws TException {

        }

        public String testReturnNull() throws TException {
            return null;
        }
    }
}
