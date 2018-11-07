package com.sankuai.octo.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zava on 16/9/29.
 */
public class TestConsumer {
    private static BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10);

    public static void main(String[] args)
    {
        ExecutorService service = Executors.newCachedThreadPool();

        Fetcher producer = new Fetcher(queue);
        Indexer consumer = new Indexer(queue);
        Indexer consumerSecond = new Indexer(queue);
        service.submit(producer);
        service.submit(consumer);
        service.submit(consumerSecond);

        try{
            Thread.sleep(50000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}