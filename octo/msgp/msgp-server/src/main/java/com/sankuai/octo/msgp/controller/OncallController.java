package com.sankuai.octo.msgp.controller;

import com.sankuai.msgp.common.utils.JsonUtil;
import com.sankuai.msgp.common.utils.client.TairClient;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.OncallDesc;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.utils.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 值班人操作类
 * Created by nero on 2018/7/11
 */
@Controller
public class OncallController {

    private Logger LOG = LoggerFactory.getLogger(OncallController.class);

    public static final String ONCALL_PRIFIX = "oncall_";

    private final Integer expire = 24 * 3600 * 7;

    @RequestMapping(value = "/oncall/save", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String listOncall(@RequestBody OncallDesc oncallDesc) {
        if (oncallDesc.getData().size() > 0) {
            TairClient.put(ONCALL_PRIFIX + oncallDesc.getAppkey(), oncallDesc.toDescStr(), expire);
            return JsonHelper.dataJson("添加成功");
        } else {
            return JsonHelper.errorDataJson("请添加有效mis账号!");
        }
    }
}
