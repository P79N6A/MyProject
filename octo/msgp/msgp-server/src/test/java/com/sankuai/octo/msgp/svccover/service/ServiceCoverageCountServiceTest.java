package com.sankuai.octo.msgp.svccover.service;

import com.sankuai.msgp.common.utils.DateTimeUtil;
import com.sankuai.octo.msgp.service.coverage.ComponentCoverageCollectionService;
import com.sankuai.octo.msgp.service.coverage.ServiceCoverageCountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;

/**
 * Created by huoyanyu on 2017/7/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:applicationContext.xml",
        "classpath*:applicationContext-*.xml",
        "classpath*:mybatis-msgp.xml",
        "classpath*:mybatis-errorlog.xml"
})
public class ServiceCoverageCountServiceTest {
    @Autowired
    private ServiceCoverageCountService serviceCoverageCountService;

    @Resource
    ComponentCoverageCollectionService svc;

    @Test
    public void tmpTest() {
        String date = DateTimeUtil.getYesterday();
        System.out.println(svc.hasOneDayAppkeySvcData(date));
    }

}