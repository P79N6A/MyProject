package com.sankuai.octo.msgp.controller;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.msgp.common.service.org.OpsService;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.serivce.servicerep.ServiceDailyReport;
import com.sankuai.octo.msgp.serivce.servicerep.ServiceReport;
import com.sankuai.octo.msgp.serivce.servicerep.ServiceWeeklyReport;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.octo.msgp.task.IdcTrafficTimerTask;
import com.sankuai.octo.msgp.utils.helper.ReportHelper;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * Created by zava on 16/3/10.
 */
@Controller
@RequestMapping("/repservice")
@Worth(model = Worth.Model.Report)
public class ReportController {

    private static final String LIMIT = "" + Integer.MAX_VALUE;


    /**
     * 提供数据 owt数据,和时间
     * 时间按照周的时间统计
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(@RequestParam(value = "owt", required = false) String owt, Model model) {
        LocalDate localDate = LocalDate.now();
        localDate = localDate.plusWeeks(-1).withDayOfWeek(1);
        String lastWeek = DateTimeUtil.format(localDate.toDate(), DateTimeUtil.DATE_DAY_FORMAT);
        //获取当前登录人的业务线
        List<String> list = OpsService.owtList();
        if (StringUtils.isBlank(owt) && !list.isEmpty()) {
            //获取RD 默认的服务
            owt = ServiceCommon.owtByUser(UserUtils.getUser());
            if (StringUtil.isBlank(owt)) {
                owt = list.get(0);
            }
        }
        //获取默认owt
        model.addAttribute("date", lastWeek);
        model.addAttribute("owt", owt);
        model.addAttribute("owtList", list);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        return "servicerep/reportTabNav";
    }

    /**
     * 获取一周的头
     */
    @RequestMapping(value = "week", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String week(@RequestParam(value = "day", required = false) String day) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getStrWeekDay(date, 6));
    }

    @Worth(model = Worth.Model.Report, function = "服务可用率")
    @RequestMapping(value = "availability", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String availability(
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getAvailability(owt, date, limit));
    }

    @Worth(model = Worth.Model.Report, function = "服务QPS")
    @RequestMapping(value = "qps", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String qps(
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getQps(owt, date, limit));
    }

    @RequestMapping(value = "qpstp", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String qpstp(
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getQpstp(owt, date, limit));
    }

    @Worth(model = Worth.Model.Report, function = "日均性能数据")
    @RequestMapping(value = "toptp", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String toptp(
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getToptp(owt, date, limit));
    }

    @Worth(model = Worth.Model.Report, function = "依赖服务")
    @RequestMapping(value = "server", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String server(
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getDepend(owt, false, date, limit));
    }

    @Worth(model = Worth.Model.Report, function = "被依赖服务")
    @RequestMapping(value = "client", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String client(
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getDepend(owt, true, date, limit));
    }

    @Worth(model = Worth.Model.Report, function = "错误日志")
    @RequestMapping(value = "error", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String error(
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getError(owt, date, limit));
    }

    @Worth(model = Worth.Model.Report, function = "机房流量分布")
    @RequestMapping(value = "idc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String idc(
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getIdc(owt, date, limit));
    }

    @RequestMapping(value = "calcIdc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String calcIdc(@RequestParam(value = "login") String login) {
        IdcTrafficTimerTask.calcIDCbyHand(System.currentTimeMillis());
        return JsonHelper.dataJson("ok");
    }

    @Worth(model = Worth.Model.Report, function = "QPS峰值均值")
    @RequestMapping(value = "qpspeak", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String qpspeak(
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit) {
        DateTime date = getDate(day);
        return JsonHelper.dataJson(ServiceReport.getQpspeak(owt, date, limit));
    }

    /**
     * 刷新业务周报
     *
     * @param day
     * @param job
     * @return
     */
    @RequestMapping(value = "refresh/week", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refresh(
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "job", required = false, defaultValue = "qps") String job) {
        DateTime date = getDate(day);
        ServiceReport.refresh(date, job);
        return JsonHelper.jsonStr("ok");
    }

    @RequestMapping(value = "refresh/daily", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refreshDaily(
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "owt", required = false) String owt,
            @RequestParam(value = "appkey", required = false) String appkey
    ) {
        java.sql.Date date = getDay(day);
        ServiceReport.refreshDaily(date, owt, appkey);
        return JsonHelper.jsonStr("ok");
    }


    @RequestMapping(value = "refresh/daily/recompute", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refreshNotComputed(
            @RequestParam(value = "day", required = false) String day,
            @RequestParam(value = "owt", required = false) String owt
    ) {
        java.sql.Date date = getDay(day);
        ServiceReport.refreshNotComputed(date, owt);
        return JsonHelper.jsonStr("ok");
    }


    @Worth(model = Worth.Model.Report, function = "查看服务日报")
    @RequestMapping(value = "daily", method = RequestMethod.GET)
    public String dailyReport(@RequestParam(value = "username", required = false) String username,
                              @RequestParam(value = "day", required = false) String day,
                              Model model) {
        if (StringUtil.isBlank(username)) {
            username = UserUtils.getUser().getLogin();
        }
        Boolean isOffline = CommonHelper.isOffline();
        String hostUrl = isOffline ? "http://octo.test.sankuai.com" : "http://octo.sankuai.com";
        java.sql.Date date = getDay(day);
        DateTime dateTime = new DateTime(date.getTime());
        //如果day为空,说明是默认进入服务列表的
        DateTime startDate = dateTime.withTimeAtStartOfDay();
        DateTime endDate = dateTime.withTimeAtStartOfDay().plusDays(1);

        String start = startDate.toString(DateTimeUtil.DATE_TIME_FORMAT);
        String end = endDate.toString(DateTimeUtil.DATE_TIME_FORMAT);
        String yesterday = StringUtil.isBlank(day) ? start : day;

        List appDailyList = ServiceDailyReport.getDailyReport(username, date);
        List nonstandardAppkey = ServiceDailyReport.getNonstandardAppkey(username);
        model.addAttribute("hostUrl", hostUrl);
        model.addAttribute("appDailyList", appDailyList);
        model.addAttribute("nonstandardAppkeyList", nonstandardAppkey);
        model.addAttribute("day", yesterday);
        model.addAttribute("username", username);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("ftl", true);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        return "servicerep/dailyReport";
    }

    @Worth(model = Worth.Model.Report, function = "查看服务日报")
    @RequestMapping(value = "daily/report", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String dailyReport(@RequestParam(value = "username", required = false) String username,
                              @RequestParam(value = "day", required = false) String day) {
        if (StringUtil.isBlank(username)) {
            username = UserUtils.getUser().getLogin();
        }
        java.sql.Date date = getDay(day);
        return JsonHelper.dataJson(ServiceDailyReport.getDailyReport(username, date));
    }


    @Worth(model = Worth.Model.Report, function = "服务日报统计")
    @RequestMapping(value = "daily/count", method = RequestMethod.GET, produces = "image/jpg")
    @ResponseBody
    public byte[] dailyCount(@RequestParam(value = "username", required = false) String username,
                             @RequestParam(value = "day", required = false) String day,
                             Model model) throws IOException {
        if (StringUtil.isBlank(username)) {
            username = UserUtils.getUser().getLogin();
        }
        java.sql.Date date = getDay(day);
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream("images/sample.jpg");
        try {
            //存储访问数据
            ServiceReport.readMail(username, date);
            return IOUtils.toByteArray(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }


    /**
     * @param username 获取 username的服务报告
     * @param mailname 发送给 mailname
     * @param day      往前移动一天
     * @return
     */
    @RequestMapping(value = "mail", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String mail(@RequestParam(value = "username") String username,
                       @RequestParam(value = "mailname", required = false) String mailname,
                       @RequestParam(value = "day", required = false) String day,
                       @RequestParam(value = "type", required = false, defaultValue = "0") int type) {
        ServiceReport.mail(username, mailname, getDay(day), type);
        return JsonHelper.jsonStr("ok");
    }

    /**
     * @param day
     * @return
     */
    @RequestMapping(value = "mail/all", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String mailall(@RequestParam(value = "day", required = false) String day) {
        ServiceReport.mailAll(getDay(day));
        return JsonHelper.jsonStr("ok");
    }

    /**
     * 主动分配任务
     *
     * @return
     */
    @RequestMapping(value = "leader", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String lead(@RequestParam(value = "day", required = false) String day) {
        ServiceReport.refreshDailyStatus(getDay(day));
        return JsonHelper.jsonStr("ok");
    }


    @Worth(model = Worth.Model.Report, function = "查看服务周报")
    @RequestMapping(value = "weekly", method = RequestMethod.GET)
    public String weekTend(@RequestParam(value = "username", required = false) String username,
                           @RequestParam(value = "day", required = false) String day,
                           Model model) {
        if (StringUtil.isBlank(username)) {
            username = UserUtils.getUser().getLogin();
        }
        Map<String, String> dateRange = ServiceWeeklyReport.getWeekRange(day);
        List<String> appkeyList = ServiceWeeklyReport.getAppkeyList(username);
        model.addAttribute("appkeyList", appkeyList);
        model.addAttribute("day", dateRange.get("current"));
        model.addAttribute("start", dateRange.get("start"));
        model.addAttribute("end", dateRange.get("end"));
        model.addAttribute("username", username);
        model.addAttribute("echart", true);
        model.addAttribute("customerServices", "OCTO技术支持(infocto)");
        return "servicerep/weeklyReport";
    }

    @RequestMapping(value = "weekly/data", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String serviceList(@RequestParam(value = "username", required = false) String username,
                              @RequestParam(value = "appkey", required = false) String appkey,
                              @RequestParam(value = "dataType", required = false) String dataType,
                              @RequestParam(value = "day", required = false) String day) {
        if (StringUtil.isBlank(username)) {
            username = UserUtils.getUser().getLogin();
        }
        return JsonHelper.dataJson(ServiceWeeklyReport.getWeeklyTrend(username, appkey, dataType, day));
    }

    @RequestMapping(value = "user/appkeys", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String userAppkes(@RequestParam(value = "username", required = false) String username) {
        if (StringUtil.isBlank(username)) {
            username = UserUtils.getUser().getLogin();
        }
        return JsonHelper.dataJson(ServiceReport.getUser(username));
    }


    @RequestMapping(value = "helper/refresh", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refreshUserAppkeyMap() {
        return JsonHelper.dataJson(ReportHelper.refreshUserAppkeyMapManually());
    }

    private java.sql.Date getDay(String day) {
        java.sql.Date date;
        if (StringUtil.isBlank(day)) {
            date = new java.sql.Date(System.currentTimeMillis() - 86400000);
        } else {
            date = new java.sql.Date(DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime());
        }
        return date;
    }


    private DateTime getDate(String day) {
        long time = System.currentTimeMillis();
        if (StringUtil.isNotBlank(day)) {
            time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime();
        }
        DateTime date = new DateTime(time);
        return date.withDayOfWeek(1);
    }

}
