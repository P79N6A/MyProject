package com.sankuai.mtthrift.testSuite.flowcopy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meituan.service.mobile.mtthrift.server.flow.FlowCopyConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FlowCopyConfigTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testJsonToObject1() throws IOException {
        String flowCopyCfgJson = "{\"enable\":true,\"taskId\":7460347472719568411,\"brokerUrl\":\"http://10.72.208.105:8080/api/record/\",\"ipport\":\"10.72.208.105:8889\",\"cfgDetail\":{\"serviceName\":\"com.sankuai.octo.benchmark.thrift.EchoService\",\"methodNames\":[\"sendString\"],\"sumCount\":1000,\"serverIps\":[\"172.18.185.152\"],\"tagged\":true,\"savePath\":\"testPath\",\"description\":\"æµ\u008Bè¯\u0095\"}}\n";
        FlowCopyConfig cfg = mapper.readValue(flowCopyCfgJson, FlowCopyConfig.class);
        Assert.assertEquals(cfg.getTaskId(), 7460347472719568411l);
        Assert.assertEquals(cfg.isEnable(), true);
        Assert.assertEquals(cfg.getBrokerUrl(), "http://10.72.208.105:8080/api/record/");
        Assert.assertEquals(cfg.getIpport(), "10.72.208.105:8889");
        Assert.assertEquals(cfg.getCfgDetail().getMethodNames().size(), 1);
        Assert.assertEquals(cfg.getCfgDetail().getMethodNames().get(0), "sendString");
        Assert.assertEquals(cfg.getCfgDetail().getSumCount(), 1000);
        Assert.assertEquals(cfg.getCfgDetail().getServerIps().get(0), "172.18.185.152");
        Assert.assertEquals(cfg.getCfgDetail().isTagged(), true);
        System.out.println(cfg);

        testObjectToJson(cfg);
    }

    @Test
    public void testJsonToObject2() throws IOException {
        String flowCopyCfgJson = "{\"enable\":true,\n" +
                "\"taskId\":7460347472719568411,\n" +
                "\"brokerUrl\":\"http://10.72.208.105:8080/api/record/\",\n" +
                "\"ipport\":\"10.72.208.105:8889\"}";
        FlowCopyConfig cfg = mapper.readValue(flowCopyCfgJson, FlowCopyConfig.class);
        Assert.assertEquals(cfg.getTaskId(), 7460347472719568411l);
        Assert.assertEquals(cfg.isEnable(), true);
        Assert.assertEquals(cfg.getBrokerUrl(), "http://10.72.208.105:8080/api/record/");
        Assert.assertEquals(cfg.getCfgDetail(), null);
    }

    @Test
    public void testJsonToObject3() throws IOException {
        String flowCopyCfgJson = "{\"taskId\":7460347472719568411,\n" +
                "\"brokerUrl\":\"http://10.72.208.105:8080/api/record/\",\n" +
                "\"ipport\":\"10.72.208.105:8889\"}";
        FlowCopyConfig cfg = mapper.readValue(flowCopyCfgJson, FlowCopyConfig.class);
        Assert.assertEquals(cfg.getTaskId(), 7460347472719568411l);
        Assert.assertEquals(cfg.isEnable(), false);
        Assert.assertEquals(cfg.getBrokerUrl(), "http://10.72.208.105:8080/api/record/");
        Assert.assertEquals(cfg.getCfgDetail(), null);
    }

    private void testObjectToJson(FlowCopyConfig cfg) {
        try {
            String json = mapper.writeValueAsString(cfg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
