package com.sankuai.inf.octo.mns.cache;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sankuai.inf.octo.mns.InvokeProxy;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.sgagent.thrift.model.*;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lhmily on 06/21/2016.
 */
public class ServiceCacheTest {
    private static Logger LOG = LoggerFactory.getLogger(ServiceCacheTest.class);

    private SGAgent.Iface tempClient = new InvokeProxy(SGAgentClient.ClientType.temp).getProxy();
    private static int ERR_NODE_NOTFIND = -101;

    private ServiceCache cache = new ServiceCache();
    private OriginServiceCache originCache = new OriginServiceCache();
    private static int n = new Random().nextInt(5) + 5;//5-10的随机数
    static boolean addFlag = false;
    static boolean deletedFlag = false;
    static boolean modifiedFlag = false;

    private ServiceCache.ServiceListListener listener = new ServiceCache().new ServiceListListener();

    @Test
    public void testGetFromSgagent() throws Exception{
        ProtocolRequest req = new ProtocolRequest();
        req.setRemoteAppkey("ssdfsdfsasdfsafasfsfasf")
                .setProtocol("thrift");
        ProtocolResponse reps = cache.doGetServiceList(tempClient, req);
        Assert.assertNotNull(reps);
        Assert.assertEquals(ERR_NODE_NOTFIND, reps.getErrcode());
        ProtocolRequest serviceNameReq = new ProtocolRequest();
        serviceNameReq.setProtocol("thrift")
                .setServiceName("sfsadfsdfsafkjzljxcvljxfjeoifjewi");
        reps = originCache.doGetServiceList(tempClient, serviceNameReq);
        Assert.assertNotNull(reps);
        Assert.assertEquals(ERR_NODE_NOTFIND, reps.getErrcode());
    }

    @Test
    public void testGetService() {
        ServiceCache cacheC = cache;
        String remoteAppkey = "com.sankuai.inf.sg_sentinel";
        String localAppkey = "com.sankuai.octo.tmy";
        ProtocolRequest req = new ProtocolRequest();
        List<SGService> list = cacheC.get(req);
        Assert.assertNotNull(list);
        System.out.println(list);
        Assert.assertTrue(list.isEmpty());

        ProtocolRequest req1 = new ProtocolRequest();
        req1.setRemoteAppkey(remoteAppkey);
        req1.setProtocol("thrift");
        list = cache.get(req1);
        System.out.println("thrift size is " + list.size());

        ProtocolRequest req2 = new ProtocolRequest();
        req2.setRemoteAppkey(remoteAppkey);
        req2.setLocalAppkey(localAppkey);
        list = cache.get(req2);
        Assert.assertTrue(list.isEmpty());

        ProtocolRequest req3 = new ProtocolRequest();
        req3.setRemoteAppkey(remoteAppkey);
        req3.setLocalAppkey(localAppkey);
        list = cache.get(req3);
        Assert.assertTrue(list.isEmpty());

        ProtocolRequest req4 = new ProtocolRequest();
        req4.setProtocol("thrift");
        list = cache.get(req4);
        Assert.assertTrue(list.isEmpty());
        list.clear();
    }

    @Test
    public void testListener() {
        //该功能不测试listener的触发情况,listener的触发见ServiceListListenerTest
        String remoteAppkey = "com.sankuai.cos.mtconfig";
        String localAppkey = "com.sankuai.octo.tmy";
        String protocol = "thrift";
        ProtocolRequest req = new ProtocolRequest();
        req.setProtocol(protocol);
        req.setRemoteAppkey(remoteAppkey);
        req.setLocalAppkey(localAppkey);
        IServiceListChangeListener listener = new IServiceListChangeListener() {
            @Override
            public void changed(ProtocolRequest req, List<SGService> oldList, List<SGService> newList, List<SGService> addList, List<SGService> deletedList, List<SGService> modifiedList) {
                System.out.println("service list changed.");
            }
        };
        cache.addListener(req, listener);
        Assert.assertEquals(0, cache.removeListener(req, listener));
    }

    @Test
    public void testRetryStrategy() throws InterruptedException {
        InvokeProxy.setIsMock(true);
        InvokeProxy.setMockValue(null);
        ServiceCache.setRunUpdateAll(false);
        ServiceCache.setSaveNullList(false);

        String remoteAppkey = "com.sankuai.cos.mtconfig";
        String localAppkey = "com.sankuai.octo.tmy";
        String protocol = "thrift";
        final ProtocolRequest req = new ProtocolRequest();
        req.setProtocol(protocol);
        req.setRemoteAppkey(remoteAppkey);
        req.setLocalAppkey(localAppkey);

        List<SGService> list = MnsInvoker.getServiceList(req);
        System.out.println(list);
        Assert.assertNull(list);

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                List<SGService> list = MnsInvoker.getServiceList(req);
                Assert.assertNotNull(list);
                Assert.assertFalse(list.isEmpty());
            }
        });
        th.start();

        Thread.sleep(650);
        InvokeProxy.setIsMock(false);
        Thread.sleep(8000);

        InvokeProxy.setIsMock(false);
        InvokeProxy.setMockValue(null);
        ServiceCache.setRunUpdateAll(true);
        ServiceCache.setSaveNullList(true);

    }



    private SGService mockSGServiceWithServiceInfo(List<String> servicenames, List<Boolean> protos){
        SGService service = new SGService();
        service.setAppkey("com.sankuai.octo.tmy")
                .setVersion("10")
                .setIp("1.1.1.1")
                .setEnvir(5)
                .setFweight(5.90)
                .setPort(9000)
                .setStatus(0)
                .setRole(10);
        Map<String, ServiceDetail> serviceInfo = new HashMap<String, ServiceDetail>();
        if(null == servicenames && null == protos){
            serviceInfo.put(null, null);
        } else if (null == servicenames && null != protos) {
            for(int i = 0; i < protos.size(); i++) {
                serviceInfo.put(null, new ServiceDetail(protos.get(i)));
            }
        } else if (null != servicenames && null == protos) {
            for(int i = 0; i < servicenames.size(); i++) {
                serviceInfo.put(servicenames.get(i), null);
            }
        } else {
            for(int i = 0; i < servicenames.size(); i++) {
                serviceInfo.put(servicenames.get(i), new ServiceDetail(protos.get(i)));
            }
        }
        service.setServiceInfo(serviceInfo);
        return service;
    }


    @Test
    public void testIsModify() {
        //返回 False, map的 k, v 都相等且不为空
        SGService s1 = mockSGServiceWithServiceInfo(null, null);
        SGService s2 = mockSGServiceWithServiceInfo(null, Arrays.asList(true));
        SGService s3 = mockSGServiceWithServiceInfo(Arrays.asList("k"), null);
        SGService s4 = mockSGServiceWithServiceInfo(Arrays.asList("k"), Arrays.asList(true));
        SGService s5 = mockSGService(0);

        SGService s1_1 = mockSGServiceWithServiceInfo(null, null);
        SGService s2_2 = mockSGServiceWithServiceInfo(null, Arrays.asList(true));
        SGService s3_3 = mockSGServiceWithServiceInfo(Arrays.asList("k"), null);
        SGService s4_4 = mockSGServiceWithServiceInfo(Arrays.asList("k"), Arrays.asList(true));
        SGService s5_5 = mockSGService(0);

        Assert.assertFalse(listener.isModified(s1, s1_1));
        Assert.assertTrue(listener.isModified(s1, s2));
        Assert.assertTrue(listener.isModified(s1, s3));
        Assert.assertTrue(listener.isModified(s1, s4));
        Assert.assertTrue(listener.isModified(s1, s5));
        Assert.assertFalse(listener.isModified(s2, s2_2));
        Assert.assertTrue(listener.isModified(s2, s3));
        Assert.assertTrue(listener.isModified(s2, s4));
        Assert.assertTrue(listener.isModified(s2, s5));
        Assert.assertFalse(listener.isModified(s3, s3_3));
        Assert.assertTrue(listener.isModified(s3, s4));
        Assert.assertTrue(listener.isModified(s3, s5));
        Assert.assertFalse(listener.isModified(s4, s4_4));
        Assert.assertTrue(listener.isModified(s4, s5));
        Assert.assertFalse(listener.isModified(s5, s5));
        //返回 False, map 均为null


        SGService s6 = mockSGServiceWithServiceInfo(Arrays.asList("k"), Arrays.asList(false));
        SGService s7 = mockSGServiceWithServiceInfo(Arrays.asList("m"), Arrays.asList(true));
        SGService s8 = mockSGServiceWithServiceInfo(new ArrayList<String>(), new ArrayList<Boolean>());
        //不同长度
        Assert.assertTrue(listener.isModified(s4, s8));

        //内容不同
        Assert.assertTrue(listener.isModified(s4, s6));
        Assert.assertTrue(listener.isModified(s4, s7));
    }


    // create a mock SGService
    private SGService mockSGService(int index){
        SGService service = new SGService();
        service.setAppkey("com.sankuai.octo.tmy" + index)
                .setVersion("10")
                .setIp("1.1.1." + index)
                .setEnvir(5)
                .setFweight(5.90)
                .setPort(9000 + index)
                .setStatus(0)
                .setRole(10);
        return service;
    }

    @Test
   public void testCallDiffLoadWithLargeList() throws Exception {
        // 模拟list
        List<String> servicenames = new ArrayList<String>();
        for(int i = 0 ; i < 100 ;i++) {
            servicenames.add("s" + i);
        }

        List<Boolean> protos = new ArrayList<Boolean>();
        for(int i = 0 ; i < 100 ;i++) {
            protos.add(true);
        }
        List<SGService> list = new ArrayList<SGService>();
        for(int i = 0; i < 10; i++){
            //list.add(mockSGServiceWithServiceInfo(servicenames, protos));
            list.add(mockSGService(i));
        }
        // 定时更新list
        Runnable task = new Task(list);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(
                task,
                0,
                5*1000,
                TimeUnit.MILLISECONDS);

        ProtocolRequest request = new ProtocolRequest();
        request.setLocalAppkey("com.sankuai.octo.tmy")
                .setRemoteAppkey("com.sankuai.octo.tmy")
                .setProtocol("thrift");
        MnsInvoker.addServiceListener(request, new IServiceListChangeListener() {
            @Override
            public void changed(ProtocolRequest req, List<SGService> oldList, List<SGService> newList, List<SGService> addList, List<SGService> deletedList, List<SGService> modifiedList) {
                LOG.info("changed.");
            }
        });


        Thread.sleep(10 * 1000);
        InvokeProxy.setIsMock(false);
        executor.shutdownNow();
        list.clear();
    }

    @Test
    public void testCallDiffLoadWithManyRequest() throws Exception {
        // 模拟list
        List<SGService> list = new ArrayList<SGService>();
        List<String> servicenames = new ArrayList<String>();
        for(int i = 0 ; i < 100 ;i++) {
            servicenames.add("s" + i);
        }

        List<Boolean> protos = new ArrayList<Boolean>();
        for(int i = 0 ; i < 100 ;i++) {
            protos.add(true);
        }
        for(int i = 0; i < 9; i++){
            //list.add(mockSGServiceWithServiceInfo(servicenames, protos));
            list.add(mockSGService(i));
        }
        // 定时更新list
        Runnable task = new Task(list);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(
                task,
                0,
                5*1000,
                TimeUnit.MILLISECONDS);


        for(int i = 0; i < 10; i++){
            ProtocolRequest request = new ProtocolRequest();
            request.setLocalAppkey("com.sankuai.octo.tmy" + i)
                    .setRemoteAppkey("com.sankuai.octo.tmy"+ i)
                    .setProtocol("thrift");
            MnsInvoker.addServiceListener(request, new IServiceListChangeListener() {
                @Override
                public void changed(ProtocolRequest req, List<SGService> oldList, List<SGService> newList, List<SGService> addList, List<SGService> deletedList, List<SGService> modifiedList) {
                    LOG.info("changed.");
                }
            });
        }


        Thread.sleep(10 * 1000);
        InvokeProxy.setIsMock(false);
        executor.shutdownNow();
        list.clear();
    }

    class Task implements Runnable{
        List<SGService> list;

        public Task(List<SGService> list) {
            this.list = list;
        }

        @Override
        public void run() {
            InvokeProxy.setIsMock(true);
            ProtocolResponse result = new ProtocolResponse();
            result.setErrcode(0)
                    .setServicelist(list);
            InvokeProxy.setMockValue(result);
            LOG.info("a new set.");
            list.clear();
        }
    }


    @Test
    public void testCallDiffCheckAddList()throws Exception{
        // 模拟list
        n = new Random().nextInt(5) + 5;//5-10的随机数
        List<SGService> list = new ArrayList<SGService>();
        list.clear();
        InvokeProxy.setIsMock(true);
        for(int i = 0; i < n; i++) {
            list.add(mockSGService(i));
        }

        // 定时更新list
        AddTask task = new AddTask(list);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(
                task,
                0,
                5*1000,
                TimeUnit.MILLISECONDS);

        ProtocolRequest request = new ProtocolRequest();
        request.setLocalAppkey("com.sankuai.octo.tmy")
                .setRemoteAppkey("com.sankuai.octo.tmy")
                .setProtocol("thrift");

        MnsInvoker.addServiceListener(request, new IServiceListChangeListener() {
            @Override
            public void changed(ProtocolRequest req, List<SGService> oldList, List<SGService> newList, List<SGService> addList, List<SGService> deletedList, List<SGService> modifiedList) {
                if (!addList.isEmpty()) {
                    print("addList:", addList);
                }else {
                    LOG.info("addList is empty");
                }
                if (!deletedList.isEmpty()) {
                    print("deletedList:", deletedList);
                }else {
                    LOG.info("deletedList is empty");
                }
                if (!modifiedList.isEmpty()) {
                    print("modifiedList:", modifiedList);

                }else {
                    LOG.info("modifiedList is empty");
                }
                modifiedFlag = true;
                deletedFlag = true;
                addFlag = true;
                LOG.info("changed.");
            }
            private void print(String msg, List<SGService> list) {
                System.out.println(msg);
                for (SGService service : list) {
                    System.out.println(service);
                }
            }
        });
        Thread.sleep(10 * 1000);
        Assert.assertTrue(addFlag);
        Assert.assertTrue(deletedFlag);
        Assert.assertTrue(modifiedFlag);
        InvokeProxy.setIsMock(false);

        executor.shutdownNow();
        list.clear();
    }

    class AddTask implements Runnable{

        List<SGService> tasklist;
        List<SGService> list;

        public AddTask(List<SGService> list) {
            this.list = list;
        }

        @Override
        public void run() {
            ProtocolResponse result = new ProtocolResponse();
            List<SGService> tasklist = new ArrayList<SGService>();
            for (SGService item : list){
                tasklist.add(item.deepCopy());
            }
            SGService s1 = tasklist.get(1);
            s1.setFweight(n++);
            tasklist.set(1, s1);//modify list
            tasklist.add(mockSGService(n));//add list
            n++;
            tasklist.add(mockSGService(n));
            n++;
            tasklist.remove(0);//delete list
            n--;
            result.setErrcode(0)
                    .setServicelist(tasklist);
            InvokeProxy.setMockValue(result);
            LOG.info("a new set.");
            //tasklist.clear();
            //list.clear();
        }
    }


/*
    @Test
    public void testCallDiffCheckDeleteList()throws Exception{
        n = new Random().nextInt(5) + 5;//50-100的随机数
        // 模拟list
        List<SGService> list = new ArrayList<SGService>();
        list.clear();
        InvokeProxy.setIsMock(true);
        for(int i = 0; i < n; i++) {
            list.add(mockSGService(i));
        }
        deletedFlag = false;
        // 定时更新list
        DeleteTask task = new DeleteTask(list);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(
                task,
                0,
                5*1000,
                TimeUnit.MILLISECONDS);

        ProtocolRequest request = new ProtocolRequest();
        request.setLocalAppkey("com.sankuai.octo.tmy")
                .setRemoteAppkey("com.sankuai.octo.tmy")
                .setProtocol("thrift");

        MnsInvoker.addServiceListener(request, new IServiceListChangeListener() {
            @Override
            public void changed(ProtocolRequest req, List<SGService> oldList, List<SGService> newList, List<SGService> addList, List<SGService> deletedList, List<SGService> modifiedList) {
                if (!deletedList.isEmpty()) {
                    print("deletedList:", deletedList);
                }else {
                    LOG.info("deletedList is empty");
                }
                deletedFlag = true;
                LOG.info("changed.");
            }
            private void print(String msg, List<SGService> list) {
                System.out.println(msg);
                for (SGService service : list) {
                    System.out.println(service);
                }
            }
        });

        Thread.sleep(10 * 1000);
        InvokeProxy.setIsMock(false);
        executor.shutdownNow();
        list.clear();
        task.tasklist.clear();
        Assert.assertTrue(deletedFlag);
    }
    class DeleteTask implements Runnable{
        List<SGService> list;
        List<SGService> tasklist;
        public DeleteTask(List<SGService> list) {
            this.list = list;
        }

        @Override
        public void run() {
            deletedFlag = false;
            ProtocolResponse result = new ProtocolResponse();
            tasklist = new ArrayList<SGService>();
            tasklist.clear();
            tasklist.addAll(list);

            tasklist.remove(n-1);
            n--;
            tasklist.remove(n-1);
            n--;
            result.setErrcode(0)
                    .setServicelist(tasklist);
            InvokeProxy.setMockValue(result);
            LOG.info("a new set.");
            list.clear();
        }
    }


    @Test
    public void testCallDiffCheckModifiedList()throws Exception{
        n = new Random().nextInt(10);
        // 模拟list
        List<SGService> list = new ArrayList<SGService>();
        InvokeProxy.setIsMock(true);
        for(int i = 0; i < n; i++) {
            list.add(mockSGService(i));
        }

        // 定时更新list
        Runnable task = new ModifiedTask(list);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(
                task,
                0,
                5*1000,
                TimeUnit.MILLISECONDS);

        ProtocolRequest request = new ProtocolRequest();
        request.setLocalAppkey("com.sankuai.octo.tmy")
                .setRemoteAppkey("com.sankuai.octo.tmy")
                .setProtocol("thrift");
        modifiedFlag = false;
        MnsInvoker.addServiceListener(request, new IServiceListChangeListener() {
            @Override
            public void changed(ProtocolRequest req, List<SGService> oldList, List<SGService> newList, List<SGService> addList, List<SGService> deletedList, List<SGService> modifiedList) {
                if (!modifiedList.isEmpty()) {
                    print("modifiedList:", modifiedList);

                }else {
                    LOG.info("modifiedList is empty");
                }
                modifiedFlag = true;
                LOG.info("changed.");

            }
            private void print(String msg, List<SGService> list) {
                System.out.println(msg);
                for (SGService service : list) {
                    System.out.println(service);
                }
            }
        });

        Thread.sleep(10 * 1000);
        Assert.assertTrue(modifiedFlag);
        InvokeProxy.setIsMock(false);
        executor.shutdownNow();
        list.clear();
    }
    class ModifiedTask implements Runnable{
        List<SGService> list;

        public ModifiedTask(List<SGService> list) {
            this.list = list;
        }

        @Override
        public void run() {
            modifiedFlag = false;
            ProtocolResponse result = new ProtocolResponse();
            List<SGService> tasklist = new ArrayList<SGService>();
            for (SGService item : list){
                tasklist.add(item.deepCopy());
            }
            SGService s1 = tasklist.get(0);
            s1.setFweight(n++);
            SGService s2 = tasklist.get(1);
            s2.setFweight(n++);
            tasklist.set(0, s1);
            tasklist.set(1, s2);
            result.setErrcode(0)
                    .setServicelist(tasklist);
            InvokeProxy.setMockValue(result);
            LOG.info("a new set.");
            list.clear();
        }
    }
*/
}