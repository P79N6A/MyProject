package com.sankuai.meituan.config;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sankuai.meituan.config.annotation.MtConfig;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.meituan.config.v1.MtConfigClientV1;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.lang.Thread.sleep;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-5
 */
public class MtConfigV1ClientTest {
    @MtConfig(nodeName = "test.test2", key = "testKey")
    public static String testValue;

    @MtConfig(nodeName = "test.test2", key = "testKey1")
    public static String testValue1;

    private static final Logger LOG = LoggerFactory.getLogger(MtConfigV1ClientTest.class);
    public static final String NODE_NAME = "test.test2";
    public static final String OTHER_NODE_NAME = NODE_NAME + ".g1";
    private static MtConfigClientV1 client;

    @BeforeClass
    public static void init() throws MtConfigException {
        MtConfigClientV1 mtConfigClientV1 = new MtConfigClientV1();
        mtConfigClientV1.setNodeName(NODE_NAME);
        mtConfigClientV1.setScanBasePackage("com.sankuai.meituan.config");
        mtConfigClientV1.setId(String.valueOf(System.currentTimeMillis()));
        mtConfigClientV1.init();
        client = mtConfigClientV1;
    }

    @Test
    public void getTestValue() throws MtConfigException ,InterruptedException{
        sleep(2000);
        System.out.println("testValue=" + testValue);
        System.out.println("testValue1=" + testValue1);

    }

    @Test
    public void testOneIdMultiClient() {
        MtConfigClientV1 mtConfigClientV1 = new MtConfigClientV1();
        mtConfigClientV1.setNodeName(NODE_NAME);
        mtConfigClientV1.setScanBasePackage("com.sankuai.meituan.config");
        try {
            mtConfigClientV1.init();
        } catch (MtConfigException e) {
            Assert.assertTrue(e.getMessage().contains("不允许重复,请检查代码"));
        }
    }

    @Test
    public void testMultiIdMultiClient() throws MtConfigException {
        MtConfigClientV1 mtConfigClientV1 = new MtConfigClientV1();
        mtConfigClientV1.setNodeName(OTHER_NODE_NAME);
        mtConfigClientV1.setScanBasePackage("com.sankuai.meituan.config");
        mtConfigClientV1.init();
        LOG.info(TestConfigBean.ggg);
        LOG.info(TestConfigBean.mcckey2);
    }

    @Test
    public void testClientId() throws MtConfigException {
        MtConfigClient mtConfigClientV1 = new MtConfigClient();
        mtConfigClientV1.setNodeName("mcctest.prod");
        mtConfigClientV1.setScanBasePackage("com.sankuai.meituan.config");
        mtConfigClientV1.init();
        LOG.info(TestConfigBean.key);
        LOG.info(TestConfigBean.mcckey2);
        Assert.assertNotNull(TestConfigBean.abc);
        Assert.assertNotNull(TestConfigBean.mcckey2);
    }

    @Test
    public void testClientPayId() throws MtConfigException {
        MtConfigClient mtConfigClientV1 = new MtConfigClient();
        mtConfigClientV1.setNodeName("payapiin.prod.rate_limiter");
//        mtConfigClientV1.setScanBasePackage("com.sankuai.meituan.config");
        mtConfigClientV1.setModel("v1");
        mtConfigClientV1.init();
        String value = mtConfigClientV1.getValue("rateLimiterRule");
        System.out.println("value = " + value);
    }

    @Test
    public void test() throws MtConfigException, InterruptedException {
        client.addListener("switch", new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOG.info("{} changed oldValue: {} , newValue: {}",
                        new Object[]{key, oldValue, newValue});
            }
        });

        client.setValue("switch", String.valueOf(!Boolean.valueOf(client.getValue("switch"))));
        sleep(1000);
    }

    @Test
    public void testAnnotation() throws Exception {
        String key = "kkk";
        LOG.info("{}:{}", key, client.getValue(key));
        LOG.info("from bean {}:{}", key, TestConfigBean.kkk);
        client.setValue(key, client.getValue(key) + "a");
        LOG.info("{}:{}", key, client.getValue(key));
        LOG.info("from bean {}:{}", key, TestConfigBean.kkk);
    }

    @Test
    public void setValue() throws MtConfigException, InterruptedException {
        String value = "aaa\"bbb\\\"ccc\\\\ddd,,,eee";
        System.out.println(client.setValue("testNewLine", value));
        sleep(1000l);
        System.out.println(client.getValue("testNewLine"));
    }

    @Test
    public void getValue() throws MtConfigException {
        System.out.println(client.getValue("testKey"));
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
    public void testConfigServerHost() {
        client.setConfigServerHost(null);
        client.setConfigServerHost("");
        client.setConfigServerHost("http://config.sankuai.com");
        client.setConfigServerHost("http://config.sankuai.com/");
        client.setConfigServerHost("http://config.sankuai.com///");
        client.setConfigServerHost("http://config.sankuai.com/ / /");
    }
}
