package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.errorlog.ApplicationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yves on 17/3/3.
 */
public class ErrorLogRouteServiceTest extends ApplicationTest {

    @Autowired
    ErrorLogRouteAdjustService errorLogRouteService;
    @Autowired
    ErrorLogRouteCfgService routeCfgService;

    @Test
    public void testGenHostRoute() {
        System.out.println(errorLogRouteService.generateInitAppkeyRouteConfig(7));
    }

    @Test
    public void testDynamicAdjustWithoutDefaultHost() {
        errorLogRouteService.dynamicAdjustAppkeyRoute(routeCfgService.getNodeAppkeyRouteMap(), false);
    }

    @Test
    public void testDynamicAdjustDecereaseNode() {
        List<String> decreaseList = new ArrayList<>();
        decreaseList.add("node5");
        errorLogRouteService.dynamicAdjustDecreaseNode(decreaseList);
    }

    @Test
    public void testDynamicAdjustIncereaseNode() {
        List<String> increaseList = new ArrayList<>();
        increaseList.add("node8");
        errorLogRouteService.dynamicAdjustIncreaseNode(increaseList);
    }
}
