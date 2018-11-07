package com.sankuai.mtthrift.testSuite;

import com.meituan.mtrace.Endpoint;
import com.meituan.service.mobile.mtthrift.client.cluster.MtThrfitInvokeInfo;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceClientTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.TraceInfoUtil;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import com.sankuai.octo.protocol.MessageType;
import com.sankuai.octo.protocol.TraceInfo;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class HeartbeatTest {

    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static final String localIp = ProcessInfoUtil.getLocalIpV4();
    private static final int port = 9115;
    private static CustomizedTFramedTransport transport;
    private static MtThrfitInvokeInfo mtThrfitInvokeInfo;
    private static String methodName = "testString";


    @BeforeClass
    public static void start() throws InterruptedException, TTransportException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/heartbeat/server.xml");
        Thread.sleep(5000);
    }

    @Before
    public void initClient() throws TTransportException {

        TSocket socket = new TSocket(localIp, port, 10000);
        socket.open();
        transport = new CustomizedTFramedTransport(socket);
        transport.setUnifiedProto(true);
        transport.setServiceName(Twitter.class.getName() );
        Endpoint localEndpoint = new Endpoint("appkey", localIp, port);
        TraceInfo traceInfo = TraceInfoUtil.getTraceInfo(Twitter.class.getSimpleName() + "." + methodName, localIp);
        transport.setTraceInfo(traceInfo);
        transport.setProtocol(Consts.protocol);

        mtThrfitInvokeInfo = new MtThrfitInvokeInfo("serverAppkey",
                Twitter.class.getSimpleName() + "." + methodName,
                localIp, 0,localIp, port);
        mtThrfitInvokeInfo.setUniProto(true);

    }

    @AfterClass
    public static void stop() {
        if(null != transport)
            transport.close();
        serverBeanFactory.destroy();
    }


//    @Test
    public void normalHeartbeat() throws TException {
        transport.setMessageType(MessageType.NormalHeartbeat);
        MtraceClientTBinaryProtocol protocol = new MtraceClientTBinaryProtocol(transport, mtThrfitInvokeInfo);
        protocol.getTransport().flush();
        System.out.println(transport.getHeaderInfo());

        transport.readFrame();
        System.out.println(transport.getHeaderInfo());
    }

    @Test
    public void normalHeartbeaWithBody() throws TException {
        transport.setMessageType(MessageType.NormalHeartbeat);
        MtraceClientTBinaryProtocol protocol = new MtraceClientTBinaryProtocol(transport, mtThrfitInvokeInfo);
        int seqid_ = 1;
        protocol.writeMessageBegin(new TMessage(methodName, (byte)1, seqid_));
        Twitter.testString_args args = new Twitter.testString_args();
        args.setS("hello!");
        args.write(protocol);
        protocol.writeMessageEnd();
        protocol.getTransport().flush();
        System.out.println(transport.getHeaderInfo());


        Twitter.testString_result result = new Twitter.testString_result();
        TMessage msg = protocol.readMessageBegin();
        System.out.println(transport.getHeaderInfo());
        if(msg.type == 3) {
            TApplicationException x = TApplicationException.read(protocol);
            protocol.readMessageEnd();
            throw x;
        } else if(msg.seqid != seqid_) {
            throw new TApplicationException(4, methodName + " failed: out of sequence response");
        } else {
            result.read(protocol);
            protocol.readMessageEnd();
        }
        if (result.isSetSuccess()) {
            System.out.println(result.success);
        } else {
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "testString failed: unknown result");
        }
//        Twitter.Client client = new Twitter.Client(protocol);
//        client.testString("hello!");

    }

    @Test
    public void scannerHeartbeat() throws TTransportException {

            transport.setMessageType(MessageType.ScannerHeartbeat);
            MtraceClientTBinaryProtocol protocol = new MtraceClientTBinaryProtocol(transport, mtThrfitInvokeInfo);
            protocol.getTransport().flush();

            System.out.println("\n" + transport.getHeaderInfo() + "\n");

            transport.readFrame();

            System.out.println("\n" + transport.getHeaderInfo() + "\n");

    }

}
