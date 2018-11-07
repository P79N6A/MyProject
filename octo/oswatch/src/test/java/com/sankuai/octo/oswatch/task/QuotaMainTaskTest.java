package com.sankuai.octo.oswatch.task;

import com.sankuai.octo.msgp.thrift.service.MSGPService;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import com.sankuai.octo.oswatch.thrift.data.DegradeStrategy;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TSocket;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by chenxi on 6/9/15.
 */
public class QuotaMainTaskTest {
    QuotaMainTask mainTask = mock(QuotaMainTask.class);
    List<DegradeAction> actions;

    @Before
    public void init(){
        actions = new ArrayList<DegradeAction>();
        for (int i=0; i<10;i++) {
            DegradeAction action = new DegradeAction()
                    .setConsumerAppkey("consumerAppKey"+i)
                    .setDegradeRatio(Math.random())
                    .setDegradeStrategy(DegradeStrategy.findByValue(i%3))
                    .setEnv(i%3)
                    .setMethod("all")
                    .setProviderAppkey("providerAppKey")
                    .setTimestamp(System.currentTimeMillis())
                    .setId(""+System.currentTimeMillis()+'/'+i);

            actions.add(action);
        }

        doCallRealMethod().when(mainTask).setContinueLoop(anyBoolean());
        doCallRealMethod().when(mainTask).setCheckInterval(anyInt());
        doCallRealMethod().when(mainTask).run();

        Map<String, DegradeAction> degradeMap = new HashMap<String, DegradeAction>();
        mainTask.setCheckInterval(1000);
    }

    @Test
    public void updateOrDeleteTest(){
        List<Future<DegradeAction>> futures = new ArrayList<Future<DegradeAction>>();
        List<DegradeAction> rv = new ArrayList<DegradeAction>();
        rv.add(actions.get(0));
        rv.add(actions.get(1));
        rv.add(actions.get(2));
        rv.add(actions.get(3));
        rv.add(actions.get(4));

        mainTask.setContinueLoop(true);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mainTask.run();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        try { Thread.sleep(2000); } catch (InterruptedException ie) {}

        rv.clear();
        rv.add(actions.get(3));
        rv.add(actions.get(4));
        rv.add(actions.get(5));
        rv.add(actions.get(6));
        rv.add(actions.get(7));
        rv.add(actions.get(8));

        try { Thread.sleep(2000); } catch (InterruptedException ie) {}
    }

    @Test
    public void threadPoolTakeOrPollTest(){
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CompletionService completionService = new ExecutorCompletionService<DegradeAction>(executorService);

        int loopCount = 10;

        for (int i =0; i< loopCount; i++) {
            completionService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    int x = (int)(Math.random() * 100);
                    System.out.println(x);
                    Thread.sleep(500);
                    return x;
                }
            });
        }

        for (int i=0; i < loopCount; i++) {
            try {
                Future<Integer> future = completionService.poll(1000, TimeUnit.MILLISECONDS);
                int rv = future.get();
                System.out.println(rv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
