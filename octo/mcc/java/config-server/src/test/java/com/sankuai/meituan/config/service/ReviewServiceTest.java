package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.configuration.MccConfiguration;
import com.sankuai.meituan.util.ConfigUtilAdapter;
import com.sankuai.octo.config.model.PRDetail;
import com.sankuai.octo.config.model.PullRequest;
import com.sankuai.octo.config.model.Review;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * Created by lhmily on 06/13/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:mybatis.xml", "classpath:applicationContext.xml", "classpath:applicationContext-thrift.xml"})
public class ReviewServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceTest.class);
    @Autowired
    private ReviewService reviewService;

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

    private void createPR() {
        PullRequest pr = new PullRequest();
        List<PRDetail> detailList = new ArrayList<PRDetail>();
        for (int i = 0; i < 3; ++i) {
            PRDetail item = new PRDetail();
            item.setKey("key" + i);
            item.setNewValue("value" + i);
            detailList.add(item);
        }
        pr.setPrMisID("yangjie17").setEnv(3).setAppkey("com.sankuai.octo.tmy").setNote("test PR");
        reviewService.createPR(pr, detailList);
    }

    @Test
    public void testPR() throws InterruptedException {
        createPR();
        Thread.sleep(5000);
        List<PullRequest> list = reviewService.getPullRequest("com.sankuai.octo.tmy", 3, 0);
        Assert.assertNotNull(list);

        PullRequest item = list.get(0);

        String contentText = "test PR" + new Date();
        item.setNote(contentText);
        reviewService.updatePR(item);

        Thread.sleep(3000);

        List<Review> listReview = reviewService.getReview(item.getPrID());
        Assert.assertNotNull(listReview);

        list = reviewService.getPullRequest("com.sankuai.octo.tmy", 3, 0);
        Assert.assertNotNull(list);
        boolean isUpdate = false;
        for (PullRequest itemTemp : list) {
            if ((itemTemp.getPrID() == item.getPrID()) && contentText.equals(itemTemp.getNote())) {
                isUpdate = true;
            }
        }
        Assert.assertTrue(isUpdate);
        Thread.sleep(3000);

//        reviewService.mergePR(item.getPrID());

//        Thread.sleep(3000);

        reviewService.deletePR(item.getPrID());
        list = reviewService.getPullRequest("com.sankuai.octo.tmy", 3, 0);
        Assert.assertNotNull(list);
        isUpdate = false;
        for (PullRequest itemTemp : list) {
            if ((itemTemp.getPrID() == item.getPrID()) && contentText.equals(itemTemp.getNote())) {
                isUpdate = true;
            }
        }
        Assert.assertFalse(isUpdate);
        Thread.sleep(200);
    }

}
