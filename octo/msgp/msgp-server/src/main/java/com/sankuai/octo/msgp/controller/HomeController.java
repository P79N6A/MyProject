package com.sankuai.octo.msgp.controller;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.ServiceModels;
import com.sankuai.msgp.common.service.org.OpsService;
import com.sankuai.msgp.common.utils.client.TairClient;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.dao.kpi.AppScreenDao;
import com.sankuai.octo.msgp.dao.kpi.BusinessDashDao;
import com.sankuai.octo.msgp.model.DashboardDomain;
import com.sankuai.octo.msgp.serivce.DashboardService;
import com.sankuai.octo.msgp.serivce.Setting;
import com.sankuai.octo.msgp.serivce.data.DaPan;
import com.sankuai.octo.msgp.serivce.data.Dashboard;
import com.sankuai.octo.msgp.serivce.data.ErrorQuery;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.serivce.service.ServiceFilter;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;


@Controller
@RequestMapping("/")
@Worth(model = Worth.Model.Monitor)
public class HomeController {

    @RequestMapping(method = RequestMethod.GET)
    public String home() {
        User user = UserUtils.getUser();
        DashboardDomain.Dashboard dashboard = Setting.getDashboard(user.getLogin());
        if (!StringUtils.isEmpty(dashboard.url()) && !dashboard.url().equalsIgnoreCase("/")) {
            return "redirect:" + dashboard.url();
        }
        return "redirect:/personal";
    }

    @RequestMapping(value = "dashboard/data", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String dashboardData(@RequestParam(value = "owt", required = true) String owt,
                                @RequestParam(value = "start", required = true) Long start,
                                @RequestParam(value = "end", required = true) Long end) {
        List<Map<String, Object>> data = DaPan.hystrixData(owt, start, end);
        return JsonHelper.dataJson(data);
    }

    @RequestMapping(value = "dashboard/overview", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String defaultDash() {
        DashboardDomain.Overview overview = DashboardService.getOverviewCache();
        return JsonHelper.dataJson(overview);
    }

    @RequestMapping(value = "dashboard/overview/idc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String fetchInstanceByIDC(@RequestParam(value = "idc", required = false) String idc,
                                     Page page) {
        return JsonHelper.dataJson(DashboardService.getInstanceByIDC(idc, page));
    }

    @RequestMapping(value = "dashboard/overview/status", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String fetchInstanceByStatus(@RequestParam(value = "status", required = false) int status,
                                        Page page) {
        return JsonHelper.dataJson(DashboardService.getInstanceByStatus(status, page));
    }

    @RequestMapping(value = "config/spaces/list", method = RequestMethod.GET)
    @ResponseBody
    public Object getSpaces() {
        return Arrays.asList("test", "msgp");
    }

    @RequestMapping(value = "api/monitor/alive")
    @ResponseBody
    public Map<String, Object> monitorAlive() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "ok");
        return result;
    }

    @Worth(model = Worth.Model.OTHER, function = "自定义导航")
    @RequestMapping(value = "/personal", method = RequestMethod.GET)
    public String personal(Model model) {
        com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
        ServiceModels.User simpleUser = new ServiceModels.User(user.getId(), user.getLogin(), user.getName());
        Boolean isOffline = CommonHelper.isOffline();
        String hostUrl = isOffline ? "http://octo.test.sankuai.com" : "http://octo.sankuai.com";
        List<String> appkeys = ServiceFilter.getFavoriteAppkeys(simpleUser);
        model.addAttribute("favorite", appkeys);
        model.addAttribute("hostUrl", hostUrl);
        return "dashboard/personal";

    }

    @RequestMapping(value = "/personal/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getFavoriteAppkey() {
        com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
        ServiceModels.User simpleUser = new ServiceModels.User(user.getId(), user.getLogin(), user.getName());

        List<String> appkeys = ServiceFilter.getFavoriteAppkeys(simpleUser);
        return JsonHelper.dataJson(appkeys);
    }

    @RequestMapping(value = "/personal/add", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String addFavoriteAppkey(@RequestParam(value = "appkeys", defaultValue = "") List<String> appkeys) {
        try {
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            ServiceModels.User simpleUser = new ServiceModels.User(user.getId(), user.getLogin(), user.getName());
            ServiceFilter.addFavoriteAppkeys(simpleUser, appkeys);
            return JsonHelper.dataJson("ok");
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @RequestMapping(value = "/personal/delete", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String delteFavoriteAppkey(@RequestParam(value = "appkey") String appkey) {
        try {
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            ServiceModels.User simpleUser = new ServiceModels.User(user.getId(), user.getLogin(), user.getName());
            return JsonHelper.dataJson(ServiceFilter.deleteFavoriteAppkeys(simpleUser, appkey));
        }catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }


    @RequestMapping(value = "/personal/tipack", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String personalTipack() {
        try {
            com.sankuai.meituan.auth.vo.User user = UserUtils.getUser();
            ServiceModels.User simpleUser = new ServiceModels.User(user.getId(), user.getLogin(), user.getName());
            TairClient.put("display_tip_"+user.getLogin(),1);
            return JsonHelper.dataJson("ok");
        }catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }


    @Worth(model = Worth.Model.Monitor, function = "数据库监控")
    @RequestMapping(value = "/database", method = RequestMethod.GET)
    public String database(@RequestParam(value = "start", required = false) String start,
                           @RequestParam(value = "end", required = false) String end, Model model) {
        if (StringUtil.isBlank(start)) {
            start = DateTimeUtil.format(new Date(System.currentTimeMillis() - 11 * 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        if (StringUtil.isBlank(end)) {
            end = DateTimeUtil.format(new Date(System.currentTimeMillis() - 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "dashboard/database";
    }

    @Worth(model = Worth.Model.Monitor, function = "缓存监控")
    @RequestMapping(value = "/tair", method = RequestMethod.GET)
    public String tair(@RequestParam(value = "start", required = false) String start,
                       @RequestParam(value = "end", required = false) String end, Model model) {
        if (StringUtil.isBlank(start)) {
            start = DateTimeUtil.format(new Date(System.currentTimeMillis() - 11 * 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        if (StringUtil.isBlank(end)) {
            end = DateTimeUtil.format(new Date(System.currentTimeMillis() - 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "dashboard/tair";
    }

    @Worth(model = Worth.Model.Monitor, function = "报错监控")
    @RequestMapping(value = "/error", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public String networkAlarm(@RequestParam(value = "start", required = false) String start,
                               @RequestParam(value = "end", required = false) String end, Model model) {

        if (StringUtil.isBlank(start)) {
            start = DateTimeUtil.format(new Date(System.currentTimeMillis() - 11 * 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        if (StringUtil.isBlank(end)) {
            end = DateTimeUtil.format(new Date(System.currentTimeMillis() - 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        return "dashboard/errorDash";
    }

    @Worth(model = Worth.Model.Monitor, function = "数据库监控")
    @RequestMapping(value = "/database/alarm", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String databaseAlarm(@RequestParam(value = "nodeSearch") String nodeSearch,
                                @RequestParam(value = "duration") Integer duration) {
        return JsonHelper.dataJson(Dashboard.queryAlarmCount(nodeSearch, duration));
    }


    @Worth(model = Worth.Model.Monitor, function = "报错监控")
    @RequestMapping(value = "/error/alarm", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String errorAlarm(@RequestParam(value = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:00") Date start,
                             @RequestParam(value = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:00") Date end,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @RequestParam(value = "sort") String sort) {

        int start_time = (int) (start.getTime() / 1000);
        int end_time = (int) (end.getTime() / 1000);
        return JsonHelper.dataJson(ErrorQuery.queryAlarm(start_time, end_time, sort, size));
    }

    @RequestMapping(value = "/error/alarm/refresh", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String errorAlarm(@RequestParam(value = "start") Integer start,
                             @RequestParam(value = "end") Integer end) {
        ErrorQuery.refreshErrorDashBoard(start, end);
        return JsonHelper.dataJson("ok");
    }

    @Worth(model = Worth.Model.Monitor, function = "业务大盘")
    @RequestMapping(value = "/business", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public String businessDash(@RequestParam(value = "start", required = false) String start,
                               @RequestParam(value = "end", required = false) String end,
                               @RequestParam(value = "appkey", required = false) String appkey,
                               @RequestParam(value = "owt", required = false) String owt,
                               Model model) {

        if (StringUtil.isBlank(start)) {
            start = DateTimeUtil.format(new Date(System.currentTimeMillis() - 4 * 60 * 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        if (StringUtil.isBlank(end)) {
            end = DateTimeUtil.format(new Date(System.currentTimeMillis() - 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        Date now = DateTimeUtil.parse(start, "yyyy-MM-dd HH:mm:00");
        DateTime datetime = new DateTime(now.getTime());
        DateTime weekDay = datetime.plusWeeks(-1).withDayOfWeek(1);
        String lastWeek = DateTimeUtil.format(weekDay.toDate(), DateTimeUtil.DATE_DAY_FORMAT);
        //获取当前登录人的业务线
        List<String> screenOwts = BusinessDashDao.getDash();
        if (StringUtils.isBlank(owt) && !screenOwts.isEmpty()) {
            //获取RD 默认的服务
            owt = ServiceCommon.owtByUser(UserUtils.getUser());
            if (StringUtil.isBlank(owt)) {
                owt = screenOwts.get(0);
            }
        }
        //获取默认owt
        model.addAttribute("date", lastWeek);
        model.addAttribute("owt", owt);
        model.addAttribute("owtList", screenOwts);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        return "dashboard/businessDash";
    }

    @Worth(model = Worth.Model.Monitor, function = "业务大盘")
    @RequestMapping(value = "/business/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String addBusinessMetric(@RequestParam(value = "category") String category,
                                    @RequestParam(value = "title", defaultValue = "") String title,
                                    @RequestParam(value = "owt") String owt,
                                    @RequestParam(value = "metricId", defaultValue = "0") Long metricId,
                                    @RequestParam(value = "endpoint", defaultValue = "") String endpoint,
                                    @RequestParam(value = "serverNode", defaultValue = "") String serverNode,
                                    @RequestParam(value = "metric", defaultValue = "") String metric,
                                    @RequestParam(value = "sampleMode", defaultValue = "") String sampleMode) {
        return JsonHelper.dataJson(BusinessDashDao.addMetricAndDash(0, category, title, owt, metricId,
                endpoint, serverNode, metric, sampleMode));
    }

    @Worth(model = Worth.Model.DataCenter, function = "业务大盘")
    @RequestMapping(value = "/business/metrics", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getOwtMetrics(@RequestParam(value = "owt") String owt,
                                @RequestParam(value = "metricId", defaultValue = "0") Long metricId) {
//        return api.dataJson(BusinessDashDao.get(owt, metricId));
        // 大盘暂时不支持权限
        return JsonHelper.dataJson(AppScreenDao.get(BusinessDashDao.get(owt, metricId)));
    }

    @Worth(model = Worth.Model.DataCenter, function = "业务大盘")
    @RequestMapping(value = "/business/metrics", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteOwtMetrics(@RequestParam(value = "owt") String owt,
                                   @RequestParam(value = "id") Long id) {
        return JsonHelper.dataJson(BusinessDashDao.delete(owt, id));
    }

    @RequestMapping(value = "dashboard/overview/refesh", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String fetchInstanceByIDC() {
        DashboardService.refreshCache();
        return JsonHelper.dataJson("ok");
    }
    /**
     * 刷新性能数据
     */
    @RequestMapping(value = "dashboard/refesh/perfday", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refreshPerfday(@RequestParam(value = "day") @DateTimeFormat(pattern = "yyyy-MM-dd") Date day) {
        DateTime dateTime = new DateTime(day.getTime());
        com.sankuai.octo.msgp.serivce.data.Kpi.syncPerfDay(dateTime);
        return JsonHelper.dataJson("ok");
    }

}