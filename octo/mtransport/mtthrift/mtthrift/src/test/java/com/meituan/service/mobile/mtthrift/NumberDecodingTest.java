package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/6/14
 * Description:
 */
public class NumberDecodingTest {
    @Test
     public void I32Encoding() throws TTransportException {
        int i = 999999;
        byte[] i32bytes = new byte[4];
        CustomizedTFramedTransport.encodeFrameSize(i, i32bytes);
//        System.out.println(CustomizedTFramedTransport.decodeFrameSize(i32bytes));
        System.out.println(CustomizedTFramedTransport.decodeFrameSize(i32bytes, 0, 4));



    }
    @Test
    public void I16Encoding() throws TTransportException {
        short i = 99;
        byte[] i16bytes = new byte[2];
        CustomizedTFramedTransport.encodeFrameSize(i, i16bytes);
//        System.out.println(CustomizedTFramedTransport.decodeI16(i16bytes));
        System.out.println(CustomizedTFramedTransport.decodeFrameSize(i16bytes, 0, 2));

    }
}
