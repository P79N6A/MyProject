package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.configuration.MccConfiguration;
import com.sankuai.meituan.util.ConfigUtilAdapter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by liangchen on 2017/12/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:mybatis.xml", "classpath:applicationContext.xml", "classpath:applicationContext-thrift.xml"})
public class SpaceConfigServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(SpaceConfigServiceTest.class);
    @Autowired
    private SpaceConfigService spaceConfigService;

    @BeforeClass
    public static void initConfig() {
        try {
            ConfigUtilAdapter.addConfiguration(new MccConfiguration("com.sankuai.octo.tmy", "", ""));
            ConfigUtilAdapter.init();
        } catch (Exception e){
            LOG.warn("fail to init ConfigUtilAdapter");
        }
        ConfigUtilAdapter.setValue("config.zookeeper","sgconfig-zk.sankuai.com:9331");
    }

    @Test
    public void testcCanAddNodeInV2() {
        String spacePath = "/com.sankuai.octo.tmy/prod";
        boolean canAddNode = spaceConfigService.canAddNodeInV2(spacePath, false);
        Assert.assertTrue(canAddNode);
    }

}
