//package com.meituan.service.mobile.mtthrift;
//
//import com.meituan.service.mobile.mtthrift.netty.NettyClient;
//import com.meituan.service.mobile.mtthrift.netty.metadata.RpcRequest;
//import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicLong;
//
///**
// * Author: caojiguang@gmail.com
// * Date: 16/9/20
// * Description:
// */
//public class TestNettyClient {
//
//    private static final AtomicLong SEQID = new AtomicLong();
//    protected static long getSequenceId() {
//        return SEQID.addAndGet(1L);
//    }
//
//
//    public static void main(String[] args) throws Exception {
//        final String host = ProcessInfoUtil.getLocalIpV4();
//        final int port = 10001;
//        final int count = 10;
//
//       // while (true) {
//            NettyClient client = new NettyClient(host, port);
//         //   Thread.sleep(50);
//        //}
//
//        Long start = System.currentTimeMillis();
//        List<Long> list = new ArrayList<Long>();
//        for (int i = 0; i < count; i++) {
//            RpcRequest request = new RpcRequest();
//            request.setServiceInterface(Class.forName("com.meituan.service.mobile.mtthrift.HelloService"));
//            request.setMethodName("sayBye");
//            request.setParameters(new Object[]{"new protocol"});
//            request.setParameterTypes(new Class[]{String.class});
//            request.setSeq(getSequenceId());
//            client.sent(request);
//            list.add(request.getSeq());
//        }
//
//        System.out.println(System.currentTimeMillis() - start);
//
//
//        for (int i = 0; i < count; i++) {
//            System.out.println(client.getResponse(list.get(i)).getReturnVal());
//        }
//
////        list = new ArrayList<Long>();
////        for (int i = 0; i < count; i++) {
////            DefaultRequest request = new DefaultRequest();
////            request.setServiceInterface(Class.forName("com.meituan.service.mobile.mtthrift.EchoService"));
////            request.setMethodName("echo");
////            request.setParameters(new Object[]{"test Multi-Service"});
////            request.setParameterTypes(new Class[]{String.class});
////            request.setSeq(getSequenceId());
////            client.sent(request);
////            list.add(request.getSeq());
////        }
////
////        System.out.println(System.currentTimeMillis() - start);
////
////
////        for (int i = 0; i < count; i++) {
////            System.out.println(client.getResponse(list.get(i)).getReturnVal());
////        }
//
//    }
//
//}
