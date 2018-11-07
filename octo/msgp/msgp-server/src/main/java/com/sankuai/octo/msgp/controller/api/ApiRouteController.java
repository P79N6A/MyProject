package com.sankuai.octo.msgp.controller.api;

import com.sankuai.msgp.common.service.org.UserService;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.model.EnvMap;
import com.sankuai.octo.msgp.serivce.service.ServiceGroup;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/route")
public class ApiRouteController {


    @RequestMapping(value = "/{appkey}/{type}/status/edit", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.ROUTE, function = "修改分组")
    @ResponseBody
    public String editRouteStatus(@PathVariable("appkey") String appkey,
                                  @PathVariable("type") String type,
                                  @RequestParam("env") String env,
                                  @RequestParam("protocol") String protocol,
                                  @RequestParam("status") Integer status,
                                  @RequestParam("username") String username) {



        // check the arguments
        if (StringUtils.isEmpty(appkey)) {
            return JsonHelper.errorJson("invalid appkey, appkey can't be empty.");
        } else if (!("center".equals(type)||"idc".equals(type))) {

            // only support center/idc
            return JsonHelper.errorJson("invalid route type, route type must be center or idc");
        } else if (!EnvMap.isValid(env)) {
            return JsonHelper.errorJson("invalid env. only support online: prod/staging, offline: dev/ppe/test");
        }else if(!"thrift".equals(protocol)){

            // currently only support thrift. For the extensibility of this API, the argument protocol is reserved.
            return JsonHelper.errorJson("invalid protocol. currently only support thrift");
        } else if (status < 0 || status > 1) {
            return JsonHelper.errorJson("invalid status, status only support 0/1");
        } else if (StringUtils.isEmpty(username)) {
            return JsonHelper.errorJson("invalid username, username can't be empty.");
        }


        // bind the user. username is misid.
        UserService.bindUser(username);



        int envInt = EnvMap.envConvertoEnvInt(EnvMap.withName(env.trim().toLowerCase()));
        String action = (1== status)?"enable":"disable";
        return ServiceGroup.editIdcOrCenterStatus("idc".equals(type), appkey, envInt, action);
    }
}
