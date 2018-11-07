package com.sankuai.octo.msgp.controller;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.config.db.msgp.Tables;
import com.sankuai.msgp.common.model.Env;
import com.sankuai.msgp.common.model.MonitorModels;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.utils.JsonUtil;
import com.sankuai.msgp.common.utils.client.TairClient;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.dao.kpi.AppScreenDao;
import com.sankuai.octo.msgp.dao.kpi.BusinessDashDao;
import com.sankuai.octo.msgp.dao.monitor.BusinessMonitorDAO;
import com.sankuai.octo.msgp.domain.UserSubscribeMonitor;
import com.sankuai.octo.msgp.serivce.AppkeyAuth;
import com.sankuai.octo.msgp.serivce.monitor.MonitorConfig;
import com.sankuai.octo.msgp.serivce.monitor.MonitorEvent;
import com.sankuai.octo.msgp.serivce.other.PerfApi;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.octo.mworth.common.model.Worth;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import scala.Option;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/monitor")
@Worth(model = Worth.Model.Monitor)
@Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
public class MonitorController {

    @Worth(model = Worth.Model.Monitor, function = "查询告警配置")
    @RequestMapping(value = "config", method = RequestMethod.GET)
    public String monitor(@RequestParam(value = "appkey", required = false) String appkey,
                          Model model) {
        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("appkey", appkey);
        model.addAttribute("apps", apps);

        model.addAttribute("triggerItems", MonitorConfig.triggerItems());
        model.addAttribute("triggerFunctions", MonitorConfig.triggerFunctions());
        return "monitor/config";
    }

    @RequestMapping(value = "{appkey}/spannames", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String apis(@PathVariable("appkey") String appkey,
                       @RequestParam("side") String side) {
        java.util.List<String> spannameList = MonitorConfig.getSpans(appkey, side, Env.prod().toString());
        return JsonHelper.dataJson(spannameList);
    }

    @RequestMapping(value = "{appkey}/trigger/items", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String items(@PathVariable("appkey") String appkey) {
        return JsonHelper.dataJson(MonitorConfig.triggerItems());
    }

    @RequestMapping(value = "{appkey}/provider/trigger/items", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String providerItems(@PathVariable("appkey") String appkey) {
        return JsonHelper.dataJson(MonitorConfig.providerTriggerItems());
    }

    @RequestMapping(value = "{appkey}/trigger/functions", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String functions(@PathVariable("appkey") String appkey) {
        return JsonHelper.dataJson(MonitorConfig.triggerFunctions());
    }

    @RequestMapping(value = "{appkey}/consumerAppkeys", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String apiConsumer(@PathVariable("appkey") String appkey,
                              @RequestParam("side") String side) {
        java.util.List<String> consumerAppkeyList = PerfApi.queryConsumer(appkey, side);
        return JsonHelper.dataJson(consumerAppkeyList);
    }

    @Worth(model = Worth.Model.Monitor, function = "查询告警配置")
    @RequestMapping(value = "{appkey}/triggers", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String triggers(@PathVariable("appkey") String appkey) {
        scala.collection.immutable.List<MonitorModels.AppkeyTriggerWithSubStatus> triggers = MonitorConfig.getTriggersWithSubStatus(appkey);
        return JsonHelper.dataJson(triggers);
    }

    @Worth(model = Worth.Model.Monitor, function = "查询服务节点告警配置")
    @RequestMapping(value = "{appkey}/provider/triggers", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String providerTriggers(@PathVariable("appkey") String appkey) {
        scala.collection.immutable.List<MonitorModels.AppkeyProviderTriggerWithSubStatus> triggers = MonitorConfig.getProviderTriggersWithSubStatus(appkey);
        return JsonHelper.dataJson(triggers);
    }

    @Worth(model = Worth.Model.Monitor, function = "修改告警配置")
    @RequestMapping(value = "{appkey}/trigger", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateTrigger(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        MonitorConfig.updateTrigger(appkey, json);
        return JsonHelper.dataJson(true);
    }

    @Worth(model = Worth.Model.Monitor, function = "批量插入告警配置")
    @RequestMapping(value = "{appkey}/triggers/insert/batch", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String batchUpdateTrigger(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        MonitorConfig.batchUpdateTrigger(appkey, json);
        return JsonHelper.dataJson(true);
    }

    @Worth(model = Worth.Model.Monitor, function = "批量插入告警配置")
    @RequestMapping(value = "{appkey}/triggers/itemDesc/count", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String getItemDescCount(@PathVariable("appkey") String appkey,
                                   @RequestParam("itemDesc") String itemDesc,
                                   @RequestParam("side") String side) {
        return JsonHelper.dataJson(MonitorConfig.getItemDescCount(appkey, itemDesc, side));
    }

    @Worth(model = Worth.Model.Monitor, function = "查询核心接口")
    @RequestMapping(value = "{appkey}/triggers/span/core", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String getCoreSpan(@PathVariable("appkey") String appkey,
                              @RequestParam("span") String span) {
        return JsonHelper.dataJson(MonitorConfig.getCoreClientSpan(appkey, span));
    }

    @Worth(model = Worth.Model.Monitor, function = "修改服务节点告警配置")
    @RequestMapping(value = "{appkey}/provider/trigger", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String updateProviderTrigger(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        MonitorConfig.updateProviderTrigger(appkey, json);
        return JsonHelper.dataJson(true);
    }

    @Worth(model = Worth.Model.Monitor, function = "修改告警配置")
    @RequestMapping(value = "{appkey}/trigger", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String deleteTrigger(@PathVariable("appkey") String appkey,
                                @RequestParam("id") Long id) throws IOException {
        MonitorConfig.deleteTrigger(appkey, id);
        return JsonHelper.dataJson(true);
    }

    @Worth(model = Worth.Model.Monitor, function = "修改告警配置")
    @RequestMapping(value = "{appkey}/provider/trigger", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    public String deleteProviderTrigger(@PathVariable("appkey") String appkey,
                                        @RequestParam("id") Long id) throws IOException {
        MonitorConfig.deleteProviderTrigger(appkey, id);
        return JsonHelper.dataJson(true);
    }

    @Worth(model = Worth.Model.Monitor, function = "批量订阅")
    @RequestMapping(value = "{appkey}/trigger/subscribe/batch", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String batchSubscribe(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        return MonitorConfig.batchSubscribe(appkey, json);
    }

    @Worth(model = Worth.Model.Monitor, function = "订阅告警")
    @RequestMapping(value = "{appkey}/trigger/subscribe", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String subscribe2(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        return MonitorConfig.subscribe(appkey, json);
    }


    @Worth(model = Worth.Model.Monitor, function = "批量订阅")
    @RequestMapping(value = "{appkey}/provider/trigger/subscribe/batch", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String providerBatchSubscribe(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        return MonitorConfig.providerBatchSubscribe(appkey, json);
    }

    @Worth(model = Worth.Model.Monitor, function = "订阅告警")
    @RequestMapping(value = "{appkey}/provider/trigger/subscribe", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String providerSubscribe(@PathVariable("appkey") String appkey, HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        return MonitorConfig.providerSubscribe(appkey, json);
    }


    @Worth(model = Worth.Model.Monitor, function = "订阅告警")
    @RequestMapping(value = "{appkey}/trigger/subscribe2", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String subscribe(@PathVariable("appkey") String appkey, @RequestBody UserSubscribeMonitor userSubscribeMonitor) throws IOException {
        return MonitorConfig.subscribe2(userSubscribeMonitor);
    }


    @Worth(model = Worth.Model.Monitor, function = "查询告警记录")
    @RequestMapping(value = "log", method = RequestMethod.GET)
    public String log(@RequestParam(value = "appkey", required = false) String appkey, Model model) {
        List<String> apps = ServiceCommon.apps();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("appkey", appkey);
        model.addAttribute("apps", apps);
        return "monitor/list";
    }

    @Worth(model = Worth.Model.Monitor, function = "查询告警记录")
    @RequestMapping(value = "{appkey}/log", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getEvent(@PathVariable("appkey") String appkey,
                           @RequestParam(value = "start", required = false) String start,
                           @RequestParam(value = "end", required = false) String end,
                           Page page) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Long startTime = (start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis();
        Long endTime = (end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis();
        scala.collection.immutable.List<Tables.EventRow> result = MonitorEvent.getEvents(appkey, startTime, endTime, page);
        return JsonHelper.dataJson(result, page);
    }

    @Worth(model = Worth.Model.Monitor, function = "告警确认")
    @RequestMapping(value = "{appkey}/ack", method = RequestMethod.GET)
    public String ack(@PathVariable("appkey") String appkey,
                      @RequestParam(value = "side", required = false) String side,
                      @RequestParam(value = "spanname", required = false) String spanname,
                      @RequestParam(value = "eventId", required = false) Long eventId) {
        MonitorEvent.ackEvent(eventId);
        return "redirect:/monitor/log" + "?appkey=" + appkey + "&side=" + side + "&spanname=" + spanname;
    }

    @Worth(model = Worth.Model.Monitor, function = "查询业务告警配置")
    @RequestMapping(value = "business", method = RequestMethod.GET)
    public String businessConfig(@RequestParam(value = "screenId", defaultValue = "0") Long screenId, Model model) {
        java.util.List<Tables.AppScreenRow> metrics = AppScreenDao.getFrontend();
        screenId = screenId == 0 ? (metrics.isEmpty() ? 0 : metrics.get(0).id()) : screenId;
        String name = "";
        for (Tables.AppScreenRow metric : metrics) {
            if (metric.id() == screenId) {
                name = metric.title() + "(" + metric.metric() + ")";
                break;
            }
        }
        model.addAttribute("name", name);
        model.addAttribute("screenId", screenId);
        return "monitor/business";
    }

    @RequestMapping(value = "business/triggers", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String businessTriggers() {
        return JsonHelper.dataJson(AppScreenDao.get());
    }

    @Worth(model = Worth.Model.Monitor, function = "保存业务告警配置")
    @RequestMapping(value = "business/config", method = RequestMethod.POST)
    @ResponseBody
    public String addBusinessMonitor(@RequestParam(value = "screenId") Long screenId,
                                     @RequestParam(value = "strategy") int strategy,
                                     @RequestParam(value = "desc") String desc,
                                     @RequestParam(value = "threshold") int threshold,
                                     @RequestParam(value = "duration") int duration,
                                     Model model) {
        return JsonHelper.dataJson(BusinessMonitorDAO.insert(
                BusinessMonitorDAO.wrapperBusinessMonitorRow(0, screenId, strategy, desc, threshold, duration)));
    }

    @Worth(model = Worth.Model.Monitor, function = "查询业务告警配置")
    @RequestMapping(value = "business/config", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getBusinessMonitor(@RequestParam(value = "screenId") Long screenId,
                                     Model model) {
        return JsonHelper.dataJson(BusinessMonitorDAO.get(screenId));
    }

    @Worth(model = Worth.Model.Monitor, function = "删除业务告警配置")
    @RequestMapping(value = "business/config", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteBusinessMonitor(@RequestParam(value = "id") Long id,
                                        Model model) {
        return JsonHelper.dataJson(BusinessMonitorDAO.delete(id));
    }

    @Worth(model = Worth.Model.Monitor, function = "订阅业务告警")
    @RequestMapping(value = "business/subscribe", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String businessSubscribe(HttpServletRequest request) throws IOException {
        String json = IOUtils.copyToString(request.getReader());
        return BusinessMonitorDAO.subscribe(json);
    }

    @Worth(model = Worth.Model.Monitor, function = "业务大盘配置")
    @RequestMapping(value = "business/dash/config", method = RequestMethod.GET)
    public String businessDash(@RequestParam(value = "appkey", required = false) String appkey,
                               @RequestParam(value = "owt", required = false) String owt,
                               Model model) {
        //获取当前登录人的业务线
        java.util.List<String> screenOwts = BusinessDashDao.getDash();
        if (StringUtils.isBlank(owt) && !screenOwts.isEmpty()) {
            //获取RD 默认的服务
            owt = ServiceCommon.owtByUser(UserUtils.getUser());
            if (StringUtil.isBlank(owt)) {
                owt = screenOwts.get(0);
            }
        }
        //获取默认owt
        model.addAttribute("owt", owt);
        model.addAttribute("owtList", screenOwts);
        return "monitor/businessDashConfig";
    }

    @Worth(model = Worth.Model.DataCenter, function = "服务业务指标")
    @RequestMapping(value = "business/dash/config", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getScreen(@RequestParam(value = "id") int id) {
        return JsonHelper.dataJson(AppScreenDao.dashDelete(id));
    }

    @Worth(model = Worth.Model.Monitor, function = "服务提供者报警配置")
    @RequestMapping(value = "provider/config", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public String providerConfig(@RequestParam(value = "appkey", required = false) String appkey, Model model) {
        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("appkey", appkey);
        model.addAttribute("apps", apps);
        if (CommonHelper.isOffline()) {
            Option<String> value = TairClient.get("offline_provider_trigger");
            String appkeys = value.isDefined() ? value.get() : "";
            if (appkeys.contains(appkey)) {
                model.addAttribute("isStartProviderMonitor", true);
            } else {
                model.addAttribute("isStartProviderMonitor", false);
            }

        }
        return "monitor/provider";
    }

    /**
     * provider 开关
     *
     * @param appkey
     * @param triggerSwitch
     * @return
     */
    @RequestMapping(value = "provider/trigger/switch", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String providerTriggerSwitch(@RequestParam(value = "appkey", required = true) String appkey, @RequestParam(value = "triggerSwitch", required = true) String triggerSwitch) {
        User u = UserUtils.getUser();
        if (AppkeyAuth.hasAuth(appkey, Auth.Level.ADMIN.getValue(), u)) {
            Option<String> value = TairClient.get("offline_provider_trigger");
            String appkeys = value.isDefined() ? value.get() : "";
            if (triggerSwitch.equals("on")) {
                if ("None".equals(appkeys)) {
                    TairClient.put("offline_provider_trigger", appkey);
                } else {
                    TairClient.put("offline_provider_trigger", dealString(appkeys, appkey, true));
                }
                return JsonHelper.dataJson("开启成功");
            } else {
                String v = dealString(appkeys, appkey, false);
                TairClient.put("offline_provider_trigger", v);
                return JsonHelper.dataJson("关闭成功");
            }
        } else {
            return JsonHelper.errorJson("权限不够");
        }
    }

    public String dealString(String appkeys, String doStr, boolean addorRemove) {
        String[] strs = appkeys.split(",");
        Set<String> appkeySet = new HashSet<String>();
        for (String s : strs) {
            appkeySet.add(s);
        }
        if (addorRemove) {
            appkeySet.add(doStr);
        } else {
            appkeySet.remove(doStr);
        }
        StringBuffer sb = new StringBuffer();
        if (!appkeySet.isEmpty()) {
            for (String s : appkeySet) {
                sb.append(s).append(",");
            }
            sb.substring(0, sb.lastIndexOf(","));
        } else {
            sb.append(",");
        }
        return sb.toString();
    }
}