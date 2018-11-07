package com.sankuai.octo.msgp.controller;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.common.BannerService;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/common")
public class CommonController {

    @RequestMapping(value = "business", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String business() {
        return JsonHelper.dataJson(CommonHelper.businessList());
    }

    @RequestMapping(value = "owt", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String owt(@RequestParam(value = "business") int business) {
        return JsonHelper.dataJson(CommonHelper.owtList(business));
    }

    @RequestMapping(value = "pdl", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String pdl(@RequestParam(value = "owt") String owt) {
        return JsonHelper.dataJson(CommonHelper.pdlList(owt));
    }

    @RequestMapping(value = "level", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String level() {
        return JsonHelper.dataJson(CommonHelper.levelList());
    }

    @RequestMapping(value = "status", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String status() {
        return JsonHelper.dataJson(CommonHelper.statusMap());
    }

    @RequestMapping(value = "env", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String env() {
        return JsonHelper.dataJson(CommonHelper.envMap());
    }

    @RequestMapping(value = "role", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String role() {
        return JsonHelper.dataJson(CommonHelper.roleMap());
    }

    @RequestMapping(value = "ops", method = RequestMethod.GET)
    public String ops(@RequestParam(value = "appkey", required = false) String appkey, Model model) {
        model.addAttribute("appkey", appkey);
        return "common/ops";
    }

    @RequestMapping(value = "banner/valid_message", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getBannerMessage() {
        return JsonHelper.dataJson(BannerService.getBannerMsg());
    }

    @RequestMapping(value = "banner/all_message", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAllBannerMessage() {
        return JsonHelper.dataJson(BannerService.getAllBannerMsg());
    }

    @RequestMapping(value = "banner/insert_message", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String insertBannerMessage(@RequestParam(value = "type", required = false) int type,
                                      @RequestParam(value = "title", required = false) String title,
                                      @RequestParam(value = "content", required = false) String content

    ) {
        return JsonHelper.dataJson(BannerService.insertBannerMsg(type, title, content));
    }

    @RequestMapping(value = "banner/delete_message", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteBannerMessage(@RequestParam(value = "type", required = false) int type,
                                      @RequestParam(value = "title", required = false) String title,
                                      @RequestParam(value = "content", required = false) String content

    ) {
        return JsonHelper.dataJson(BannerService.updateBannerMsg(type, title, content));
    }

    @RequestMapping(value = "online", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String online() {
        return JsonHelper.dataJson(!CommonHelper.isOffline());
    }
}