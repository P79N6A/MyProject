package com.sankuai.mtthrift.testSuite;


import com.sankuai.mtthrift.testSuite.hardCode.HardCodeServer;
import com.sankuai.mtthrift.testSuite.idl.MyException;
import com.sankuai.mtthrift.testSuite.idl.TestService;
import org.apache.thrift.TException;
import org.junit.Test;

public class ServerDegradeTest {

    String remoteAppkey = "com.sankuai.octo.testMTthrift";
    int port = 10213;
    @Test
    public void testServerDegrade() throws InterruptedException {

        HardCodeServer server = new HardCodeServer(new ServerDegreade(), remoteAppkey, port );
        server.run();
        Thread.sleep(10000);
//        final HardCodeClient client = new HardCodeClient(TestService.class,"com.sankuai.octo.testMTthrift.Client",
//                10000, port);
//
//        for(int i = 0; i < 300; i++) {
//            System.out.println(i);
//            client.testLong(1L);
//            Thread.sleep(1000);
//        }

//        Thread.sleep(10000);
        server.destroy();
    }


    public class ServerDegreade implements TestService.Iface {

        public String testNull() throws TException {
            return "";
        }

        public String testException() throws MyException, TException {
            return "";
        }

        @Override
        public int testBaseTypeException() throws MyException, TException {
            return 0;
        }

        public String testMock(String str) throws TException {
            return "";
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
