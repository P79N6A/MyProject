package com.sankuai.octo.msgp.model;

import com.sankuai.octo.msgp.service.portrait.PortraitLoadDataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * Created by zmz on 2017/9/20.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:applicationContext.xml", "classpath*:applicationContext-*.xml", "classpath*:mybatis*.xml"})
public class PortraitLoadDataServiceTest {

    @Autowired
    PortraitLoadDataService portraitLoadDataService;

    private String appkey = "com.sankuai.inf.msgp";

    @Test
    public void getLoadFeatureData() throws Exception {
        System.out.println(portraitLoadDataService.getLoadFeatureData(appkey));
    }

    @Test
    public void getServiceResourceLoadPicData() throws Exception {
        PortraitLoadDataService portraitLoadDataService = new PortraitLoadDataService();
        Map<String, List> ans = portraitLoadDataService.getServiceResourceLoadPicData(appkey);
        System.out.println(ans);
    }

}