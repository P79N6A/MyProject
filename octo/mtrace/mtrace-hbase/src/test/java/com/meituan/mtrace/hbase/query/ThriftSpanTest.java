package com.meituan.mtrace.hbase.query;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

/**
 * @author zhangzhitong
 * @created 9/17/15
 */
public class ThriftSpanTest {
    TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
    TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
    public void testSerializer() {
    }
}
