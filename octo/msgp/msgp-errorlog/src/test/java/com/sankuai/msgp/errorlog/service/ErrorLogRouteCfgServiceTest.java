package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.errorlog.ApplicationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by emma on 2017/9/21.
 */
public class ErrorLogRouteCfgServiceTest extends ApplicationTest {

    @Autowired
    private ErrorLogRouteCfgService routeCfgService;

    @Test
    public void isRouteAppkeyTest() {
        System.out.println(routeCfgService.isRouteAppkey("com.sankuai.inf.msgp"));
    }
}
