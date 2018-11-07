package com.sankuai.meituan.config.test;

import com.sankuai.meituan.config.configuration.MccConfiguration;
import com.sankuai.meituan.config.service.ConfigTairClient;
import com.sankuai.meituan.util.ConfigUtilAdapter;
import com.sankuai.octo.config.model.ConfigGroup;
import com.sankuai.octo.config.model.ConfigGroups;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:mybatis.xml", "classpath:applicationContext.xml", "classpath:applicationContext-thrift.xml"})
public class TairTest {

    String env = "prod";
    String appkey = "com.sankuai.octo.tmy";

    @javax.annotation.Resource
    private ConfigTairClient client;
    private static final Logger LOG = LoggerFactory.getLogger(TairTest.class);
    @BeforeClass
    public static  void init(){
        try {
            ConfigUtilAdapter.addConfiguration(new MccConfiguration("com.sankuai.octo.tmy", "", ""));
            ConfigUtilAdapter.init();
        } catch (Exception e){
            LOG.warn("fail to init ConfigUtilAdapter");
        }
        ConfigUtilAdapter.setValue("config.zookeeper","sgconfig-zk.sankuai.com:9331");
    }

    @Test
    public void testGroup() throws TException {
        client.init();

        ConfigGroups groups = client.getGroups(env, appkey);
        System.out.println(groups);
        groups.getGroups().clear();
        client.updateGroup(env, appkey, groups);
        ConfigGroup group = client.addGroup(env, appkey, "test", Arrays.asList("192.168.1.1"));
        groups = client.getGroups(env, appkey);
        Assert.assertNotNull(groups);
        Assert.assertNotNull(groups.getGroups());
        boolean isExist = false;
        for (ConfigGroup item : groups.getGroups()) {
            if("test".equals(item.getName())){
                isExist = true;
                break;
            }
        }
        Assert.assertTrue(isExist);

        ConfigGroup newGroup = client.updateGroup(env, appkey, group.getId(), Arrays.asList("172.16.3.3"));
        Assert.assertTrue(Arrays.asList("172.16.3.3").equals(newGroup.getIps()));

        System.out.println(newGroup);

        int code = client.deleteGroup(env, appkey, newGroup.getId());
        Assert.assertTrue(0 == code);

        ConfigGroups groups1 = client.getGroups(env, appkey);
        Assert.assertNotNull(groups);
        System.out.println(groups1);

    }
}
