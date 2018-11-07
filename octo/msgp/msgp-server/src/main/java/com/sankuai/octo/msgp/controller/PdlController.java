package com.sankuai.octo.msgp.controller;

import com.sankuai.msgp.common.model.Pdl;
import com.sankuai.msgp.common.service.org.OpsService;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.mworth.common.model.Worth;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

/**
 * 业务线 操作
 */
@Controller
@RequestMapping("/pdl")
@Worth(model = Worth.Model.OTHER)
public class PdlController {

    @RequestMapping(value = "list", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String list(@RequestParam(value = "owt", required = false) String owt) {
        return JsonHelper.dataJson(OpsService.pdlList(owt));
    }

    @RequestMapping(value = "saveOwner", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String list(
            @RequestParam(value = "owt", required = true) String owt,
            @RequestParam(value = "pdl", required = false,defaultValue = "") String pdl,
            @RequestParam(value = "owners", required = true) String owners
    ) {
        Pdl pdlObj = new Pdl(owt,pdl);
        List<String> ownerList = Arrays.asList(owners.split(","));
        return JsonHelper.dataJson(OpsService.saveOwtOwner(pdlObj,ownerList));
    }

}
