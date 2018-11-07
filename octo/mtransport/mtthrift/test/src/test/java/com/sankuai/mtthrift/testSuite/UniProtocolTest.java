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
import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.nio.ByteBuffer;

public class UniProtocolTest {

    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static final String localIp = ProcessInfoUtil.getLocalIpV4();
    private static final int port = 9015;
    private static CustomizedTFramedTransport transport;
    private static MtThrfitInvokeInfo mtThrfitInvokeInfo;
    private static String methodName = "testString";
    private static String strArg = "hello!";
    private final TByteArrayOutputStream writeBuffer_ = new TByteArrayOutputStream(1024);
    private final byte[] i32buf = new byte[4];
    private ByteBuffer buffer_;



    @BeforeClass
    public static void start() throws InterruptedException, TTransportException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/uniProtocol/server.xml");
        Thread.sleep(5000);
        initClient();

    }

    private static void initClient() throws TTransportException {
        System.out.println(localIp + ":" + port);
        TSocket socket = new TSocket(localIp, port, 2000);
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

    @Test
    public void testProtocol() throws TException {
        transport.setMessageType(MessageType.Normal);
        transport.setUnifiedProto(true);
        MtraceClientTBinaryProtocol protocol = new MtraceClientTBinaryProtocol(transport, mtThrfitInvokeInfo);
        int seqid_ = 1;
        protocol.writeMessageBegin(new TMessage(methodName, (byte)1, seqid_));
        Twitter.testString_args args = new Twitter.testString_args();
        args.setS(strArg);
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
            assert (result.success.equals(strArg));
        } else {
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "testString failed: unknown result");
        }
    }

//    @Test
    public void protocolCompareBenchmark() throws TException {
        //TODO: 组装一个旧协议二进制请求包

        boolean unifiedProto = false;
        transport.setMessageType(MessageType.Normal);
        transport.setUnifiedProto(true);
        MtraceClientTBinaryProtocol protocol = new MtraceClientTBinaryProtocol(transport, mtThrfitInvokeInfo);
        int seqid_ = 1;
        protocol.writeMessageBegin(new TMessage(methodName, (byte)1, seqid_));
        Twitter.testString_args args = new Twitter.testString_args();
        args.setS(strArg);
        args.write(protocol);
        protocol.writeMessageEnd();

        if (!unifiedProto) {
            byte[] buf = this.writeBuffer_.get();
            int len = this.writeBuffer_.len();
            this.writeBuffer_.reset();
            CustomizedTFramedTransport.encodeFrameSize(len, this.i32buf);
            writeBuffer_.write(this.i32buf, 0, 4);
            writeBuffer_.write(buf, 0, len);

        }


        //TODO: 循环 解析此二进制数据, 封装成响应包


        //TODO: 组装一个新协议二进制请求包
        //TODO: 循环 解析此二进制数据, 封装成新协议响应包
    }


}
