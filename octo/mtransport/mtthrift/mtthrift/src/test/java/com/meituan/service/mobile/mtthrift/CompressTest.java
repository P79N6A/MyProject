package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.util.NewProtocolUtil;
import org.junit.Test;

import java.io.IOException;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/6/24
 * Description:
 */
public class CompressTest {
    @Test
    public void gzip() throws IOException {
        String str = "123456789123456789";
        byte[] zip = NewProtocolUtil.gZip(str.getBytes());
        zip = NewProtocolUtil.unGZip(zip);
        String str1 = new String(zip);
        System.out.println(str);
        System.out.println(str1);
        assert(str.equals(str1));
    }
}
