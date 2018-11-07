package com.meituan.service.mobile.mtthrift.util;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jiguang on 15/7/13.
 */
public class ThriftSerializeUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftSerializeUtil.class);

    /**
     * @param o Thrift 对象
     * @return 二进制字节数组
     */
    public static byte[] serialize(TBase o) {
        TMemoryBuffer memoryBuffer = new TMemoryBuffer(512);
        TBinaryProtocol outProtocol = new TBinaryProtocol(memoryBuffer);
        try {
            o.write(outProtocol);
        } catch (TException e) {
            LOG.debug("serialize failed...", e);
        }
        byte[] b = new byte[memoryBuffer.length()];
        System.arraycopy(memoryBuffer.getArray(), 0, b, 0, b.length);
        return b;
    }

    /**
     * @param c   对应的类
     * @param b   二进制字节数组
     * @param <O> Thrift 对象
     * @return
     */
    public static <O extends TBase> O deserialize(Class<O> c, byte[] b) {
        if (b == null || c == null) {
            return null;
        }
        TMemoryInputTransport tMemoryInputTransport = new TMemoryInputTransport(
                b);
        TBinaryProtocol inProtocal = new TBinaryProtocol(tMemoryInputTransport);
        try {
            O o = c.newInstance();
            o.read(inProtocal);
            return o;
        } catch (Exception e) {
            LOG.debug("deserialize failed...", e);
        }
        return null;
    }
}
