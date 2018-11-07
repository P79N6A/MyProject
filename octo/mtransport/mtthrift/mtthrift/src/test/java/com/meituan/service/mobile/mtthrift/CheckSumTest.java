package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.util.NewProtocolUtil;
import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.Consts;
import com.meituan.service.mobile.mtthrift.util.HeaderUtil;
import com.meituan.service.mobile.mtthrift.util.ThriftSerializeUtil;
import com.sankuai.octo.protocol.TraceInfo;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.thrift.TException;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/6/21
 * Description:
 */
public class CheckSumTest {
//    @Test
    // http://wiki.sankuai.com/x/oKYlHQ
    public void checkSumWithByteBuffer() throws TException {

        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setClientAppkey("clientAppkey");
        byte[] headerBytes = HeaderUtil.headerSerialize(HeaderUtil.headerAsRequest(1L, "serviceName", traceInfo));
        short headLength = (short)headerBytes.length;
        byte[] headerLenBytes = new byte[2];
        CustomizedTFramedTransport.encodeFrameSize(headLength, headerLenBytes);

        byte[] body = ThriftSerializeUtil.serialize(new SGService());
        int totalLength = 2 + headLength + body.length + 4;
        byte[] totalLenBytes = new byte[4];
        CustomizedTFramedTransport.encodeFrameSize(totalLength, totalLenBytes);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + 4 + totalLength);

        byteBuffer.put(Consts.magic);
        byteBuffer.put(Consts.version);
        byteBuffer.put(Consts.protocol);
        byteBuffer.put(totalLenBytes);

        byteBuffer.put(headerLenBytes);
        byteBuffer.put(headerBytes);
        byteBuffer.put(body);
        byte[] checkSum1 = NewProtocolUtil.getChecksum(byteBuffer.array());
//        System.out.println(checkSum1);

    }

//    @Test
    public void checkSumWithStringBuilder() throws TException {

        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setClientAppkey("clientAppkey");
        byte[] headerBytes = HeaderUtil.headerSerialize(HeaderUtil.headerAsRequest(1L, "serviceName", traceInfo));
        short headLength = (short)headerBytes.length;
        byte[] headerLenBytes = new byte[2];
        CustomizedTFramedTransport.encodeFrameSize(headLength, headerLenBytes);
        byte[] body = ThriftSerializeUtil.serialize(new SGService());
        int totalLength = 2 + headLength + body.length + 4;
        byte[] totalLenBytes = new byte[4];
        CustomizedTFramedTransport.encodeFrameSize(totalLength, totalLenBytes);

        StringBuilder str4Checksum = new StringBuilder(2000);
        str4Checksum.append(Consts.magic);
        str4Checksum.append(Consts.version);
        str4Checksum.append(Consts.protocol);

        str4Checksum.append(totalLenBytes);
        str4Checksum.append(headerLenBytes);
        str4Checksum.append(headerBytes);
        str4Checksum.append(body);

        byte[] checkSum1 = NewProtocolUtil.getChecksum(str4Checksum.toString().getBytes());
//        System.out.println(checkSum1.toString());

    }

    @Test
    public void performanceByteBuff() throws TException {
        int count = 1024 * 1024;
        int i = 0;
        long now = System.currentTimeMillis();
        while(i < count) {
            checkSumWithByteBuffer();
            i++;
        }
        System.out.println("performanceByteBuff:" + (System.currentTimeMillis() - now));
    }

    @Test
    public void performanceStringBuilder() throws TException {
        int count = 1024 * 1024;
        int i = 0;
        long now = System.currentTimeMillis();
        while(i < count) {
            checkSumWithStringBuilder();
            i++;
        }
        System.out.println("performanceStringBuilder:" + (System.currentTimeMillis() - now));
    }
}
