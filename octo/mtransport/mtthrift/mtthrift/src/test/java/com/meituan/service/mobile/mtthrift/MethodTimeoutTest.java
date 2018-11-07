package com.meituan.service.mobile.mtthrift;

import com.meituan.service.mobile.mtthrift.client.invoker.MTThriftMethodInterceptor;
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.meituan.service.mobile.mtthrift.util.MtConfigUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodTimeoutTest {
    private static Logger logger = LoggerFactory.getLogger(AuthorUtilTest.class);

    @Before
    public void setUp(){
        MtConfigUtil mtConfigUtil = new MtConfigUtil();
        if(MtConfigUtil.isMtConfigClientInitiated()){
            System.out.println("MtConfig is initiated");
        }
    }

    @Test
    public void methodTimeout(){
        ThriftClientProxy proxy = new ThriftClientProxy();
        try {
            proxy.afterPropertiesSet();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
