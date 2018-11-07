package com.sankuai.octo.test.thrift;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Server {

    private static void mtraceConf() {
//        System.setProperty("app.key", "com.sankuai.inf.test");
//        System.setProperty("app.port", "8600");
        System.setProperty("mtrace.collector", "xxxx");
//        System.setProperty("app.tracelog", "true");
        System.setProperty("mtrace.printExceptionStack", "true");
//        System.setProperty("octo.agentHost", "192.168.3.163,192.168.12.185");
    }

    public static void main(String[] args) throws Exception {
        mtraceConf();
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("applicationContext.xml");
    }
}
