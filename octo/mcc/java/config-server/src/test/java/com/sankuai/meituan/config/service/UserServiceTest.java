package com.sankuai.meituan.config.service;

import com.alibaba.fastjson.JSON;
import com.sankuai.meituan.config.configuration.MccConfiguration;
import com.sankuai.meituan.config.model.ConfigSpace;
import com.sankuai.meituan.config.model.UserBean;
import com.sankuai.meituan.org.remote.vo.OrgTreeNodeVo;
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

import java.util.List;
import java.util.Set;

/**
 * Created by liangchen on 2017/9/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:mybatis.xml", "classpath:applicationContext.xml", "classpath:applicationContext-thrift.xml"})
public class UserServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceTest.class);
    @Autowired
    private UserService service;

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
    public void testConfigAdminService() {
        Assert.assertTrue(service.isConfigAdmin(1));

        Assert.assertTrue(service.isSpaceAdmin("com.sankuai.octo.tmy", 24567));

        List<ConfigSpace> list = service.getConfigSpaces(24567);
        System.out.println(JSON.toJSONString(list));

        Set<String> spaces = service.getSpaces(24567);
        System.out.println(JSON.toJSONString(spaces));

        List<UserBean> configAdmins = service.getConfigAdmins();
        Assert.assertNotNull(configAdmins);
        System.out.println(JSON.toJSONString(configAdmins));

        try {
            System.out.println(service.addConfigAdmin(100000000));
            Assert.assertTrue(service.isConfigAdmin(100000000));
        } catch (Exception e) {
            LOG.warn("addConfigAdmin failed");
        }
        try {
            System.out.println(service.deleteConfigAdmin(100000000));
            Assert.assertFalse(service.isConfigAdmin(100000000));
        } catch (Exception e) {
            LOG.warn("deleteConfigAdmin failed");
        }

        List<OrgTreeNodeVo> nodeVos = service.empListSearch("com.sankuai.octo.tmy");
        Assert.assertNotNull(nodeVos);
        System.out.println(JSON.toJSONString(nodeVos));
    }
}
