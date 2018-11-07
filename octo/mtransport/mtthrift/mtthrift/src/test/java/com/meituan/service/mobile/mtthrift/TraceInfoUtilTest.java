package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.util.TraceInfoUtil;
import org.junit.Test;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/10/25
 * Description:
 */
public class TraceInfoUtilTest {
    private int count = 10;
    private String appkey = "com.sankuai.mttransport.testSuite";


    @Test
    public void mtraceInit() {
        for(int i = 0; i < count; i++) {
            long curTime = System.currentTimeMillis();
            TraceInfoUtil.mtraceInitAtClient(appkey);
            System.out.println(System.currentTimeMillis() - curTime);
        }
    }

    @Test
    public void mtraceInitMultiAppkey() {
        for(int i = 0; i < count; i++) {
            long curTime = System.currentTimeMillis();
            TraceInfoUtil.mtraceInitAtClient(appkey + i);
            System.out.println(System.currentTimeMillis() - curTime);
        }
    }

    @Test
    public void mtraceInitEmptyAppkey() {
        for(int i = 0; i < count; i++) {
            long curTime = System.currentTimeMillis();
            TraceInfoUtil.mtraceInitAtClient("");
            System.out.println(System.currentTimeMillis() - curTime);
        }
    }

    @Test
    public void mtraceInitNullAppkey() {
        for(int i = 0; i < count; i++) {
            long curTime = System.currentTimeMillis();
            TraceInfoUtil.mtraceInitAtClient(null);
            System.out.println(System.currentTimeMillis() - curTime);
        }
    }


}
