package com.sankuai.octo.queue;

import java.util.concurrent.BlockingQueue;

/**
 * Created by zava on 16/9/29.
 */
public class Indexer implements Runnable {

    private BlockingQueue<String> queue;

    public Indexer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            while(true)
            {
//            int i;
//            for (i = 0; i < 10; i++) {
                Thread.sleep(10);
                String name = queue.take();
                System.out.println("ThreadName : " + Thread.currentThread().getName() + " 消费完成 " + name);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

}