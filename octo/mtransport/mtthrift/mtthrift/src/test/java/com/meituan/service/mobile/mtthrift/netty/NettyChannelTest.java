package com.meituan.service.mobile.mtthrift.netty;

import com.meituan.service.mobile.mtthrift.netty.channel.NettyChannel;
import com.meituan.service.mobile.mtthrift.server.netty.DefaultServerDecoder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/8
 * Time: 14:08
 */
public class NettyChannelTest extends DefaultServerDecoder{
    private static final Logger log = LoggerFactory.getLogger(NettyChannelTest.class);
    private static NettyChannel customizedChannel;
    private static MockServerBootstrap mockServer;

    public NettyChannelTest() {
        super(null);
    }

    @BeforeClass
    public static void init() throws InterruptedException {
        mockServer = new MockServerBootstrap();
        InetSocketAddress remoteAddress = new InetSocketAddress(mockServer.host, mockServer.port);
        customizedChannel = new NettyChannel(null, remoteAddress, 1000);
    }

    @Test
    public void test0() throws Exception {
//        String username = "meituan";
//        String methodName = "sayHello";
//        String appkey = "testAppkey";
//        Class<?> serviceInterface = HelloService.class;
//
//        RpcRequest request = new RpcRequest(serviceInterface, methodName, new Object[]{username});
//        request.setSeq(1L);
//        request.setParameterTypes(new Class[]{username.getClass()});
//
//        MtThrfitInvokeInfo invokeInfo = new MtThrfitInvokeInfo("serverAppkey",
//                serviceInterface.getSimpleName() + "." + methodName,
//                mockServer.host, 0, mockServer.host, mockServer.port);
//        ThriftClientProxy clientProxy = new ThriftClientProxy();
//        clientProxy.setServiceInterface(HelloService.class);
//        clientProxy.setAppKey(appkey);
//        clientProxy.setLocalServerPort(mockServer.port);
//        clientProxy.afterPropertiesSet();
//        try {
//            ByteBuf byteBuf = customizedChannel.encodeRequest(request, invokeInfo, clientProxy);
//            List<Object> list = new ArrayList<Object>(1);
//            this.decode(null, byteBuf, list);
//            Assert.assertEquals(1, list.size());
//
//            RPCContext context = ((RPCContext) list.get(0));
//            Assert.assertNotNull(context);
//            Assert.assertEquals(RequestType.unifiedProto, context.getRequestType());
//            Assert.assertTrue(context.isUnifiedProto());
//
//            Header header = context.getHeader();
//            Assert.assertNotNull(header);
//
//            RequestInfo requestInfo = header.getRequestInfo();
//            Assert.assertNotNull(requestInfo);
//            Assert.assertEquals(request.getSeq(), requestInfo.getSequenceId());
//            Assert.assertEquals(serviceInterface.getName(), requestInfo.getServiceName());
//
//            TraceInfo traceInfo = header.getTraceInfo();
//            Assert.assertNotNull(traceInfo);
////            Assert.assertNotNull(traceInfo.getTraceId());
//            Assert.assertEquals(appkey, traceInfo.getClientAppkey());
//
//            Assert.assertNotNull(context.getThriftDataBytes());
//            Assert.assertNotEquals(0, context.getRequestSize());
//        } catch (Exception e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
    }

    @Test
    public void test1() {
//        if (JdkUtil.isJdk6()) {
//            return;
//        }
//        try {
//            String content = "meituan";
//            String appkey = "testAppkey";
//            RpcRequest request = new RpcRequest(AnnotatedService.class, "echo", new Object[]{content});
//            request.setSeq(1L);
//            request.setParameterTypes(new Class[]{content.getClass()});
//
//            Class<?> serviceInterface = AnnotatedService.class;
//            ThriftCodecManager codecManager = new ThriftCodecManager();
//            MTThriftInvocationHandler.ThriftClientMetadata clientMetadata =
//                    new MTThriftInvocationHandler.ThriftClientMetadata(serviceInterface, serviceInterface.getName(), codecManager);
//
//            Method method = serviceInterface.getMethod("echo", String.class);
//            ThriftMethodHandler methodHandler = clientMetadata.getMethodHandlers().get(method);
//            request.setThriftMethodHandler(methodHandler);
//
//            MtThrfitInvokeInfo invokeInfo = new MtThrfitInvokeInfo(appkey,
//                    "AnnotatedService.echo",
//                    mockServer.host, 0, mockServer.host, mockServer.port);
//            ThriftClientProxy clientProxy = new ThriftClientProxy();
//            clientProxy.setServiceInterface(HelloService.class);
//            clientProxy.setAppKey(appkey);
//            clientProxy.setLocalServerPort(mockServer.port);
//            clientProxy.afterPropertiesSet();
//            ByteBuf byteBuf = customizedChannel.encodeAnnotatedRequest(request, invokeInfo, clientProxy);
//            List<Object> list = new ArrayList<Object>(1);
//            this.decode(null, byteBuf, list);
//
//            Assert.assertEquals(1, list.size());
//
//            RPCContext context = ((RPCContext) list.get(0));
//            Assert.assertNotNull(context);
//            Assert.assertEquals(RequestType.unifiedProto, context.getRequestType());
//            Assert.assertTrue(context.isUnifiedProto());
//
//            Header header = context.getHeader();
//            Assert.assertNotNull(header);
//
//            RequestInfo requestInfo = header.getRequestInfo();
//            Assert.assertNotNull(requestInfo);
//            Assert.assertEquals(request.getSeq(), requestInfo.getSequenceId());
//            Assert.assertEquals(serviceInterface.getName(), requestInfo.getServiceName());
//
//            TraceInfo traceInfo = header.getTraceInfo();
//            Assert.assertNotNull(traceInfo);
////            Assert.assertNotNull(traceInfo.getTraceId());
//            Assert.assertEquals(appkey, traceInfo.getClientAppkey());
//
//            Assert.assertNotNull(context.getThriftDataBytes());
//            Assert.assertNotEquals(0, context.getRequestSize());
//        } catch (Exception e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
    }
}
