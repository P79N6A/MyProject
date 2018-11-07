package com.sankuai.octo.msgp.controller.oauth2;

import com.sankuai.octo.msgp.serivce.Oauth2;
import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/oauth2/api")
@Worth(model = Worth.Model.OTHER)
public class Oauth2ApiController {
    @RequestMapping(value = "{appkey}/desc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appkeyDesc(@PathVariable(value = "appkey") String appkey,
                             @RequestParam(value = "access_token")String accessToken) {
        return Oauth2.getDescByToken(accessToken, appkey);
    }
}
