package com.sankuai.octo.msgp.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.JsonUtil;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.msgp.common.config.db.msgp.Tables;
import com.sankuai.octo.msgp.model.SubsStatus;
import com.sankuai.octo.msgp.serivce.AppSubscribeService;
import com.sankuai.octo.msgp.serivce.subscribe.AppkeySubscribe;
import com.sankuai.octo.msgp.serivce.subscribe.ReportSubscribe;
import com.sankuai.octo.msgp.service.subscribe.SubscribeService;
import com.sankuai.octo.msgp.utils.Auth;
import de.ruedigermoeller.serialization.dson.DsonDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/subscribe")
public class SubscribeController {

    private static final Logger LOG = LoggerFactory.getLogger(SubscribeController.class);

    @Resource
    private SubscribeService subscribeService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Model model) {
        return "dashboard/subs";
    }

    @RequestMapping(value = "/listsubs", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String listSubs(Page page) {
        User user = UserUtils.getUser();
        List<SubsStatus> list = subscribeService.listSubsByUser(user, page);
        return JsonHelper.dataJson(list, page);
    }

    @RequestMapping(value = "/changeSingle", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String changeSingle(@RequestBody String data) {
        LOG.info(data);
        User user = UserUtils.getUser();
        JSONObject o = JSON.parseObject(data);
        String appkey = o.getString("appkey");
        String option = o.getString("option");
        int subStatus = o.getInteger("subStatus");
        subscribeService.singleChangeSubs(user, appkey, option, subStatus);
        return JsonHelper.dataJson("修改成功");
    }

    @RequestMapping(value = "/changeList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String changeList(@RequestBody String data) {
        try {
            LOG.info(data);
            User user = UserUtils.getUser();
            int enable = JSON.parseObject(data).getInteger("enabled");
            List<SubsStatus> list = JsonUtil.converToList(JSON.parseObject(data).getString("data"), SubsStatus.class);
            subscribeService.batchChangeSubs(user, enable, list);
            return JsonHelper.dataJson("修改成功");
        } catch (IOException e) {
            LOG.error("parse json error");
            return JsonHelper.errorDataJson("修改失败");
        }
    }

    //用户服务订阅表 的查询
    @RequestMapping(value = "list", method = RequestMethod.GET, produces = "application/json")
    @Auth(level = Auth.Level.READ, responseMode = Auth.ResponseMode.JSON)
    @ResponseBody
    public String search(@RequestParam(value = "appkey", required = false) String appkey, Page page) {
        scala.collection.immutable.List<Tables.AppSubscribeRow> list = AppSubscribeService.search(appkey, UserUtils.getUser().getId(), page);
        return JsonHelper.dataJson(list, page);
    }

    //用户 添加 服务订阅表
    @RequestMapping(value = "{appkey}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    @Auth(level = Auth.Level.READ, responseMode = Auth.ResponseMode.JSON)
    public String add(@PathVariable("appkey") String appkey,
                      @RequestParam(value = "userId", required = true) Long userId,
                      @RequestParam(value = "username", required = true) String username) {
        long count = AppSubscribeService.insert(appkey, username, userId);
        return JsonHelper.dataJson(count);
    }

    //用户 添加 服务订阅表
    @RequestMapping(value = "save", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    @Auth(level = Auth.Level.READ, responseMode = Auth.ResponseMode.JSON)
    public String save(@RequestParam(value = "appkeys", required = true) String appkeys) {
        Long userId = Long.valueOf(UserUtils.getUser().getId());
        String username = UserUtils.getUser().getName();
        AppSubscribeService.delete(null, userId);
        String[] arr_appkey = appkeys.split(",");
        long count = 0;
        for (String appkey : arr_appkey) {
            if (StringUtils.isBlank(appkey)) {
                continue;
            }
            AppSubscribeService.insert(appkey, username, userId);
            count++;
        }
        return JsonHelper.dataJson(count);
    }

    //删除
    @RequestMapping(value = "{appkey}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    @Auth(level = Auth.Level.READ, responseMode = Auth.ResponseMode.JSON)
    public boolean delete(@PathVariable("appkey") String appkey,
                          @RequestParam(value = "userId", required = false) Long userId) {
        AppSubscribeService.delete(appkey, userId);
        return true;
    }

    /**
     * 增加日报和周报的订阅
     *
     * @param appkey
     * @return
     */
    @RequestMapping(value = "report/subscribe", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @Auth(level = Auth.Level.READ, responseMode = Auth.ResponseMode.JSON)
    public String subscribeReport(@RequestParam("appkey") String appkey) {
        try {
            return JsonHelper.dataJson(ReportSubscribe.addReportSubscribe(UserUtils.getUser().getLogin(), appkey));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "report/unsubscribe", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @Auth(level = Auth.Level.READ, responseMode = Auth.ResponseMode.JSON)
    public String unsubscribeReport(@RequestParam("appkey") String appkey) {
        try {
            return JsonHelper.dataJson(ReportSubscribe.cancelReportSubscribe(UserUtils.getUser().getLogin(), appkey));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "report/subscribedAppkey", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @Auth(level = Auth.Level.READ, responseMode = Auth.ResponseMode.JSON)
    public String getSubscribeForReport() {
        try {
            return JsonHelper.dataJson(AppkeySubscribe.getSubscribeForDailyReport(UserUtils.getUser().getLogin()));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }
}
