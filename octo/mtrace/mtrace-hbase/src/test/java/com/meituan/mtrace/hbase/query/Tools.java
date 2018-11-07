package com.meituan.mtrace.hbase.query;

import junit.framework.TestCase;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author zhangzhitong
 * @created 10/22/15
 */
public class Tools extends TestCase {
    public void testGetRowKey() {
        long i = 123L;
        String x = Bytes.toStringBinary(Bytes.toBytes(i));
        System.out.println(x);
    }
    public void testToTimestamp() {
        long i = 1445256234348L;
        System.out.println(i * 5000);
    }
}
