package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.configuration.MccConfiguration;
import com.sankuai.meituan.util.ConfigUtilAdapter;
import junit.framework.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by liangchen on 2017/12/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:mybatis.xml", "classpath:applicationContext.xml", "classpath:applicationContext-thrift.xml"})
public class MnscServiceTest extends TestCase {
    private static final Logger LOG = LoggerFactory.getLogger(MnscServiceTest.class);

    @Resource
    private MnscService mnscService;

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
    public void testGetAllAppkeys() {
        mnscService.asynDeleteOfflineNode("10.4.245.3");
        //Assert.assertNotNull(appkeys);
    }

}
