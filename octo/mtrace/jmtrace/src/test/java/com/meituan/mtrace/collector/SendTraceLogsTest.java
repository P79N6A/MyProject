package com.meituan.mtrace.collector;

import com.meituan.mtrace.octo.OctoCollector;
import com.meituan.mtrace.thrift.model.Annotation;
import com.meituan.mtrace.thrift.model.Endpoint;
import com.meituan.mtrace.thrift.model.ThriftSpan;
import com.meituan.mtrace.thrift.model.ThriftSpanList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * @author zhangzhitong
 * @created 10/22/15
 */
public class SendTraceLogsTest {

    public static void main(String[] args) throws InterruptedException {
        //System.setProperty("octo.agentHost", "10.32.98.121:5266");
        //String host = System.getProperty("octo.agentHost");
        //System.out.println("sgagent address : " + host);
        Logger logger = LoggerFactory.getLogger("root");
        OctoCollector octoCollector = new OctoCollector();
        ThriftSpanList thriftSpanList = new ThriftSpanList();
        long traceId = 123;
        Endpoint local = new Endpoint(0, (short)0, "com.meituan.mtrace.ClientTestA");
        Endpoint remote = new Endpoint(0, (short)0, "com.meituan.mtrace.ClientTestB");
        ThriftSpan thriftSpan = new ThriftSpan(traceId, "0", "clientTest", local, remote, System.currentTimeMillis(), 10, true);
        thriftSpanList.addToSpans(thriftSpan);

        octoCollector.sendTraceLogs(thriftSpanList);

    }
}
