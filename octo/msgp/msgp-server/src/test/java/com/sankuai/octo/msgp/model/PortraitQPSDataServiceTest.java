package com.sankuai.octo.msgp.model;

import com.sankuai.octo.msgp.service.portrait.PortraitQPSDataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * Created by zmz on 2017/8/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:applicationContext.xml", "classpath*:applicationContext-*.xml", "classpath*:mybatis*.xml"})
public class PortraitQPSDataServiceTest {

    private String appkey = "com.sankuai.inf.msgp";

    @Autowired
    PortraitQPSDataService portraitQPSDataService;

    @Test
    public void getQPSFeatureData() throws Exception {
        System.out.println(portraitQPSDataService.getQPSFeatureData(appkey));
    }

    @Test
    public void getServicePropertyQPSPicData() throws Exception {
        PortraitQPSDataService portraitQPSDataService = new PortraitQPSDataService();
        Map<String, List> ans = portraitQPSDataService.getQPSPicData(appkey);
        System.out.println(ans);
    }

}