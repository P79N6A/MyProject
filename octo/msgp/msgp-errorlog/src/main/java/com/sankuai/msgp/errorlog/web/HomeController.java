package com.sankuai.msgp.errorlog.web;

import com.sankuai.msgp.errorlog.domain.Result;
import com.sankuai.msgp.errorlog.service.AlarmHostCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private AlarmHostCheckService alarmHostCheckService;

    @RequestMapping(value = "/monitor/alive")
    public Result alive() {
        return  new Result("ok");
    }

    @RequestMapping(value = "/alarmnode/update")
    public Result updateFalconAlarmNode() {
        try {
            Result result = alarmHostCheckService.updateAlarmVirtualNode();
            return result;
        } catch (Exception e) {
            logger.error("updateFalconAlarmNode failed", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return new Result(errorMsg, false);
        }
    }

    @RequestMapping(value = "/appkeybind/check")
    public Result checkOctoAppkeySvrTreeBind() {
        try {
            Result result = alarmHostCheckService.checkOctoAppkeySvrTreeBind();
            return result;
        } catch (Exception e) {
            logger.error("checkOctoAppkeySvrTreeBind failed", e);
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return new Result(errorMsg, false);
        }
    }
}
