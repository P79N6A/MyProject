package com.sankuai.octo.msgp.controller.mns;

import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/**
 * 命名服务的接口
 * 检查mns下服务节点的完整性，并输出
 */
@Controller
@RequestMapping("/mns")
@Worth(model = Worth.Model.MNS)
@Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
public class MnsController {



    /**
     * 获取服务节点数量
     * status 默认值为-1
     */
    @RequestMapping(value = "node/check", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public List<String> check(@RequestParam(value = "appkey",required = false) String appkey) {
        return ServiceCommon.checkMnsNode();
    }

}
