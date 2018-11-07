package com.sankuai.octo.queue;

import java.util.concurrent.BlockingQueue;

/**
 * Created by zava on 16/9/29.
 */
public class Fetcher implements Runnable {

    private BlockingQueue<String> queue = null;

    public Fetcher(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            int i = 0;
            /*while(true)
            {*/
            for (i = 0; i < 5; i++) {
                queue.put("segment-name-" + i);
                System.out.println("ThreadName : " + Thread.currentThread().getName() + "生产完成:" + "segment-name-" + i);

            }
            Thread.sleep(50);
            int j = 0;
            for (j = 0; j < 25; j++) {
                queue.put("segment-name-" + (j + i));
                System.out.println("ThreadName : " + Thread.currentThread().getName() + "生产完成:segment-name-"  + (j + i));
            }
            System.out.println(Thread.currentThread().getName() + "thread--------" + (i + j));
        } catch (InterruptedException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


}