package com.sankuai.meituan.config;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-5
 */
public class MtConfigClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(MtConfigClientTest.class);
    public static final String SERVER_HOST = "http://localhost:8080";
    public static final String NODE_NAME = "test.test1";

    @Test
    public void test() throws MtConfigException {
        String nodeName = NODE_NAME;

        MtConfigClient mtConfigClient = new MtConfigClient();
        mtConfigClient.setNodeName(nodeName);
        mtConfigClient.setId(String.valueOf(System.currentTimeMillis()));
        mtConfigClient.setScanBasePackage("com.sankuai.meituan.config");
        mtConfigClient.init();
        mtConfigClient.addListener("switch", new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOG.info("{} changed oldValue: {} , newValue: {}",
                        new Object[]{key, oldValue, newValue});
            }
        });


//        while (true) {
        String key = "switch";
        LOG.info("{}:{}", key, mtConfigClient.getValue(key));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        }
    }


    @Test
    public void setValue() throws MtConfigException {
        MtConfigClient client = new MtConfigClient();
        client.setNodeName(NODE_NAME);
        client.init();
        client.setValue("testNewLine", "aaa\nbbb\\n\"\\\\ccc");
    }

    @Test
    public void getValue() throws MtConfigException {
        MtConfigClient client = new MtConfigClient();
        client.setNodeName(NODE_NAME);
        client.init();
        System.out.println(client.getValue("testNewLine"));
    }

    @Test
    public void testProperty() throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(testEscape("test1=aaa\nbbb\ntest2=test\\naaa\ntest3=adsf")));
        System.out.println(properties.get("test2"));
    }

    public String testEscape(String value) {
        value = StringUtils.replace(value, "\\n", "\n");
        List<String> splitList = Lists.newArrayList();
        for (String split : StringUtils.split(value, '=')) {
            int replaceCount = StringUtils.countMatches(split, "\n") - 1;
            if (replaceCount > 0) {
                split = StringUtils.replace(split, "\n", "\\n", replaceCount);
            }
            splitList.add(split);
        }
        return Joiner.on('=').join(splitList);
    }

    @Test
    public void replaceMap() {
        Map<String, String> map = Maps.newHashMap(ImmutableMap.<String, String>builder().put("test1", "aaa\nbbb").put("test2", "aaa").put("test3", "aaa\\nbbb").build());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            map.put(entry.getKey(), StringEscapeUtils.escapeJava(entry.getValue()));
        }
        System.out.println(map);
    }

    @Test
    public void commonTest() {
        Boolean ret = StringUtils.isBlank(null);
        System.out.println(ret);
    }

    @Test
    public void removeListenerV1Test () throws Exception{
        MtConfigClient mtConfigClient = new MtConfigClient();
        mtConfigClient.setNodeName(NODE_NAME);
        mtConfigClient.setId(String.valueOf(System.currentTimeMillis()));
        mtConfigClient.setScanBasePackage("com.sankuai.meituan.config");
        mtConfigClient.init();

        String key = "testKey";
        IConfigChangeListener listener = new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOG.info("{} changed oldValue: {} , newValue: {}",
                        new Object[]{key, oldValue, newValue});
            }
        };
        mtConfigClient.addListener(key, listener);

        LOG.info("{}:{}", key, mtConfigClient.getValue(key));
        Thread.sleep(10*1000);
        LOG.info("start remove listener");
        mtConfigClient.removeListener(key ,listener);
        Thread.sleep(10*1000);
    }

    @Test
    public void removeListenerV2Test () throws Exception{
        MtConfigClient mtConfigClient = new MtConfigClient();
        mtConfigClient.setAppkey("com.sankuai.octo.tmy");
        mtConfigClient.setModel("v2");
        mtConfigClient.setId(String.valueOf(System.currentTimeMillis()));
        mtConfigClient.setScanBasePackage("com.sankuai.meituan.config");
        mtConfigClient.init();

        String key = "key13";
        IConfigChangeListener listener = new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOG.info("{} changed oldValue: {} , newValue: {}",
                        new Object[]{key, oldValue, newValue});
            }
        };
        mtConfigClient.addListener(key, listener);

        LOG.info("{}:{}", key, mtConfigClient.getValue(key));
        Thread.sleep(10*1000);
        LOG.info("start remove listener");
        mtConfigClient.removeListener(key, listener);
        Thread.sleep(10*1000);
    }

    @Test
    public void removeListenerMultiThreadV2Test () throws Exception{
        String key = "key13";
        MtConfigClient mtConfigClient = new MtConfigClient();
        mtConfigClient.setAppkey("com.sankuai.octo.tmy");
        mtConfigClient.setModel("v2");
        mtConfigClient.setId(String.valueOf(System.currentTimeMillis()));
        mtConfigClient.setScanBasePackage("com.sankuai.meituan.config");
        mtConfigClient.init();

        IConfigChangeListener listener = new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOG.info("{} changed oldValue: {} , newValue: {}",
                        new Object[]{key, oldValue, newValue});
            }
        };

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 20; i++) {
            fixedThreadPool.execute(new AddListenerTask(mtConfigClient, key, listener));
            Thread.sleep(100);
            fixedThreadPool.execute(new RemoveListenerTask(mtConfigClient, key, listener));
            Thread.sleep(100);
        }

        Thread.sleep(100 * 1000);
    }

    class RemoveListenerTask implements  Runnable{
        private MtConfigClient client;
        private IConfigChangeListener listener;
        private String key;
        public  RemoveListenerTask(MtConfigClient mtConfigClient, String key, IConfigChangeListener listener) {
            this.client = mtConfigClient;
            this.key = key;
            this.listener = listener;
        }

        @Override
        public void run() {
            try{
                client.removeListener(key, listener);
                LOG.info("{}:{}", key, client.getValue(key));


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class AddListenerTask implements  Runnable{
        private MtConfigClient client;
        private String key;
        private IConfigChangeListener listener;
        public  AddListenerTask(MtConfigClient mtConfigClient, String key, IConfigChangeListener listener) {
            this.client = mtConfigClient;
            this.key = key;
            this.listener = listener;
        }

        @Override
        public void run() {
            try{
                client.addListener(key, listener);
                LOG.info("{}:{}", key, client.getValue(key));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }



}
