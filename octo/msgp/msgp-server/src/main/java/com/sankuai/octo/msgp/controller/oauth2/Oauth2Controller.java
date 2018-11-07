package com.sankuai.octo.msgp.controller.oauth2;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.octo.msgp.serivce.Oauth2;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
@RequestMapping("/oauth2")
public class Oauth2Controller {

    @RequestMapping(value = "authorize", method = RequestMethod.GET)
    @Worth(model = Worth.Model.MNS, function = "服务授权")
    public String auth(@RequestParam(value = "response_type") String responseType,
                       @RequestParam(value = "client_id") String clientId,
                       @RequestParam(value = "redirect_uri", required = true) String redirectUri,
                       Model model) {
        User user = UserUtils.getUser();
        model.addAttribute("username", user.getName());
        model.addAttribute("response_type", responseType);
        model.addAttribute("client_id", clientId);
        model.addAttribute("redirect_uri", redirectUri);
//        model.addAttribute("apps",new ArrayList<String>());
        model.addAttribute("apps", ServiceCommon.appsByUser());
        //TODO 检查参数是否正确，然后返回错误页面
        return "oauth2/authorize";
    }

    @RequestMapping(value = "token", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String token(HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        User user = UserUtils.getUser();
        return Oauth2.token(json, user);
    }

    /**
     * 该请求格式为GET，返回为json
     */
    @RequestMapping(value = "accesstoken", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String accessToken(@RequestParam(value = "grant_type") String grantType,
                              @RequestParam(value = "code") String code,
                              @RequestParam(value = "client_id") String clientId,
                              @RequestParam(value = "client_secret") String clientSecret,
                              @RequestParam(value = "redirect_uri") String redirectUri) {
        return Oauth2.getAccessToken(grantType, code, clientId, clientSecret, redirectUri);
    }

}
