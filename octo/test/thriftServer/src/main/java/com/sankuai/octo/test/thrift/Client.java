package com.sankuai.octo.test.thrift;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Client {

    private static void mtraceConf() {
//        System.setProperty("app.key","com.sankuai.inf.test");
//        System.setProperty("app.port","8600");
        System.setProperty("mtrace.collector", "log");
        System.setProperty("app.tracelog", "true");
        System.setProperty("mtrace.printExceptionStack", "true");
    }

    public static void main(String[] args) throws Exception {
        mtraceConf();
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("client.xml");

    }
}
