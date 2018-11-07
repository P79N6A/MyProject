package com.sankuai.meituan.config.service;

import com.alibaba.fastjson.JSON;
import com.sankuai.meituan.config.configuration.MccConfiguration;
import com.sankuai.meituan.config.model.OperationFileLog;
import com.sankuai.meituan.config.model.OperationLog;
import com.sankuai.meituan.config.model.Page;
import com.sankuai.meituan.util.ConfigUtilAdapter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by liangchen on 2017/9/25.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:mybatis.xml", "classpath:applicationContext.xml", "classpath:applicationContext-thrift.xml"})
public class OperationRecordServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(OperationRecordServiceTest.class);

    @Resource
    private OperationRecordService operationRecordService;

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
    public void testgetOperationFileRecord() {
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(20);
        page.setTotalCount(-1);

        Date start = new Date("Mon Sep 1 16:17:11 CST 2017");
        Date end = new Date("Mon Sep 25 16:17:11 CST 2017");
        Collection<OperationFileLog> list = operationRecordService.getOperationFileRecord("/filelog/com.sankuai.octo.tmy/prod", start, new Date(), page);
        Assert.assertNotNull(list);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void testgetOperationRecord() {
        Page page = new Page();
        page.setPageNo(1);
        page.setPageSize(20);
        page.setTotalCount(-1);

        Date start = new Date("Mon Sep 1 16:17:11 CST 2017");
        Date end = new Date("Mon Sep 25 16:17:11 CST 2017");
        List<String> entityIds = Arrays.asList("/com.sankuai.octo.tmy/prod");
        Collection<OperationLog> list = operationRecordService.getOperationRecord(entityIds, start, new Date(), page);
        Assert.assertNotNull(list);
        System.out.println(JSON.toJSONString(list));
    }
}
