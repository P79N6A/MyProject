package com.meituan.mtrace;

import com.meituan.mtrace.sample.ConcurrentLRUHashMap;

/**
 * User: YangXuehua
 * Date: 14-4-14
 * Time: 上午10:18
 */
public class ConcurrentLRUHashMapTest {
    public static void main(String... avgs) {
        final ConcurrentLRUHashMap map = new ConcurrentLRUHashMap(1000);
        Thread[] threads = new Thread[20];
        for(int j=0;j<threads.length;j++) {
            threads[j] = new Thread(new Runnable() {
                int i=1;
                @Override
                public void run() {
                    long begin = System.currentTimeMillis();
                    for(int count=1;count<=100000;count++) {
                        map.put("key_"+i,i++);
                    }
                    System.out.println(System.currentTimeMillis()-begin+":"+map.size());
                }
            });
            threads[j].start();
        }
    }
}
