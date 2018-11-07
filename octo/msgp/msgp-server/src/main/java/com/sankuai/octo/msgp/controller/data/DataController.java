package com.sankuai.octo.msgp.controller.data;

import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.msgp.common.config.db.msgp.Tables;
import com.sankuai.octo.msgp.dao.kpi.AppScreenDao;
import com.sankuai.octo.msgp.dao.perf.PerfDayDao;
import com.sankuai.octo.msgp.serivce.AppkeyAuth;
import com.sankuai.octo.msgp.service.portrait.GetMethodDegreeService;
import com.sankuai.octo.msgp.serivce.data.*;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.serivce.servicerep.ServiceKpiReport;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import scala.collection.immutable.List;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/data")
@Worth(model = Worth.Model.DataCenter)
@Auth(level = Auth.Level.LOGIN, responseMode = Auth.ResponseMode.JSON)
public class DataController {
    private static final Logger logger = LoggerFactory.getLogger(DataController.class);
    private static final String REDIRECT = "true";
    private static final FiniteDuration timeout = Duration.create(20000, TimeUnit.MILLISECONDS);
    private static String mtraceUrl = "http://mtrace.inf.sankuai.com/elasticSearch/getTraceIDByAnnotation";

    @Autowired
    private GetMethodDegreeService getMethodDegreeService;

    static {
        if (CommonHelper.isOffline()) {
            mtraceUrl = "http://mtrace.inf.dev.sankuai.com/elasticSearch/getTraceIDByAnnotation";
        }
    }


    /**
     * 从数据中心Hbase取历史KPI数据
     * 可以组合各种条件进行查询
     */
    @Worth(model = Worth.Model.DataCenter, function = "查询性能指标")
    @RequestMapping(value = "kpi", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getKpi(@RequestParam(value = "appkey") String appkey,
                         @RequestParam(value = "start") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                         @RequestParam(value = "end") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end,
                         @RequestParam(value = "protocolType", required = false) String protocolType,
                         @RequestParam(value = "role", required = false) String role,
                         @RequestParam(value = "dataType", required = false) String dataType,
                         @RequestParam(value = "env", required = false) String env,
                         @RequestParam(value = "unit", required = false) String unit,
                         @RequestParam(value = "group", required = false) String group,
                         @RequestParam(value = "spanname", required = false) String spanname,
                         @RequestParam(value = "localhost", required = false) String localhost,
                         @RequestParam(value = "remoteApp", required = false) String remoteAppkey,
                         @RequestParam(value = "remoteHost", required = false) String remoteHost,
                         @RequestParam(value = "dataSource", required = false) String dataSource,
                         @RequestParam(value = "merge", required = false, defaultValue = "false") Boolean merge) {
        int i_start = (int) (start.getTime() / 1000);
        int i_end = (int) (end.getTime() / 1000);
        return DataQuery.getHistoryStatisticMerged(appkey, i_start, i_end, protocolType, role, dataType, env, unit, group, spanname,
                localhost, remoteAppkey, remoteHost, dataSource, merge);
    }


    /**
     * @param appkey 如果appkey为空，则已当前用户从后台能访问到的appkey列表中选择第一个appkey作为默认的appkey.但这带有随机性。
     * @param model
     * @return 数据分析主tab页面
     */
    @RequestMapping(value = "tabNav", method = RequestMethod.GET)
    public String dataTabNav(@RequestParam(value = "type", required = false) String type,
                             @RequestParam(value = "appkey", required = true) String appkey,
                             @RequestParam(value = "env", required = false) String env,
                             @RequestParam(value = "start", required = false) String start,
                             @RequestParam(value = "end", required = false) String end,
                             @RequestParam(value = "day", required = false) String day,
                             @RequestParam(value = "hour", required = false) String hour,
                             @RequestParam(value = "timeUnit", required = false) String timeUnit,
                             @RequestParam(value = "spanname", required = false) String spanname,
                             @RequestParam(value = "remoteApp", required = false) String remoteApp,
                             @RequestParam(value = "remoteHost", required = false) String remoteHost,
                             @RequestParam(value = "idc", required = false) String idc,
                             @RequestParam(value = "idcLocalHosts", required = false, defaultValue = "all") String idcLocalHosts,
                             @RequestParam(value = "merge", required = false, defaultValue = "true") Boolean merge,
                             Model model) {


        java.util.List<String> startAndEnd = DataQuery.getStartAndEndByType(type, start, end);

        java.util.List<String> apps = ServiceCommon.appsByUser();
        model.addAttribute("apps", ServiceCommon.apps());
        model.addAttribute("start", startAndEnd.get(0));
        model.addAttribute("end", startAndEnd.get(1));
        model.addAttribute("env", env);
        model.addAttribute("day", day);
        model.addAttribute("hour", hour);
        model.addAttribute("timeUnit", timeUnit);
        model.addAttribute("spanname", spanname);
        model.addAttribute("remoteApp", remoteApp);
        model.addAttribute("remoteHost", remoteHost);
        model.addAttribute("idc", idc);
        model.addAttribute("idcLocalHosts", idcLocalHosts);
        model.addAttribute("merge", merge);
        model.addAttribute("mtraceUrl", mtraceUrl);
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("appkey", appkey);
        return "data/dataTabNavigation";
    }

    //数据分析-性能指标: 查询天粒度的性能数据
    @Worth(model = Worth.Model.DataCenter, function = "性能指标")
    @RequestMapping(value = "/performance", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getDailyPerformance(@RequestParam(value = "appkey") String appkey,
                                      @RequestParam(value = "day", required = false) String day,
                                      @RequestParam(value = "env") String env,
                                      @RequestParam(value = "source", required = false, defaultValue = "server") String source) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate date = (day == null ? LocalDate.now() : formatter.parseLocalDate(day));
        try {
            return JsonHelper.dataJson(DataQuery.getDailyPerformance(appkey, "", env, date.toDateTimeAtStartOfDay(), source));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    //数据分析-性能指标: appkey点击后详情页
    @RequestMapping(value = "/performance/span", method = RequestMethod.GET)
    public String getSpanPerformance(@RequestParam(value = "appkey", required = false) String appkey,
                                     @RequestParam(value = "spanname", required = false) String spanname,
                                     @RequestParam(value = "day", required = false) String day,
                                     @RequestParam(value = "hour", required = false) String hour,
                                     @RequestParam(value = "unit", required = false, defaultValue = "Hour") String unit,
                                     @RequestParam(value = "type", required = false, defaultValue = "1") Integer type,
                                     Model model) {
        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("appkey", appkey);
        model.addAttribute("apps", ServiceCommon.apps());
        model.addAttribute("spanname", spanname);
        if (StringUtil.isBlank(day)) {
            day = DateTimeUtil.format(new Date(), DateTimeUtil.DATE_DAY_FORMAT);
        }

        if (StringUtil.isBlank(hour)) {
            hour = DateTimeUtil.format(new Date(), "yyyy-MM-dd HH:mm");
        }
        model.addAttribute("day", day);
        model.addAttribute("hour", hour);
        model.addAttribute("unit", unit);
        model.addAttribute("type", type);
        model.addAttribute("mtraceUrl", mtraceUrl);

        if (!StringUtils.isEmpty(appkey)) {
            int start = (int) (new DateTime().minusDays(1).getMillis() / 1000);
            int end = (int) (new DateTime().getMillis() / 1000);
            String env = "prod";
            String source = "server";
            DataQuery.MetricsTags tags = DataQuery.tags(appkey, start, end, env, source);
            model.addAttribute("spannameList", tags.spannameJavaList());
        }
        return "data/data_performance_span";

    }

    //数据分析-性能指标: 查询客户端调用详情
    @RequestMapping(value = "/availability_details", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAvailabilityDetails(@RequestParam(value = "appkey") String appkey,
                                         @RequestParam(value = "spanname", required = false, defaultValue = "server") String spanname,
                                         @RequestParam(value = "env") String env,
                                         @RequestParam(value = "day", required = false) String day) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate date = (day == null ? LocalDate.now() : formatter.parseLocalDate(day));
        try {
            return JsonHelper.dataJson(DataQuery.getAvailabilityDetails(appkey, spanname, env, date.toDateTimeAtStartOfDay()));
        } catch (Exception e) {
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    @Worth(model = Worth.Model.DataCenter, function = "来源/去向分析")
    @RequestMapping(value = "appkey_spanname", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppRemoteAppkey(@RequestParam(value = "appkey", required = false) String appkey,
                                     @RequestParam(value = "env", required = false) String env,
                                     @RequestParam(value = "source", required = false) String source) {
        return JsonHelper.dataJson(DataQuery.getAppRemoteAppkey(appkey, env, source));
    }

    //数据分析-主机分析
    @Worth(model = Worth.Model.DataCenter, function = "主机分析")
    @RequestMapping(value = "host", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getHostData(@RequestParam(value = "appkey") String appkey,
                              @RequestParam(value = "start") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                              @RequestParam(value = "end") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end,
                              @RequestParam(value = "protocolType", required = false) String protocolType,
                              @RequestParam(value = "role", required = false) String role,
                              @RequestParam(value = "dataType", required = false) String dataType,
                              @RequestParam(value = "env", required = false) String env,
                              @RequestParam(value = "unit", required = false) String unit,
                              @RequestParam(value = "group", required = false) String group,
                              @RequestParam(value = "spanname", required = false) String spanname,
                              @RequestParam(value = "localhost", required = false) String localhost,
                              @RequestParam(value = "remoteApp", required = false) String remoteAppkey,
                              @RequestParam(value = "remoteHost", required = false) String remoteHost,
                              @RequestParam(value = "dataSource", required = false, defaultValue = "hbase") String dataSource,
                              @RequestParam(value = "idc", required = false) String idc,
                              @RequestParam(value = "idcLocalHosts", required = false, defaultValue = "all") String idcLocalHosts,
                              @RequestParam(value = "merge", required = false, defaultValue = "false") Boolean merge) {
        int i_start = (int) (start.getTime() / 1000);
        int i_end = (int) (end.getTime() / 1000);
        if (merge) {
            return JsonHelper.dataJson(DataQuery.getHistoryStatisticMergedByHost(appkey, i_start, i_end, protocolType, role, dataType, env, unit, group, spanname,
                    localhost, remoteAppkey, remoteHost, dataSource, idc, idcLocalHosts));
        } else {
            return JsonHelper.dataJson(DataQuery.getHistoryStatisticByHost(appkey, i_start, i_end, protocolType, role, dataType, env, unit, group, spanname,
                    localhost, remoteAppkey, remoteHost, dataSource, idc, idcLocalHosts));
        }

    }

    //数据分析-秒级指标
    @Worth(model = Worth.Model.DataCenter, function = "秒级指标")
    @RequestMapping(value = "second", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getSecondLevelData(@RequestParam(value = "appkey") String appkey,
                                     @RequestParam(value = "start") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                                     @RequestParam(value = "end") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end,
                                     @RequestParam(value = "env") String env,
                                     @RequestParam(value = "ip") String ip) {
        int startTime = (int) (start.getTime() / 1000);
        int endTime = (int) (end.getTime() / 1000);
        return JsonHelper.dataJson(DataQuery.getSecondLevelData(appkey, startTime, endTime, env, ip));
    }

    //数据分析-标签治理
    @Worth(model = Worth.Model.DataCenter, function = "标签治理")
    @RequestMapping(value = "tags_get_data", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getMethodDegree(@RequestParam(value = "appkey") String appkey
            , @RequestParam(value = "methods") java.util.List<String> methods) {
        return JsonHelper.dataJson(getMethodDegreeService.getMethodDegree(appkey, methods));
    }

    @RequestMapping(value = "tags_set_data", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String setMethodDegree(@RequestParam(value = "appkey") String appkey
            , @RequestParam(value = "method") String method
            , @RequestParam(value = "degree") String degree) {
        return JsonHelper.dataJson(getMethodDegreeService.setMethodDegree(appkey, method, degree));
    }

    //数据分析-上下游分析
    @Worth(model = Worth.Model.DataCenter, function = "上下游分析")
    @RequestMapping(value = "service/kpi", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String serviceKpi(
            @RequestParam(value = "appkey") String appkey
            , @RequestParam(value = "start") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:00") Date start
            , @RequestParam(value = "end") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:00") Date end
            , @RequestParam(value = "role", required = false) String role
            , @RequestParam(value = "group", required = false) String group
            , @RequestParam(value = "source", required = false) String source
            , @RequestParam(value = "remoteAppkey", required = false, defaultValue = "*") String remoteAppkey
    ) {
        return JsonHelper.dataJson(ServiceKpiReport.kpi(appkey, start, end, role, group, remoteAppkey));
    }

    @RequestMapping(value = "ip2host", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String ip2Host(@RequestParam(value = "ips", required = false) List<String> ips) {
        return JsonHelper.dataJson(DataQuery.ip2HostDesc(ips));
    }

    @RequestMapping(value = "api/daily", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String apiDaily(@RequestParam(value = "day", required = false) String day) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate date = (day == null ? LocalDate.now() : formatter.parseLocalDate(day));
        List<Tables.PerfDayRow> list = PerfDayDao.dailyKpi(date);
        return JsonHelper.dataJson(list);
    }


    @RequestMapping(value = "idc_host", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppLocalhost(
            @RequestParam(value = "appkey", required = false) String appkey,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end,
            @RequestParam(value = "env", required = false) String env) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:00");
        if (StringUtil.isBlank(start)) {
            start = DateTimeUtil.format(new Date(System.currentTimeMillis() - 63 * 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        if (StringUtil.isBlank(end)) {
            end = DateTimeUtil.format(new Date(System.currentTimeMillis() - 3 * 60 * 1000), "yyyy-MM-dd HH:mm:00");
        }
        int start_time = (int) (formatter.parseDateTime(start).getMillis() / 1000);
        int end_time = (int) (formatter.parseDateTime(end).getMillis() / 1000);
        return JsonHelper.dataJson(DataQuery.getAppLocalhost(appkey, env, "server", start_time, end_time));
    }

    @RequestMapping(value = "tags", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String tags(@RequestParam(value = "appkey") String appkey,
                       @RequestParam(value = "env") String env,
                       @RequestParam(value = "start", required = false) String start,
                       @RequestParam(value = "end", required = false) String end,
                       @RequestParam(value = "source") String source) {
        // 默认取过去两天tags的并集
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        int startTime = (int) (((StringUtils.isBlank(start) ? new DateTime() : formatter.parseDateTime(start))).minusDays(2).getMillis() / 1000);
        int endTime = (int) (((StringUtils.isBlank(end) ? new DateTime() : formatter.parseDateTime(end))).getMillis() / 1000);
        return JsonHelper.dataJson(DataQuery.getTagsAndHostname(appkey, startTime, endTime, env, source));
    }


    // 查询一个服务的 来源主机/去向主机
    @RequestMapping(value = "host_detail", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String hostDetails(@RequestParam(value = "appkey") String appkey,
                              @RequestParam(value = "start") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                              @RequestParam(value = "end") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end,
                              @RequestParam(value = "protocolType", required = false) String protocolType,
                              @RequestParam(value = "role", required = false) String role,
                              @RequestParam(value = "dataType", required = false) String dataType,
                              @RequestParam(value = "env", required = false) String env,
                              @RequestParam(value = "unit", required = false) String unit,
                              @RequestParam(value = "group", required = false) String group,
                              @RequestParam(value = "spanname", required = false) String spanname,
                              @RequestParam(value = "localhost", required = false) String localhost,
                              @RequestParam(value = "remoteApp", required = false) String remoteAppkey,
                              @RequestParam(value = "remoteHost", required = false) String remoteHost,
                              @RequestParam(value = "dataSource", required = false) String dataSource) {
        int i_start = (int) (start.getTime() / 1000);
        int i_end = (int) (end.getTime() / 1000);
        return DataQuery.getHostDetails(appkey, i_start, i_end, protocolType, role, dataType, env, unit, group, spanname,
                localhost, remoteAppkey, remoteHost, dataSource);
    }

    /**
     * 性能指标的日趋图
     * 目前通过tair 查询日趋数据
     */
    @Worth(model = Worth.Model.DataCenter, function = "性能指标日趋势")
    @RequestMapping(value = "daily_kpi", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getDailyKpi(@RequestParam(value = "appkey") String appkey,
                              @RequestParam(value = "spanname", required = false) String spanname,
                              @RequestParam(value = "start") String start,
                              @RequestParam(value = "env", required = false) String env,
                              @RequestParam(value = "source", required = false) String source
    ) {
        return JsonHelper.dataJson(DataQuery.getDailyKpiTrends(appkey, spanname, start, env, source));
    }

    /**
     * 数据总览中可用率趋势图
     */
    @Worth(model = Worth.Model.DataCenter, function = "数据总览可用率趋势")
    @RequestMapping(value = "dailyAvailability", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getDailyAvailability(@RequestParam(value = "appkey") String appkey,
                                       @RequestParam(value = "spanname", required = false) String spanname,
                                       @RequestParam(value = "env", required = false) String env,
                                       @RequestParam(value = "source", required = false) String source
    ) {
        return JsonHelper.dataJson(DataQuery.getDailyAvailability(appkey, spanname, env, source));
    }

    @RequestMapping(value = "utilization/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getUtilization(@RequestParam(value = "appkey") String appkey) {
        return ResourceQuery.getUtilizationRate(appkey);
    }

    /**
     * 获取 接口的 环比/同比 数据
     * type: 0:环比,1:同比 数据
     */
    @RequestMapping(value = "daily_compared", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getDailyComparedData(@RequestParam(value = "appkey") String appkey,
                                       @RequestParam(value = "type") int type,
                                       @RequestParam(value = "start") String start,
                                       @RequestParam(value = "end") String end,
                                       @RequestParam(value = "env") String env,
                                       @RequestParam(value = "unit", required = false) String unit,
                                       @RequestParam(value = "source") String source,
                                       @RequestParam(value = "group", required = false) String group,
                                       @RequestParam(value = "spanname", required = false) String spanname,
                                       @RequestParam(value = "localhost", required = false) String localhost,
                                       @RequestParam(value = "remoteApp", required = false) String remoteAppkey,
                                       @RequestParam(value = "remoteHost", required = false) String remoteHost) {
        return JsonHelper.dataJson(DataCompare.dailyCompare(appkey, start, end, env, unit, source, group, spanname, localhost, remoteAppkey, remoteHost, type));
    }

    @RequestMapping(value = "key/metric", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String keyMetric(@RequestParam(value = "appkey") String appkey) {
        return JsonHelper.dataJson(Dashboard.getKeyMetric(appkey));
    }

    @RequestMapping(value = "perf/metric", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String perfMetric(@RequestParam(value = "appkey") String appkey) {
        return JsonHelper.dataJson(Dashboard.getPerfMetric(appkey));
    }

    @RequestMapping(value = "all/alarm", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String falconAllAlarm(@RequestParam(value = "appkey") String appkey,
                                 @RequestParam(value = "endTime") Integer endTime) {
        return Dashboard.getAllAlarm(appkey, endTime);
    }


    @Worth(model = Worth.Model.DataCenter, function = "业务指标")
    @RequestMapping(value = "app/metrics", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppMetrics(@RequestParam(value = "appkey") String appkey,
                                @RequestParam(value = "id", required = false, defaultValue = "0") Long id) {
        return JsonHelper.dataJson(AppScreenDao.get(appkey, id));
    }

    @Worth(model = Worth.Model.DataCenter, function = "业务指标")
    @RequestMapping(value = "business/metric", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppMetric(@RequestParam(value = "id") int id,
                               @RequestParam(value = "start") int start,
                               @RequestParam(value = "end") int end) {
        try {
            return JsonHelper.dataJson(AppScreenCtr.getScreen(start, end, id));
        } catch (Exception e) {
            logger.error("getScreen get error appkey:{} id:{}", id, e);
            return JsonHelper.errorJson("get failed " + e);
        }
    }

    @Worth(model = Worth.Model.DataCenter, function = "业务指标")
    @RequestMapping(value = "business/metric", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String saveAppMetric(@RequestParam(value = "appkey") String appkey,
                                @RequestParam(value = "title") String title,
                                @RequestParam(value = "metric") String metric,
                                @RequestParam(value = "sampleMode") String sampleMode) {
        Tables.AppScreenRow data = AppScreenDao.wrapper(0, "appkey", appkey, "", "", title, metric, sampleMode, 0, new DateTime().getMillis());
        try {
            return AppScreenDao.wrapperInsertOrUpdate(data);
        } catch (Exception e) {
            logger.error("saveScreen insert error appkey:{} title:{} metric:{}", appkey, title, metric, e);
            return JsonHelper.errorJson("save failed " + e);
        }
    }

    @RequestMapping(value = "business/metric", method = RequestMethod.DELETE, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getScreen(@RequestParam(value = "id") int id) {
        return JsonHelper.dataJson(AppScreenDao.delete(id));
    }

    @RequestMapping(value = "business/metric", method = RequestMethod.PUT, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateScreen(@RequestParam(value = "id") Long id,
                               @RequestParam(value = "category", defaultValue = "appkey") String category,
                               @RequestParam(value = "appkey", defaultValue = "") String appkey,
                               @RequestParam(value = "title", defaultValue = "") String title,
                               @RequestParam(value = "endpoint", defaultValue = "") String endpoint,
                               @RequestParam(value = "serverNode", defaultValue = "") String serverNode,
                               @RequestParam(value = "metric", defaultValue = "") String metric,
                               @RequestParam(value = "sampleMode", defaultValue = "") String sampleMode,
                               @RequestParam(value = "auth") int auth) {
        Tables.AppScreenRow data = AppScreenDao.wrapper(id, category, appkey, endpoint, serverNode, title, metric, sampleMode, auth, new DateTime().getMillis());
        return JsonHelper.dataJson(AppScreenDao.insertOrUpdate(data));
    }

}