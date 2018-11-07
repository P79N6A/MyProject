package com.meituan.service.mobile.mtthrift.jiguang;

import org.apache.thrift.TException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by jiguang on 14-8-12.
 */
public class Client implements FactoryBean<Object>, ApplicationContextAware,
        InitializingBean {


    private  Object thriftClientProxy;
    private TestService testService;

    private  void initClient() {
        testService = (TestService) thriftClientProxy;


    }
    private void query(int i) {


        try {
            TestRequest testRequest = new TestRequest();
            testRequest.setUserid(123);
            testRequest.setName("土豆");
            testRequest.setMessage("你是谁");
            testRequest.setSeqid(i);

//            long testResponse = testService.method2(i);
            TestResponse testResponse = testService.method1(testRequest);
            System.out.println(testResponse);

        } catch (TException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    public void test() {
        int i = 0;
        while(true) {
            System.out.println("Trying, ---   i = " + i++ + "    ---");
            query(i);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        System.exit(0);

    }

    public static void main(String[] args) {
        Client client = new Client();

        try {
            client.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void setApplicationContext(
            ApplicationContext applicationContext) throws BeansException {
        ApplicationContext applicationContext1 = applicationContext;
    }

    @Override public Object getObject() throws Exception {
        return this;
    }

    @Override public Class<?> getObjectType() {
        return this.getClass();
    }

    @Override public boolean isSingleton() {
        return true;
    }

    @Override public void afterPropertiesSet() throws Exception {
        initClient();
        test();
    }

    public void setThriftClientProxy(Object thriftClientProxy) {
        this.thriftClientProxy = thriftClientProxy;
    }


}
