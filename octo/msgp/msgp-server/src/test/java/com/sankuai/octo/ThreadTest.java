package com.sankuai.octo;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ThreadTest {

    @Test
    public void testInit() {
        for (int i = 0; i < 10; i++) {
            ThreadInfo info = new ThreadInfo();
            info.set(true);
        }
    }

    @Test
    public void testThread() throws InterruptedException {
        int max = 5;
        final CountDownLatch countDownLatch = new CountDownLatch(max);
        for (int i = 0; i < max; i++) {
            new Thread() {
                @Override
                public void run() {
                    for (int j = 0; j < 10; j++) {
                        ThreadInfo info = new ThreadInfo();
                        info.set(true);
                    }
                    countDownLatch.countDown();
                }
            }.start();
        }
        countDownLatch.await();
    }

    @Test
    public void printAbc() throws InterruptedException {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        Thread aThread = new Thread(new AbcThread("a", a, b));
        Thread bThread = new Thread(new AbcThread("b", b, c));
        Thread cThread = new Thread(new AbcThread("c", c, a));

        aThread.start();
        bThread.start();
        cThread.start();
        bThread.join();
        aThread.join();
        cThread.join();
    }
}

class AbcThread implements Runnable {
    private String name;
    private Object self;
    private Object next;

    public AbcThread(String name, Object self, Object next) {
        this.name = name;
        this.next = next;
        this.self = self;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            synchronized (self) {
                synchronized (next) {
                    System.out.print(name);
                    next.notify();
                }
                if (i == 9) {
                    return;     // 当i == 9 即最后一次循环, 将直接退出 不再进行等待
                }
                try {
                    self.wait();
                }catch (Exception e){

                }
            }
        }

    }
}

class ThreadInfo {
    private static ThreadLocal<Boolean> test = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            System.out.println(Thread.currentThread().getId() + " init false");
            return false;
        }
    };

    public void set(boolean value) {
        if (test.get() != value) {
            System.out.println(Thread.currentThread().getId() + " change " + test.get() + " -> " + value);
        }
        test.set(value);
    }
}