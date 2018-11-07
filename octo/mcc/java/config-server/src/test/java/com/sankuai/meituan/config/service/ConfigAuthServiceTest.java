package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.configuration.MccConfiguration;
import com.sankuai.meituan.util.ConfigUtilAdapter;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by liangchen on 2017/12/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:mybatis.xml", "classpath:applicationContext.xml", "classpath:applicationContext-thrift.xml"})
public class ConfigAuthServiceTest  extends TestCase {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigAuthServiceTest.class);

    @Resource
    private ConfigAuthService configAuthService;

    @BeforeClass
    public static void init() {
        try {
            ConfigUtilAdapter.addConfiguration(new MccConfiguration("com.sankuai.cos.mtconfig", "", "com.sankuai.meituan.config.thrift.service.impl"));
            ConfigUtilAdapter.init();
        } catch (Exception e) {
            LOG.warn("fail to init ConfigUtilAdapter");
        }

        ConfigUtilAdapter.setValue("config.zookeeper", "sgconfig-zk.sankuai.com:9331");
        LOG.info(ConfigUtilAdapter.getString("config.zookeeper"));
    }

    @Test
    public void testAuth() {
        boolean isPassed0 = configAuthService.auth(null, "/com.sankuai.octo.tmy");
        Assert.assertFalse(isPassed0);
        boolean isPassed1 = configAuthService.auth("", "/com.sankuai.octo.tmy");
        Assert.assertFalse(isPassed1);
        boolean isPassed2 = configAuthService.auth("12F871EDB38C497D624D5D5C6105501FDD073DDD1", "/com.sankuai.octo.tmy");
        Assert.assertFalse(isPassed2);
        boolean isPassed3 = configAuthService.auth("69FD65D4B6F8FB298A2AB023D66B58744EE955FD", "/com.sankuai.octo.tmy");
        Assert.assertTrue(isPassed3);
        boolean isPassed4 = configAuthService.auth("12F871EDB38C497D624D5D5C6105501FDD073DDD", null);
        Assert.assertFalse(isPassed4);
        boolean isPassed5 = configAuthService.auth("12F871EDB38C497D624D5D5C6105501FDD073DDD", "");
        Assert.assertFalse(isPassed5);
        boolean isPassed6 = configAuthService.auth("12F871EDB38C497D624D5D5C6105501FDD073DDD", "/com.sankuai.cos.mtconfig");
        Assert.assertTrue(isPassed6);
    }
}
