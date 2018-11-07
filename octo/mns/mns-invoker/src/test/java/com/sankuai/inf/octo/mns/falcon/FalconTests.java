package com.sankuai.inf.octo.mns.falcon;

import com.sankuai.inf.octo.mns.falcon.FalconCollect;
import com.sankuai.inf.octo.mns.util.CommonUtil;
import junit.framework.Assert;
import org.junit.Test;

import java.sql.ResultSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-2
 * Time: 下午3:32
 */

public class FalconTests {

    public static void main(String[] args) {
        System.out.println("Start!");
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    FalconCollect.addItem("test1", "id=1,name=11");
                    FalconCollect.addItem("test2", "id=2,name=22", 10);
                    FalconCollect.setItem("test3", "id=3,name=33", String.valueOf(99.333333));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 17, TimeUnit.SECONDS);

    }

    @Test
    public void testGetHostName() {
        Assert.assertTrue(!CommonUtil.isBlankString(ReportUtil.getLocalHostName()));
    }
}
