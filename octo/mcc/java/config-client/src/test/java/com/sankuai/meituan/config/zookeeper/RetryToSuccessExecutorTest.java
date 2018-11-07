package com.sankuai.meituan.config.zookeeper;

public class RetryToSuccessExecutorTest {
    public void testRetryToSuccess() {
        new RetryToSuccessExecutor(new Runnable() {
            volatile int count = 0;

            @Override
            public void run() {
                System.out.println(count);
                System.gc();
                if (++ count < 5) {
                    throw new RuntimeException("test");
                }
            }

            @Override
            public String toString() {
                return "ZK连接失败,可能是config server down机或zk出异常";
            }
        }, 1);
        System.out.println("begin");
    }

    public static void main(String args[]) {
        new RetryToSuccessExecutorTest().testRetryToSuccess();
    }
}