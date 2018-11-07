package com.sankuai.meituan.config;

import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.v1.MtConfigClientV1;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-20
 */
public class MtConfigV1ClientConcurrentTest {
    private static final Logger LOG = LoggerFactory.getLogger(MtConfigV1ClientConcurrentTest.class);


    private final String nodeName = "test";

    private final long sleepTime = 3000;
    private final int providerNumber = 10;
    private final int consumerNumber = 10;

    @Test
    public void concurrentTest() throws MtConfigException {
//        MtConfigClientV1 client = genClient();
//        for (int i = 0; i < 5; i++) {
//            String key = "key" + i;
//            client.addListener(key, new ConfigChangeListener(key));
//        }
//        for (int i = 0; i < providerNumber; i++) {
//            new Thread(new Provider()).start();
//        }
//        for (int i = 0; i < consumerNumber; i++) {
//            new Thread(new Consumer()).start();
//        }
//        try {
//            Thread.sleep(sleepTime);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public String genKey() {
        Integer k = (int) (Math.random() * 50);
        return "key" + k;
    }

    public MtConfigClientV1 genClient() throws MtConfigException {
        MtConfigClientV1 client = new MtConfigClientV1();
        client.setNodeName(nodeName + ".test3");
        client.init();
        return client;
    }


    public class Provider implements Runnable {
        private MtConfigClientV1 client = null;

        public Provider() throws MtConfigException {
            client = new MtConfigClientV1();
            client.setNodeName(nodeName);
            client.init();
        }

        @Override
        public void run() {
            long threadId = Thread.currentThread().getId();
            try {
                String key = genKey();
                String value = String.valueOf(System.currentTimeMillis());
                client.setValue(key, value);
                LOG.info("[{}] setValue: {} {}", new Object[]{threadId, key, value});
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                LOG.error("[{}] exception:", threadId, e);
            }
        }

    }

    public class Consumer implements Runnable {
        private MtConfigClientV1 client = null;

        public Consumer() throws MtConfigException {
            client = new MtConfigClientV1();
            client.setNodeName(nodeName);
            client.init();
        }

        @Override
        public void run() {

            long threadId = Thread.currentThread().getId();
            try {
                String key = genKey();
                String value = client.getValue(key);
                LOG.info("[{}] getValue: {} {}", new Object[]{threadId, key, value});
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                LOG.error("[{}] exception:", threadId, e);
            }

        }
    }

    public class ConfigChangeListener implements IConfigChangeListener {
        private String key = null;

        public ConfigChangeListener(String key) {
            this.key = key;
        }

        @Override
        public void changed(String key, String oldValue, String newValue) {
            long threadId = Thread.currentThread().getId();
            LOG.info("[{}] listener: {} {} {}", new Object[]{threadId, key, oldValue, newValue});
        }
    }
}
