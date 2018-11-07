package com.sankuai.mtthrift.testSuite;

import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.client.cell.ICellPolicy;
import com.meituan.service.mobile.mtthrift.client.cell.RouterMetaData;
import com.meituan.service.mobile.mtthrift.client.cluster.ICluster;
import com.meituan.service.mobile.mtthrift.client.cluster.OctoAgentCluster;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig;
import com.sankuai.inf.octo.mns.InvokeProxy;
import com.sankuai.sgagent.thrift.model.ProtocolResponse;
import com.sankuai.sgagent.thrift.model.SGService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 2018/1/9
 * Time: 下午4:49
 */
public class RouterTest {

    private static SGService service1;
    private static SGService service2;
    private static SGService service3;
    private static SGService service4;
    private static SGService service5;
    private static SGService service6;
    private static SGService service7;
    private static SGService service8;
    private static SGService service9;
    private static SGService service10;
    private static SGService service11;
    private static SGService service12;
    private static SGService service13;
    private static SGService service14;

    private static MTThriftPoolConfig mtThriftPoolConfig;

    private static List<SGService> input;
    private static List<SGService> expect;
    private static List<ServerConn> output;

    @BeforeClass
    public static void start() throws InterruptedException {

        mtThriftPoolConfig = new MTThriftPoolConfig();
        mtThriftPoolConfig.setMaxActive(100);
        mtThriftPoolConfig.setMaxIdle(100);
        mtThriftPoolConfig.setMinIdle(5);
        mtThriftPoolConfig.setMaxWait(500);
        mtThriftPoolConfig.setTestOnBorrow(false);

        //权重为10
        service1 = new SGService("com.meituan.server", "", "1.1.1.1", 9001, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service1.setCell("");
        service1.setSwimlane("");

        //权重为10
        service2 = new SGService("com.meituan.server", "", "2.2.2.2", 9001, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service2.setCell("");
        service2.setSwimlane("");

        //权重为0.01
        service3 = new SGService("com.meituan.server", "", "3.3.3.3", 9001, 10, 2, 0, 0, 0, "", 0.01, 0, "thrift", null, (byte) 0x01);
        service3.setCell("");
        service3.setSwimlane("");

        //权重为10,swimlane=swimlane-one
        service4 = new SGService("com.meituan.server", "", "4.4.4.4", 9001, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service4.setCell("");
        service4.setSwimlane("swimlane-one");

        //权重为0.01,swimlane=swimlane-one
        service5 = new SGService("com.meituan.server", "", "5.5.5.5", 9001, 10, 2, 0, 0, 0, "", 0.01, 0, "thrift", null, (byte) 0x01);
        service5.setCell("");
        service5.setSwimlane("swimlane-one");

        //权重为10,swimlane=swimlane-one,状态为0(未启动)
        service6 = new SGService("com.meituan.server", "", "6.6.6.6", 9001, 10, 0, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service6.setCell("");
        service6.setSwimlane("swimlane-one");

        //权重为10,swimlane=swimlane-two
        service7 = new SGService("com.meituan.server", "", "7.7.7.7", 9001, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service7.setCell("");
        service7.setSwimlane("swimlane-two");

        //权重为10,cell=cell-one
        service8 = new SGService("com.meituan.server", "", "8.8.8.8", 9001, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service8.setCell("cell-one");
        service8.setSwimlane("");

        //权重为0.01,cell=cell-one
        service9 = new SGService("com.meituan.server", "", "9.9.9.9", 9001, 10, 2, 0, 0, 0, "", 0.01, 0, "thrift", null, (byte) 0x01);
        service9.setCell("cell-one");
        service9.setSwimlane("");

        //权重为10,cell=cell-two
        service10 = new SGService("com.meituan.server", "", "10.10.10.10", 9001, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service10.setCell("cell-two");
        service10.setSwimlane("");

        //权重为10,cell=cell-one,swimlane=swimlane-one
        service11 = new SGService("com.meituan.server", "", "11.11.11.11", 9001, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service11.setCell("cell-one");
        service11.setSwimlane("swimlane-one");

        //权重为0.01,cell=cell-one,swimlane=swimlane-one
        service12 = new SGService("com.meituan.server", "", "12.12.12.12", 9001, 10, 2, 0, 0, 0, "", 0.01, 0, "thrift", null, (byte) 0x01);
        service12.setCell("cell-one");
        service12.setSwimlane("swimlane-one");

        //权重为10,cell=cell-one,swimlane=swimlane-two
        service13 = new SGService("com.meituan.server", "", "13.13.13.13", 9001, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service13.setCell("cell-one");
        service13.setSwimlane("swimlane-two");


        //灰度流量测试
        //权重为10,cell=gray-release-cell-one
        service14 = new SGService("com.meituan.server", "", "14.14.14.14", 9001, 10, 2, 0, 0, 0, "", 10, 0, "thrift", null, (byte) 0x01);
        service14.setCell("gray-release-cell-one");
        service14.setSwimlane("");

    }

    @AfterClass
    public static void stop() {
        InvokeProxy.setIsMock(false);
        Tracer.setSwimlane("");
    }

    @Test
    public void regionTest() {

        //1、测试正常路由
        input = new ArrayList<SGService>();
        input.add(service1);
        input.add(service2);
        expect = new ArrayList<SGService>();
        expect.add(service1);
        expect.add(service2);
        mockMNSResponse(input);
        output = getServerConnList(null, false);
        compare(expect, output, "测试正常路由");

        //2、测试region路由
        input = new ArrayList<SGService>();
        input.add(service1);//权重为10
        input.add(service2);//权重为10
        input.add(service3);//权重为0.01
        expect = new ArrayList<SGService>();
        expect.add(service1);
        expect.add(service2);
        mockMNSResponse(input);
        output = getServerConnList(null, false);
        compare(expect, output, "测试region路由");

    }


    @Test
    public void swimlaneTest() {

        //1、测试swimlane,同时测试region
        Tracer.setSwimlane("swimlane-one");
        input = new ArrayList<SGService>();
        input.add(service1);//权重为10
        input.add(service2);//权重为10
        input.add(service3);//权重为0.01
        input.add(service4);//权重为10,swimlane=swimlane-one
        input.add(service5);//权重为0.01,swimlane=swimlane-one
        expect = new ArrayList<SGService>();
        expect.add(service4);
        mockMNSResponse(input);
        output = getServerConnList(null, false);
        compare(expect, output, "测试swimlane,同时测试region");

        //2、测试无swimlane服务节点回调到主干
        Tracer.setSwimlane("swimlane-one");
        input = new ArrayList<SGService>();
        input.add(service1);//权重为10
        input.add(service2);//权重为10
        input.add(service3);//权重为0.01
        input.add(service7);//权重为10,swimlane=swimlane-two
        expect = new ArrayList<SGService>();
        expect.add(service1);
        expect.add(service2);
        mockMNSResponse(input);
        output = getServerConnList(null, false);
        compare(expect, output, "测试无swimlane服务节点回调到主干");


        //3、测试有未启动的swimlane服务节点时调用报错,不回调到主干
        Tracer.setSwimlane("swimlane-one");
        input = new ArrayList<SGService>();
        input.add(service1);//权重为10
        input.add(service2);//权重为10
        input.add(service3);//权重为0.01
        input.add(service6);//权重为10,swimlane=swimlane-one,状态为0(未启动)
        expect = new ArrayList<SGService>();
        mockMNSResponse(input);
        output = getServerConnList(null, false);
        compare(expect, output, "测试有未启动的swimlane服务节点时调用报错,不回调到主干");

    }

    @Test
    public void cellTest() {

        //1、测试cell,同时测试region
        Tracer.setSwimlane("");
        input = new ArrayList<SGService>();
        input.add(service1);//权重为10
        input.add(service2);//权重为10
        input.add(service3);//权重为0.01
        input.add(service8);//权重为10,cell=cell-one
        input.add(service9);//权重为0.01,cell=cell-one
        input.add(service10);//权重为10,cell=cell-two
        expect = new ArrayList<SGService>();
        expect.add(service8);
        mockMNSResponse(input);
        output = getServerConnList(new MyCellPolicy("cell-one"), true);
        compare(expect, output, "测试cell,同时测试region");

        //2、测试无cell服务节点调用报错
        input = new ArrayList<SGService>();
        input.add(service1);//权重为10
        input.add(service2);//权重为10
        input.add(service3);//权重为0.01
        input.add(service10);//权重为10,cell=cell-two
        expect = new ArrayList<SGService>();
        mockMNSResponse(input);
        output = getServerConnList(new MyCellPolicy("cell-one"), true);
        compare(expect, output, "测试无cell服务节点调用报错");

        //3、测试cell里面有可用的swimlane节点(cell>swimlane>region)
        Tracer.setSwimlane("swimlane-one");
        input = new ArrayList<SGService>();
        input.add(service1);//权重为10
        input.add(service2);//权重为10
        input.add(service3);//权重为0.01
        input.add(service4);//权重为10,swimlane=swimlane-one
        input.add(service5);//权重为0.01,swimlane=swimlane-one
        input.add(service6);//权重为10,swimlane=swimlane-one,状态为0(未启动)
        input.add(service7);//权重为10,swimlane=swimlane-two
        input.add(service8);//权重为10,cell=cell-one
        input.add(service9);//权重为0.01,cell=cell-one
        input.add(service10);//权重为10,cell=cell-two
        input.add(service11);//权重为10,cell=cell-one,swimlane=swimlane-one
        input.add(service12);//权重为0.01,cell=cell-one,swimlane=swimlane-one
        input.add(service13);//权重为10,cell=cell-one,swimlane=swimlane-two
        expect = new ArrayList<SGService>();
        expect.add(service11);
        mockMNSResponse(input);
        output = getServerConnList(new MyCellPolicy("cell-one"), true);
        compare(expect, output, "测试cell里面有可用的swimlane节点(cell>swimlane>region)");

    }

    @Test
    public void grayTest() {

        //测试流出中心
        Tracer.setCell("gray-release-cell-one");
        input = new ArrayList<SGService>();
        input.add(service1);//权重为10
        input.add(service3);//权重为0.01
        input.add(service14);//权重为10,cell=gray-release-cell-one
        expect = new ArrayList<SGService>();
        expect.add(service14);
        mockMNSResponse(input);
        output = getServerConnList(null, true);
        compare(expect, output, "测试灰度流量路由规则,流出中心");
        Tracer.setCell("");

        //测试流入中心
        Tracer.setCell("gray-release-cell-one");
        input = new ArrayList<SGService>();
        input.add(service1);//权重为10
        input.add(service3);//权重为0.01
        expect = new ArrayList<SGService>();
        expect.add(service1);
        mockMNSResponse(input);
        output = getServerConnList(new MyCellPolicy("gray-release-cell-one"), true);
        compare(expect, output, "测试灰度流量路由规则,流入中心");
        Tracer.setCell("");

    }

    public void compare(List<SGService> expect, List<ServerConn> output, String message) {
        List<String> expectList = new ArrayList<String>();
        List<String> outputList = new ArrayList<String>();

        for (SGService sgService : expect) {
            expectList.add(sgService.getIp() + ":" + sgService.getPort());
        }

        for (ServerConn serverConn : output) {
            outputList.add(serverConn.getServer().getIp() + ":" + serverConn.getServer().getPort());
        }

        System.out.println(expectList.toString());
        System.out.println(outputList.toString());
        System.out.println(message + ":" + (expectList.containsAll(outputList) && outputList.containsAll(expectList)));
        assert (expectList.containsAll(outputList) && outputList.containsAll(expectList));
    }

    public void mockMNSResponse(List<SGService> sgServices) {
        ProtocolResponse response = new ProtocolResponse();
        response.setErrcode(0);
        response.setServicelist(sgServices);
        InvokeProxy.setIsMock(true);
        InvokeProxy.setMockValue(response);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<ServerConn> getServerConnList(ICellPolicy cellPolicy, boolean remoteAppIsCell) {
        OctoAgentCluster cluster = null;
        try {
            cluster = new OctoAgentCluster(mtThriftPoolConfig, 1000, false, false, "com.meituan.client",
                    "com.meituan.server", 0, "com.meituan.HelloService", 100, false, false, false, cellPolicy);
            cluster.setRemoteAppIsCell(remoteAppIsCell);
            return cluster.getServerConnList(new RouterMetaData());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cluster != null) {
                cluster.destroy();
            }
        }
        return null;
    }

    class MyCellPolicy implements ICellPolicy {

        private String cell;

        public MyCellPolicy(String cell) {
            this.cell = cell;
        }

        @Override
        public String getCell(RouterMetaData routerMetaData) {
            return cell;
        }
    }

}
