package com.sankuai.octo.msgp.controller.realtime;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.msgp.common.config.MsgpConfig;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.HostIp;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.utils.helper.RealtimeHelper;
import com.sankuai.octo.mworth.common.model.Worth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Worth(model = Worth.Model.DataCenter)
public class RTController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/realtime/entry")
    @Worth(model = Worth.Model.DataCenter, function = "实时日志")
    public String entry(@RequestParam String appkey, @RequestParam(value = "env", defaultValue = "3") int env, Model model) {
        User user = UserUtils.getUser();
        String rtServer = MsgpConfig.get("RTServer", "http://10.20.217.145:8080");
        model.addAttribute("logMonitorServer", rtServer);
        logger.info("rtServer:{}", rtServer);
        model.addAttribute("logPath", RealtimeHelper.getLogPath(appkey));
        model.addAttribute("apps", ServiceCommon.apps());
        model.addAttribute("appkey", appkey);
        model.addAttribute("userName", user.getLogin());
        model.addAttribute("env", env);
        model.addAttribute("providers", RealtimeHelper.getHostIP(appkey, env));
        return "realtime/realtime";
    }

    @RequestMapping(value = "/api/rt/hosts", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String hosts(@RequestParam("appkey") String appkey, @RequestParam("env") int env) {
        try {
            List<HostIp> providers = RealtimeHelper.getHostIP(appkey, env);
            return JsonHelper.dataJson(providers);
        } catch (Exception e) {
            return JsonHelper.errorDataJson(e.getMessage());
        }
    }

}
