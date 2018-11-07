package com.sankuai.msgp.errorlog.web;

import com.sankuai.msgp.errorlog.domain.Result;
import com.sankuai.msgp.errorlog.service.ErrorLogRouteAdjustService;
import com.sankuai.msgp.errorlog.service.ErrorLogRouteCfgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yves on 17/3/6.
 */

@RestController
@RequestMapping("route")
public class RouteController {

    @Autowired
    private ErrorLogRouteAdjustService errorLogRouteService;
    @Autowired
    private ErrorLogRouteCfgService routeCfgService;

    /**
     * 通过msgp调用，手动触发路由变更
     * @return
     */
    @RequestMapping(value = "/config/adjust")
    public Result adjustRouteConfig() {
        boolean isAdjusted = errorLogRouteService.dynamicAdjustAppkeyRoute(routeCfgService.getNodeAppkeyRouteMap(), false);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isAdjusted", isAdjusted);
        return new Result(result);
    }
}
