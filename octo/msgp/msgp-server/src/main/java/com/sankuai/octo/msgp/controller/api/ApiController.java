package com.sankuai.octo.msgp.controller.api;

import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.meituan.common.json.JSONUtil;
import com.sankuai.meituan.common.time.TimeUtil;
import com.sankuai.meituan.common.util.ApiUtil;
import com.sankuai.msgp.common.config.db.msgp.Tables;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.service.org.BusinessOwtService;
import com.sankuai.msgp.common.service.org.OpsService;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.errorlog.dao.ErrorLogStatisticDao;
import com.sankuai.octo.msgp.dao.availability.AvailabilityDao;
import com.sankuai.octo.msgp.dao.kpi.AppScreenDao;
import com.sankuai.octo.msgp.dao.monitor.BusinessMonitorDAO;
import com.sankuai.octo.msgp.dao.realtime.RealtimeLogDao;
import com.sankuai.octo.msgp.domain.AppsKpiReq;
import com.sankuai.octo.msgp.domain.KpiReq;
import com.sankuai.octo.msgp.domain.ShutDownIPs;
import com.sankuai.octo.msgp.serivce.AppkeyAlias;
import com.sankuai.octo.msgp.serivce.component.AppConfigService;
import com.sankuai.octo.msgp.serivce.component.ComponentHelper;
import com.sankuai.octo.msgp.serivce.component.ComponentService;
import com.sankuai.octo.msgp.serivce.data.DataQuery;
import com.sankuai.octo.msgp.serivce.data.PublicQuery;
import com.sankuai.octo.msgp.serivce.manage.ScannerChecker;
import com.sankuai.octo.msgp.serivce.monitor.MonitorConfig;
import com.sankuai.octo.msgp.serivce.monitor.MonitorEvent;
import com.sankuai.octo.msgp.serivce.monitor.business.KpiMonitorModel;
import com.sankuai.octo.msgp.serivce.overload.OverloadDegrade;
import com.sankuai.octo.msgp.serivce.service.*;
import com.sankuai.octo.msgp.serivce.servicerep.ServiceReport;
import com.sankuai.octo.msgp.serivce.servicerep.ServiceReportApi;
import com.sankuai.octo.msgp.task.AvailabilityTask;
import com.sankuai.octo.msgp.task.ReportDailyTask;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.common.model.WorthEvent;
import com.sankuai.octo.mworth.service.mWorthEventService;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import com.sankuai.octo.statistic.helper.TimeProcessor;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import scala.collection.JavaConversions;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/api")
public class ApiController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);
    private static final FiniteDuration timeout = Duration.create(20000, TimeUnit.MILLISECONDS);
    private static final String LIMIT = Integer.MAX_VALUE + "";


    @RequestMapping(value = "scanner/report", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public void scannerReport(HttpServletRequest request) {
        try {
            String text = IOUtils.copyToString(request.getReader());
            ScannerChecker.report(text);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 统计zk下，指定环境，服务活跃的节点个数
     *
     * @param appkey
     * @param env
     * @return
     */
    @RequestMapping(value = "zk/service/alive", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String countAliveProviderNode(@RequestParam("appkey") String appkey, @RequestParam("env") int env) {
        try {
            int count = AppkeyProviderService.countAliveProviderNode(appkey, env);
            return "{\"count\":" + String.valueOf(count) + "}";
        } catch (Exception e) {
            LOG.error("查询服务节点失败,appkey:{},env:{}", appkey, env, e);
            return JsonHelper.errorDataJson("查询服务节点失败");
        }
    }


    @RequestMapping(value = "apps", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String apps(@RequestParam(value = "username", required = false) String username,
                       @RequestParam(value = "type", required = false) String type,
                       @RequestParam(value = "keyword", required = false) String keyword
    ) {
        try {
            return JsonHelper.dataJson(ServiceFilter.applist(username, type, keyword));
        } catch (Exception e) {
            LOG.error("获取服务失败，username" + username + "type" + type + "keyword" + keyword, e);
            return JsonHelper.errorDataJson(e.getMessage());
        }
    }

    @RequestMapping(value = "appsByCategory", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appsByCategory(@RequestParam(value = "username", required = true) String username,
                                 @RequestParam(value = "category", required = true) String category) {
        LOG.info("{} , get appkey by category", username);
        return JsonHelper.dataJson(ServiceFilter.serviceByCategory(category));
    }

    @RequestMapping(value = "getAppsByOwtPdl", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appsByOwtPdl(@RequestParam(value = "username", required = true) String username,
                               @RequestParam(value = "owt", required = true) String owt,
                               @RequestParam(value = "pdl", required = false) String pdl
    ) {
        try {
            LOG.info("获取owtpdl appkey 获取人：" + username);
            Page page = new Page();
            page.setPageSize(Integer.MAX_VALUE);
            return JsonHelper.dataJson(ServiceFilter.serviceAppkeyByOwtPdl(owt, pdl, page));
        } catch (Exception e) {
            LOG.error("获取服务失败，username" + username + "owt" + owt + "pdl" + pdl, e);
            return JsonHelper.errorDataJson(e.getMessage());
        }
    }


    @RequestMapping(value = "hosts/{appkey:.+}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String hosts(@PathVariable(value = "appkey") String appkey,
                        @RequestParam(value = "env", required = false, defaultValue = "3") Integer env,
                        @RequestParam(value = "status", required = false) Integer status) {
        try {
            return JsonHelper.dataJson(AppkeyProviderService.getProvider(appkey, env, status));
        } catch (Exception e) {
            LOG.error("获取主机失败，appkey" + appkey + "status" + status, e);
            return JsonHelper.errorDataJson(e.getMessage());
        }
    }


    /**
     * 获取指定协议，环境的appkey，host
     *
     * @param username
     * @param type
     * @param env
     * @return
     */
    @RequestMapping(value = "appkey/hosts", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appsprovider(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "env", required = false, defaultValue = "prod") String env
    ) {
        try {
            return JsonHelper.dataJson(AppkeyProviderService.appkeyHosts(username, type, env));
        } catch (Exception e) {
            LOG.error("获取服务主机，username:" + username + "，type:" + type + ",env:" + env, e);
            return JsonHelper.errorDataJson(e.getMessage());
        }
    }

    @Deprecated
    @RequestMapping(value = "appkeyhosts", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appkeyhosts() {
        try {
            return JsonHelper.dataJson(ServiceDesc.apphosts());
        } catch (Exception e) {
            LOG.error("获取所有主机", e);
            return JsonHelper.errorDataJson(e.getMessage());
        }
    }

    /* *
     * 手动删除ZK中的降级策略
     * */
    @RequestMapping(value = "overload/degradeActionDelete", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteDegrdeAction(@RequestParam(value = "appkey") String appkey,
                                     @RequestParam(value = "env") Integer env,
                                     @RequestParam(value = "method") String method) {
        try {
            OverloadDegrade.removeDegradeNode(env, appkey, method);
            return JsonHelper.dataJson(true);
        } catch (Exception e) {
            LOG.error("删除降级策略。appkey:" + appkey + ",env:" + env + ",method:" + method, e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /* *
     * 依据appkey/主机数 查询服务有效节点
     * */
    @RequestMapping(value = "zk/providerForOverload/aliveNode", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String countAliveProviderNode(@RequestParam("env") int env, @RequestParam("appkey") String appkey, @RequestParam("providerCountSwitch") int providerCountSwitch) {
        int count = ServiceQuota.countAliveProviderNode(appkey, env, providerCountSwitch);
        return "{\"count\":" + String.valueOf(count) + "}";
    }

    /**
     * @deprecated 废弃中
     * 替代api : api/service?appkey=com.sankuai.wmarch.d.es
     */
    @RequestMapping(value = "appkeyDesc/{appkey:.+}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String desc(@PathVariable("appkey") String appkey) {
        return JsonHelper.dataJson(ServiceCommon.apiDesc(appkey).toRich());
    }

    @RequestMapping(value = "appkeys/getByUserId/{userId}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppkeysByUserId(@PathVariable("userId") Long userId) {
        return JsonHelper.dataJson(ServiceCommon.getAppkeysByUserId(userId));
    }


    /**
     * 1：关闭 指定ip的机器，删除所有相关的服务下的服务节点
     * 2：删除失败需要周知用户
     */
    @RequestMapping(value = "hulk/provider/shutdown", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String hulkDelProviderByIP(@RequestBody ShutDownIPs shutDownIPs) {
        LOG.info("服务节点下线" + shutDownIPs.toString());
        if (shutDownIPs.getIps().isEmpty()) {
            return JsonHelper.errorJson("删除失败，ip列表为空");
        } else {
            return ServiceCommon.batchDelProviderByIPs(shutDownIPs);
        }
    }

    @RequestMapping(value = "flow", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String flow(@RequestParam(value = "appkey", required = false) String appkey,
                       @RequestParam(value = "start", required = false) String start,
                       @RequestParam(value = "end", required = false) String end,
                       @RequestParam(value = "role", required = false) String role,
                       @RequestParam(value = "rate", required = false) Double rate) {
        start = (StringUtil.isBlank(start) ? new LocalDate().toDateTimeAtStartOfDay().toString("yyyy-MM-dd HH:mm:ss") : start);
        end = (StringUtil.isBlank(end) ? new LocalDate().toDateTimeAtStartOfDay().plusHours(1).minusMinutes(1).toString("yyyy-MM-dd HH:mm:ss") : end);
        role = StringUtil.isBlank(role) ? "client" : role;
        rate = (rate == null ? 0.5f : rate);
        if (StringUtil.isBlank(appkey)) {
            return JsonHelper.dataJson(DataQuery.validateAll(start, end, role, rate));
        } else {
            return JsonHelper.dataJson(DataQuery.validateFlow(appkey, start, end, role, rate, 1));
        }
    }


    @RequestMapping(value = "/worth/save", method = RequestMethod.POST)
    @ResponseBody
    public String saveAction(HttpServletRequest request) {
        WorthEvent worthEvent = null;
        String result = "";
        try {
            worthEvent = ApiUtil.parse(request.getReader(), WorthEvent.class);
            mWorthEventService.put(worthEvent);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            LOG.info("save action failed {}", JSONUtil.toJSONString(worthEvent));
            return JsonHelper.errorJson(e.toString());
        }

        return JsonHelper.dataJson(result);
    }

    @RequestMapping(value = "perf", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getPerf(@RequestParam("appkey") String appkey,
                          @RequestParam("spanname") String spanname,
                          @RequestParam("time") int time) {
        return DataQuery.apiPerf(appkey, spanname, time);
    }

    @RequestMapping(value = "errorLog/count", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String errorLogCount(@RequestParam(value = "appkey") String appkey,
                                @RequestParam(value = "day", required = false) String day,
                                @RequestParam(value = "start", required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                                @RequestParam(value = "end", required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end) {
        Date startTime = null;
        Date endTime = null;
        if (!StringUtil.isBlank(day)) {
            startTime = TimeUtil.parse(day);
            endTime = new Date(startTime.getTime() + 3600000 * 24);
        } else if (null != start && null != end) {
            startTime = start;
            endTime = end;
        } else {
            DateTime time = new DateTime();
            endTime = new Date();
            startTime = time.withTimeAtStartOfDay().toDate();
        }

        appkey = AppkeyAlias.aliasAppkey(appkey);
        return JsonHelper.dataJson(ErrorLogStatisticDao.groupByFilterId(appkey, TimeProcessor.getMinuteStart((int) (startTime.getTime() / 1000)),
                TimeProcessor.getMinuteStart((int) (endTime.getTime() / 1000)), "All", "All"));

    }

    /**
     * 查询服务的天粒度数据
     *
     * @param appkey
     * @param day
     * @param env
     * @param source
     * @return
     */
    @RequestMapping(value = "app/kpi", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String kpi(@RequestParam(value = "appkey") String appkey,
                      @RequestParam(value = "day", required = false) String day,
                      @RequestParam(value = "env") String env,
                      @RequestParam(value = "source", required = false, defaultValue = "server") String source) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate date = (day == null ? LocalDate.now() : formatter.parseLocalDate(day));
        try {
            return JsonHelper.dataJson(DataQuery.getDailyStatisticFormatted(appkey, env, date.toDateTimeAtStartOfDay(), source));
        } catch (Exception e) {
            LOG.error("获取服务性能失败:" + appkey + ",env:" + env + ",day:" + day + ",source:" + source, e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }


    @RequestMapping(value = "app/host/kpi", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String hostkpi(@RequestParam(value = "appkey") String appkey,
                          @RequestParam(value = "start") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                          @RequestParam(value = "end") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end,
                          @RequestParam(value = "env", required = false) String env,
                          @RequestParam(value = "unit", required = false) String unit,
                          @RequestParam(value = "localhost", required = false) String localhost
    ) {
        int i_start = (int) (start.getTime() / 1000);
        int i_end = (int) (end.getTime() / 1000);
        return JsonHelper.dataJson(DataQuery.getDataRecord(appkey, i_start, i_end, "", "server", "", env, unit,
                "spanLocalhost", "*", localhost, "", "",
                "hbase").get());
    }

    @RequestMapping(value = "app/kpi2", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String kpi2(@RequestParam(value = "appkey") String appkey,
                       @RequestParam(value = "remoteAppkey", required = false, defaultValue = "") String remoteAppkey,
                       @RequestParam(value = "spanname", required = false, defaultValue = "") String spanname,
                       @RequestParam(value = "start") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                       @RequestParam(value = "end") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end,
                       @RequestParam(value = "group", required = false, defaultValue = "spanLocalhost") String group,
                       @RequestParam(value = "role", required = false, defaultValue = "server") String role,
                       @RequestParam(value = "env", required = false) String env,
                       @RequestParam(value = "unit", required = false) String unit,
                       @RequestParam(value = "localhost", required = false) String localhost
    ) {
        int i_start = (int) (start.getTime() / 1000);
        int i_end = (int) (end.getTime() / 1000);
        return JsonHelper.dataJson(DataQuery.getDataRecord(appkey, i_start, i_end, "", role, "", env, unit,
                group, spanname, localhost, remoteAppkey, "", "hbase").get());
    }

    /**
     * 获取主机统计数据
     */
    @RequestMapping(value = "apps/host_count", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String hostCount(@RequestParam(value = "appkey", required = true) String appkey,
                            @RequestParam(value = "env", required = false) String env,
                            @RequestParam(value = "ts", required = true) long ts) {
        return JsonHelper.dataJson(PublicQuery.getHostCount(appkey, env, ts));
    }

    /**
     * 获取多个服务的性能数据
     */
    @RequestMapping(value = "apps/kpi", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appskpi(@RequestBody AppsKpiReq appsKpiReq) {
        return JsonHelper.dataJson(PublicQuery.appskpi(appsKpiReq));
    }

    /**
     * 获取去向的性能数据
     */
    @RequestMapping(value = "apps/dest", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appsDest(@RequestBody KpiReq kpiReq) {
        return JsonHelper.dataJson(PublicQuery.getDestinationData(kpiReq));
    }


    /**
     * 获取服务或者接口的qps
     */
    @RequestMapping(value = "app/qps", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appQPS(@RequestParam(value = "appkey", required = true, defaultValue = "") String appkey,
                         @RequestParam(value = "spanname", required = false) String spanname,
                         @RequestParam(value = "env", required = false) String env,
                         @RequestParam(value = "start", required = false) long start,
                         @RequestParam(value = "end", required = false) long end,
                         @RequestParam(value = "idc", required = false, defaultValue = "") List<String> idc) {
        try {
            return JsonHelper.dataJson(PublicQuery.getQPS(appkey, spanname, env, start, end, idc));
        } catch (Exception e) {
            LOG.error("获取服务qps失败", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 获取服务或者接口的平均qps
     */
    @RequestMapping(value = "app/avg_qps", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String appAverageQPS(@RequestParam(value = "appkey", required = true, defaultValue = "") String appkey,
                                @RequestParam(value = "spanname", required = false) String spanname,
                                @RequestParam(value = "env", required = false) String env,
                                @RequestParam(value = "ts", required = false) long ts,
                                @RequestParam(value = "days", required = false) int days) {
        try {
            return JsonHelper.dataJson(PublicQuery.getAverageQps(appkey, spanname, env, ts, days));
        } catch (Exception e) {
            LOG.error("获取服务avg_qps失败", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 获取服务日性能数据
     */
    @RequestMapping(value = "app/dailyData", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getDailyData(@RequestParam(value = "appkey", required = true, defaultValue = "") String appkey,
                               @RequestParam(value = "env", required = false) String env,
                               @RequestParam(value = "ts", required = false) long ts) {
        return PublicQuery.getDailyData(appkey, env, ts, "sever");
    }

    /**
     * 获取所有服务appkey
     */
    @RequestMapping(value = "app/allService", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAllService() {
        return JsonHelper.dataJson(ServiceCommon.listService());
    }


    /**
     * 周粒性能 报告信息
     */
    @RequestMapping(value = "report/kpi", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String reportKpi(@RequestParam(value = "appkey") String appkey,
                            @RequestParam(value = "day", required = false) String day,
                            @RequestParam(value = "unit", defaultValue = "week") String unit
    ) {
        try {
            return JsonHelper.dataJson(ServiceReportApi.kpi(appkey, day, unit));
        } catch (Exception e) {
            LOG.error("获取服务周报失败", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 周粒度 机房 报告信息
     */
    @RequestMapping(value = "report/idc", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String reportIdc(@RequestParam(value = "appkey") String appkey,
                            @RequestParam(value = "day", required = false) String day) {
        DateTime date = getDate(day);
        try {
            return JsonHelper.dataJson(ServiceReportApi.idc(appkey, date));
        } catch (Exception e) {
            LOG.error("获取服务周报idc信息失败", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }

    /**
     * 周粒性能 owt维度报告信息
     */
    @RequestMapping(value = "report/owt/kpi", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String reportOwtKpi(@RequestParam(value = "owt") String owt,
                               @RequestParam(value = "day", required = false) String day,
                               @RequestParam(value = "limit", required = false, defaultValue = LIMIT) Integer limit
    ) {
        try {
            return JsonHelper.dataJson(ServiceReportApi.getWeeklyKpi(owt, day, limit));
        } catch (Exception e) {
            LOG.error("获取服务周报owt报告失败", e);
            return JsonHelper.errorJson(e.getMessage());
        }
    }


    private DateTime getDate(String day) {
        long time = System.currentTimeMillis();
        if (StringUtil.isNotBlank(day)) {
            time = DateTimeUtil.parse(day, DateTimeUtil.DATE_DAY_FORMAT).getTime();
        }
        DateTime date = new DateTime(time);
        return date.withDayOfWeek(1);
    }

    /**
     * @param appkey  服务标识
     * @param logPath 实时日志路径
     * @return 成功 or 失败
     */
    @RequestMapping(value = "realtime/update_path", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public
    @ResponseBody
    String updateLogPath(@RequestParam String appkey, @RequestParam String logPath) {
        try {
            RealtimeLogDao.RealtimeLogDomain domain = new RealtimeLogDao.RealtimeLogDomain(appkey, logPath, System.currentTimeMillis());
            RealtimeLogDao.insert(domain);
            LOG.info("appkey:{},logPath:{}", appkey, logPath);
            return JsonHelper.jsonStr("success");
        } catch (Exception e) {
            LOG.error("updateLogPath fail", e);
            return JsonHelper.jsonStr("failure");
        }

    }

    @RequestMapping(value = "business/kpi/metrics", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppKpiMetric() {
        return JsonHelper.dataJson(AppScreenDao.getKpiMetrics());
    }

    @RequestMapping(value = "business/normal/metrics", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAppNotKpiMetric() {
        return JsonHelper.dataJson(AppScreenDao.getNotKpiMetrics());
    }

    @RequestMapping(value = "business/kpi/base/sync", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String syncKpiBase() {
        KpiMonitorModel.syncBase();
        return JsonHelper.dataJson("ok");
    }

    //获取所有的调用方
    @RequestMapping(value = "app/tags", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getTags(@RequestParam(value = "appkey") String appkey,
                          @RequestParam(value = "env") String env,
                          @RequestParam(value = "source") String source) {
        int startTime = (int) (System.currentTimeMillis() / 1000);
        int endTime = (int) (System.currentTimeMillis() / 1000);
        return JsonHelper.dataJson(DataQuery.getTagsAndHostname(appkey, startTime, endTime, env, source));
    }

    //获取所有的调用方
    @RequestMapping(value = "app/monitor", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String monitor(@RequestParam(value = "appkey") String appkey,
                          @RequestParam(value = "start") String start,
                          @RequestParam(value = "end") String end,
                          Page page
    ) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Long startTime = (start == null ? new DateTime().minusDays(7) : formatter.parseDateTime(start)).getMillis();
        Long endTime = (end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis();
        scala.collection.immutable.List<Tables.EventRow> result = MonitorEvent.getEvents(appkey, startTime, endTime, page);
        return JsonHelper.dataJson(result, page);
    }

    @RequestMapping(value = "apps/availabilities", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String availabilities(@RequestParam(value = "appkeys") List<String> appkeys,
                                 @RequestParam(value = "day") String day) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dayTime = formatter.parseDateTime(day);
        return JsonHelper.dataJson(AvailabilityDao.fetchAvailability(appkeys, dayTime));
    }

    @RequestMapping(value = "apps/availability/refresh_all", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refreshAllAvailabilities(@RequestParam(value = "day") String day) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dayTime = formatter.parseDateTime(day);
        AvailabilityTask.insertAllAppkeyAvailability(dayTime);
        return JsonHelper.dataJson("ok");
    }

    @RequestMapping(value = "apps/availability/refresh_single", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refreshSingleAvailabilities(@RequestParam(value = "appkey") String appkey,
                                              @RequestParam(value = "day") String day) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dayTime = formatter.parseDateTime(day);
        AvailabilityTask.insertSingleAvailability(appkey, dayTime);
        return JsonHelper.dataJson("ok");
    }

    @RequestMapping(value = "apps/availability/fill", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String refreshAllAvailabilities(@RequestParam(value = "ts") Long ts) {
        ReportDailyTask.fillAvailability(ts);
        return JsonHelper.dataJson("ok");
    }


    @RequestMapping(value = "business/kpi/manual/monitor", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String manualMonitor() {
        BusinessMonitorDAO.syncBusinessMonitor();
        return JsonHelper.dataJson("ok");
    }

    @RequestMapping(value = "consumer/unknown_service", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String unknownService(@RequestParam(value = "appkey", required = false) String appkey,
                                 @RequestParam(value = "env", required = false) String env,
                                 @RequestParam(value = "range", required = false) String range,
                                 @RequestParam(value = "remoteHost", required = false) String remoteHost)

    {
        return JsonHelper.dataJson(ServiceConsumer.getUnknownService(appkey, range, env, remoteHost));
    }


    @RequestMapping(value = "provider/node_type", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getNodeTypeByIp(@RequestParam(value = "ip", required = false) String ip) {
        return JsonHelper.dataJson(AppkeyProviderService.getNodeTypeByIp(ip));
    }


    @RequestMapping(value = "test/error", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String testError() {
        try {
            throw new Exception("Exception get exception name");
        } catch (Exception e) {
            LOG.error("test error", e);
        }
        return "ok";
    }

    @RequestMapping(value = "/component/dependency/upload", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "组件依赖上报")
    @ResponseBody
    public String updateDependency(@RequestBody String text) {
        return JsonHelper.dataJson(ComponentHelper.uploadDependency(text));
    }

    @RequestMapping(value = "/component/bom/upload", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "Bom依赖上报")
    @ResponseBody
    public String updateBomInfo(@RequestBody String text) {
        return JsonHelper.dataJson(ComponentHelper.uploadBomInformation(text));
    }

    @RequestMapping(value = "/component/broken/upload", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "Broken组件上报")
    @ResponseBody
    public String updateBrokenInfo(@RequestBody String text) {
        LOG.info("broken artifacts: " + text);
        return JsonHelper.dataJson(ComponentHelper.uploadBrokenInformation(text));
    }

    @RequestMapping(value = "/component/config", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @Worth(model = Worth.Model.Component, function = "获取组件检查配置")
    @ResponseBody
    public String getAppConfig(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl") String pdl,
            @RequestParam(value = "app") String app,
            @RequestParam(value = "appkey") String appkey
    ) {
        return JsonHelper.dataJson(AppConfigService.getBlackListConfig(base, owt, pdl, app));
    }

    @RequestMapping(value = "/component/version_count", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getVersionCount(
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId,
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl") String pdl
    ) {
        return JsonHelper.dataJson(ComponentService.getComponentVersionCount(groupId, artifactId, base, business, owt, pdl));
    }

    @RequestMapping(value = "/component/version_detail", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getVersionDetail(
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId,
            @RequestParam(value = "version") String version,
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl", required = false) String pdl,
            Page page
    ) {
        return JsonHelper.dataJson(ComponentService.getComponentVersionDetails(groupId, artifactId, version, base, business, owt, pdl, page), page);
    }

    @RequestMapping(value = "/component/coverage", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getComponentCoverage(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business,
            @RequestParam(value = "owt") String owt,
            @RequestParam(value = "pdl", required = false) String pdl,
            @RequestParam(value = "groupId") String groupId,
            @RequestParam(value = "artifactId") String artifactId,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "matching_type", required = false) int matchingType,
            Page page
    ) {
        return JsonHelper.dataJson(ComponentService.getComponentCoverage(base, business, owt, pdl, groupId, artifactId, version, matchingType, page), page);
    }

    @RequestMapping(value = "/component/details/appkey", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getDetailsByAppkey(
            @RequestParam(value = "appkey") String appkey) {
        return JsonHelper.dataJson(ComponentService.getDetailsByAppkey(appkey));
    }

    @RequestMapping(value = "/org/owt/findByUsername", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getOwtListByUsername(
            @RequestParam(value = "username") String username
    ) {
        return JsonHelper.dataJson(OpsService.getOwtsbyUsername(username));
    }

    @RequestMapping(value = "/org/business/findByOwt", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getBusinessByOwt(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "owt") String owt
    ) {
        return JsonHelper.dataJson(BusinessOwtService.getBusiness(base, owt));
    }

    @RequestMapping(value = "/org/owt/findByBusiness", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getAllOwtByBusiness(
            @RequestParam(value = "base") String base,
            @RequestParam(value = "business") String business
    ) {
        return JsonHelper.dataJson(BusinessOwtService.getOwtList(base, business));
    }

    @RequestMapping(value = "ip2hostname", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String ip2host(@RequestParam(value = "ip", required = false) String ip) {
        return JsonHelper.dataJson(OpsService.ipToHost(ip));
    }

    @RequestMapping(value = "/monitor/unconfigured", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getUnconfiguredSpans(
            @RequestParam(value = "appkey") String appkey,
            @RequestParam(value = "env") String env
    ) {
        try {
            return JsonHelper.dataJson(MonitorConfig.getUnconfiguredSpans(appkey, env));
        } catch (Exception e) {
            return JsonHelper.errorJson("get unconfigured spans failed.");
        }
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
}



