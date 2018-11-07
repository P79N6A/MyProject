package com.sankuai.mtthrift.testSuite.annotation;


import org.apache.thrift.TException;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-9-8
 * Time: 下午3:05
 */
public class TestServiceImpl implements TestService {

    public String testNull() throws TException{
        System.out.println("testNull!");
        return "null";
    }

    public String testException() throws MyException, InternalErrorException, TException{
        System.out.println("testException!");
        throw new MyException("My Error");
//        throw new InternalErrorException("Internal Error");
//        throw new TException("T Error");
//        return "";
    }

    public String testMock(String str) throws TException {
        System.out.println("testMock!");
        return str;
    }

    public void testTimeout() throws TException {
        try {
            Thread.sleep(100000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TestResponse testStruct(TestRequest testRequest) throws TException {
        TestResponse testResponse = new TestResponse();
//        testResponse.setUserid(testRequest.getUserid());
        testResponse.setMessage("haha" + testRequest.getMessage());
        testResponse.setSeqid(testRequest.getSeqid());
        return testResponse;
    }

    public int testBaseTypeException() throws MyException, TException {
        System.out.println("testBaseTypeException");
        throw new MyException("My Error");
    }
}
