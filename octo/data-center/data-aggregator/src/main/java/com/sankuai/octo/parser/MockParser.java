package com.sankuai.octo.parser;

import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher;
import org.apache.thrift.TException;

import java.util.List;

public class MockParser implements Parser.Iface {

    @Override
    public int sendMetric(Metric metric) throws TException {
        System.out.println(metric);
        return 0;
    }

    @Override
    public int sendMetrics(List<Metric> metrics) throws TException {
        System.out.println(metrics);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        MockParser mockParser = new MockParser();
        ThriftServerPublisher publisher = new ThriftServerPublisher();
        publisher.setServiceInterface(Parser.class);
        publisher.setServiceImpl(mockParser);
        publisher.setPort(8890);
        publisher.publish();

        Thread.sleep(1000000);
    }
}
