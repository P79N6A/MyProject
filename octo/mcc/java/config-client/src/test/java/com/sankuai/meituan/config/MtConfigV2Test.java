package com.sankuai.meituan.config;

import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.sentinel.CustomizedManager;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MtConfigV2Test {
    private static final Logger LOG = LoggerFactory.getLogger(MtConfigV2Test.class);

    private String appkey = "com.sankuai.octo.tmy";
    private MtConfigClient client;

    @Before
    public void setUp() {
        //CustomizedManager.setCustomizedSGAgents("10.4.229.149 :5266");
        client = new MtConfigClient();
        client.setAppkey(appkey);
        client.setModel("v2");
        client.setId(String.valueOf(System.currentTimeMillis()));
        client.setPath("gD.d1.d2.d3.d4");
        client.setToken("69FD65D4B6F8FB298A2AB023D66B58744EE955FD");
        client.init();
        //gD  gD.d1     gD.d1.d2       gD.d1.d2.d3     gD.d1.d2.d3.d4
        //    gD(sd1)   gD.d1(sd2)     gD.d1.d2(sd3)   gD.d1.d2.d3(sd4)   gD.d1.d2.d3.d4(sd5)
    }

    @Test
    public void testGet() throws InterruptedException {
        String key = "d";
        client.addListener(key, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                System.out.println("----" + key + " " + oldValue + " " + newValue);
            }
        });
        String value = client.getValue(key);
        System.out.println(value);
        Map<String, String> map =  client.getAllKeyValues();
        Thread.sleep(10000);

    }

    @Test
    public void testSet() throws InterruptedException {
        String key = "d";
        String value = "d4";
        client.addListener(key, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                System.out.println("----" + key + " " + oldValue + " " + newValue);
            }
        });
        client.setValue(key, value);
    }

    @Test
    public void testOldSgAgentGet() throws TException {
        String conf = MnsInvoker.getConfig("com.sankuai.octo.tmy");
        System.out.println("conf = " + conf);
    }

    @Test
    public void testNewSgAgentGet() throws TException {
        String conf = MnsInvoker.getConfig("com.sankuai.octo.tmy");
        System.out.println("conf = " + conf);
    }
}
