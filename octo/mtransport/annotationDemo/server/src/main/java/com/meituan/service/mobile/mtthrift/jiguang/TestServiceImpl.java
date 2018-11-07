package com.meituan.service.mobile.mtthrift.jiguang;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;

/**
 * Created by jiguang on 15/2/26.
 */
public class TestServiceImpl implements TestService {
    private final static Log logger = LogFactory.getLog(
            TestServiceImpl.class);
    @Override public TestResponse method1(TestRequest testRequest)
            throws TException {
        TestResponse testResponse = new TestResponse();
        testResponse.setUserid(testRequest.getUserid());
        testResponse.setMessage("haha" + testRequest.getMessage());
        testResponse.setSeqid(testRequest.getSeqid() );
        logger.info(testRequest.toString() );
        logger.info(testResponse.toString() );
        try {
            logger.info("sleepping");
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return testResponse;
    }

    @Override public long method2(int i) throws TException {
        return i * 10;
    }

    @Override public String method3() throws TException {
        return "333";
    }

    @Override public String methodNPE()  {
        String str = null;
        str.length();
        return str;
    }
}
