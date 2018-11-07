package com.sankuai.inf.octo.mns;

import com.sankuai.sgagent.thrift.model.SGAgent;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.junit.Test;

import java.util.List;

public class AsyncTests {
    @Test
    public void testAsync() throws Exception {
        TProtocolFactory factory = new TBinaryProtocol.Factory();
        TAsyncClientManager manager = new TAsyncClientManager();
        SGAgent.AsyncClient.Factory clientFactory = new SGAgent.AsyncClient.Factory(manager, factory);

        TNonblockingTransport transport = new TNonblockingSocket("10.4.241.165", 5266, Consts.connectTimeout);
        SGAgent.AsyncClient client = clientFactory.getAsyncClient(
                new TNonblockingSocket("10.4.241.165", 5266, Consts.connectTimeout));
        SGAgent.AsyncClient client1 = clientFactory.getAsyncClient(
                new TNonblockingSocket("10.4.241.165", 5266, Consts.connectTimeout));
        SGAgent.AsyncClient client2 = clientFactory.getAsyncClient(
                new TNonblockingSocket("10.4.241.165", 5266, Consts.connectTimeout));

        AsyncMethodCallback<SGAgent.AsyncClient.getServiceList_call> callback1 = new ServiceListCallback();
        client.getServiceList("local", "com.sankuai.inf.logCollector", callback1);
//        Thread.sleep(1000);
        AsyncMethodCallback<SGAgent.AsyncClient.getServiceList_call> callback2 = new ServiceListCallback();
        client1.getServiceList("local", "com.sankuai.inf.sgnotify", callback2);
//        Thread.sleep(1000);
        AsyncMethodCallback<SGAgent.AsyncClient.getServiceList_call> callback3 = new ServiceListCallback();
        client2.getServiceList("local", "com.sankuai.inf.data.statistic", callback3);
        Thread.sleep(10000);
    }

    class ServiceListCallback implements AsyncMethodCallback<SGAgent.AsyncClient.getServiceList_call> {
        @Override
        public void onComplete(
                SGAgent.AsyncClient.getServiceList_call getServiceList_call) {
            try {
                List<SGService> list = getServiceList_call.getResult();
                System.out.println(list);
                System.out.println("-------------------");
            } catch (TException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    }
}
