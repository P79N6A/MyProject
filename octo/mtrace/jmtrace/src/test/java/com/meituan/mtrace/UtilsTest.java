package com.meituan.mtrace;

import com.meituan.mtrace.thrift.model.*;
import com.meituan.mtrace.thrift.model.Endpoint;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

public class UtilsTest {
    public Random random = new Random();
    private TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
    private TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
    @Test
    public void testCompress() throws Exception {
        ThriftSpanList thriftSpanList = new ThriftSpanList();
        for (int i = 0; i < 1000; ++i) {
            long traceId = UUID.randomUUID().getLeastSignificantBits();
            for (int j = 0; j < 5; ++j) {
                ThriftSpan span = new ThriftSpan();
                span.setTraceId(traceId);
                span.setSpanId("0." + j);
                span.setSpanName("Utils.testCompress");
                span.setLocal(new Endpoint(11, (short) 90, "com.meituan.mtrace.Test"));
                span.setRemote(new Endpoint(12345, (short) 8080,"com.meituan.mtrace.RemoteTest"));
                //span.setRemote(new Endpoint(0, (short) 0, ""));
                span.setClientSide(false);
                span.setDuration(random.nextInt(100));
                span.setInfraName("mtthrift");
                span.setStart(System.currentTimeMillis());
                span.setStatus(StatusCode.SUCCESS);
                span.setInfraVersion("1.6.4");
                thriftSpanList.addToSpans(span);
            }
        }
        byte[] idata = serializer.serialize(thriftSpanList);
        long t1 = System.nanoTime();
        byte[] data = Utils.compress(idata);
        long t2 = System.nanoTime();
        byte[] dedate = Utils.decompress(data);
        long t3 = System.nanoTime();
        ThriftSpanList newThriftSpanList = new ThriftSpanList();
        deserializer.deserialize(newThriftSpanList, dedate);
        System.out.println(newThriftSpanList.getSpans().get(0));
        System.out.println(idata.length + " " + data.length + " " + dedate.length + " " + (double)dedate.length / data.length);
        System.out.println((t2 - t1) / 1000000 + " " + (t3 - t2) / 1000000);


    }
}
