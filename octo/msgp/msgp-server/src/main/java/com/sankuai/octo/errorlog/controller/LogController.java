package com.sankuai.octo.errorlog.controller;

import com.alibaba.fastjson.JSONObject;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.borp.vo.ActionType;
import com.sankuai.meituan.common.time.TimeUtil;
import com.sankuai.msgp.common.model.EntityType;
import com.sankuai.msgp.common.model.Page;
import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.JsonUtil;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.client.BorpClient;
import com.sankuai.msgp.common.utils.client.EsClient;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.errorlog.dao.ErrorLogStatisticDao;
import com.sankuai.octo.errorlog.model.ErrorLogFilter;
import com.sankuai.octo.errorlog.model.LogAlarmConfiguration;
import com.sankuai.octo.errorlog.service.CellAppkeyService;
import com.sankuai.octo.errorlog.service.ErrorLogFilterService;
import com.sankuai.octo.errorlog.service.LogService;
import com.sankuai.octo.msgp.serivce.AppkeyAlias;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.msgp.utils.Result;
import com.sankuai.octo.msgp.utils.ResultData;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.util.DateTimeUtil;
import com.sankuai.octo.statistic.helper.TimeProcessor;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = "log")
@Worth(model = Worth.Model.ErrorLog)
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    @Resource
    private LogService logService;
    @Resource
    private ErrorLogFilterService errorLogFilterService;
    @Resource
    private CellAppkeyService cellAppkeyService;

    @Worth(model = Worth.Model.ErrorLog, function = "查看异常概要")
    @RequestMapping(value = "report")
    public String report(@RequestParam(value = "appkey", required = false) String appkey,
                         @RequestParam(value = "start", required = false) String start,
                         @RequestParam(value = "end", required = false) String end,
                         @RequestParam(value = "hostSet", defaultValue = "All") String hostSet,
                         @RequestParam(value = "host", defaultValue = "All") String host,
                         @RequestParam(value = "filterId", required = false) Integer filterId,
                         @RequestParam(value = "exceptionName", required = false) String exceptionName,
                         @RequestParam(value = "message", required = false) String message,
                         Model model) {
        Date endTime = new Date();
        Date startTime = new Date(endTime.getTime() - 3600000);
        if (!StringUtil.isBlank(start) && !StringUtil.isBlank(end)) {
            startTime = TimeUtil.parse(start);
            endTime = TimeUtil.parse(end);
        }

        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("apps", ServiceCommon.apps());
        model.addAttribute("appkey", appkey);
        model.addAttribute("filterId", filterId);
        model.addAttribute("start", DateTimeUtil.format(startTime, "yyyy-MM-dd HH:mm:ss"));
        model.addAttribute("end", DateTimeUtil.format(endTime, "yyyy-MM-dd HH:mm:ss"));
        model.addAttribute("hostSet", hostSet);
        model.addAttribute("host", host);
        model.addAttribute("exceptionName", exceptionName);
        model.addAttribute("message", message);
        model.addAttribute("isSetService", String.valueOf(cellAppkeyService.getCellAppkeys().contains(appkey)));

        return "log/report";
    }

    @Worth(model = Worth.Model.ErrorLog, function = "查看异常总量")
    @RequestMapping(value = "report/count", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String report(String appkey, Long start, Long end) {
        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        appkey = AppkeyAlias.aliasAppkey(appkey);
        int total = logService.getErrorCount(appkey, new Date(start), new Date(end));
        logger.info("get report  appkey={}, start={},end={}, total={}",
                appkey, start, end, total);

        return JsonHelper.dataJson(total);
    }

    @Worth(model = Worth.Model.ErrorLog, function = "查看异常趋势")
    @RequestMapping(value = "trend")
    public String trend(String appkey, String startTime, String stopTime, Integer periodType, Model model) {
        Date start = TimeUtil.dayStart(-13);
        Date end = TimeUtil.dayStart(-1);
        if (!StringUtil.isBlank(startTime)) {
            start = TimeUtil.parse(startTime);
        }
        if (!StringUtil.isBlank(stopTime)) {
            end = TimeUtil.parse(stopTime);
        }

        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("apps", ServiceCommon.apps());
        model.addAttribute("appkey", appkey);
        model.addAttribute("startTime", StringUtil.isBlank(startTime) ? TimeUtil.format(TimeUtil.DAY_FORMAT, TimeUtil.unixtime(start)) : startTime);
        model.addAttribute("stopTime", StringUtil.isBlank(stopTime) ? TimeUtil.format(TimeUtil.DAY_FORMAT, TimeUtil.unixtime(end)) : stopTime);
        model.addAttribute("periodType", periodType);

        // use alias appkey
        appkey = AppkeyAlias.aliasAppkey(appkey);
        model.addAttribute("logMap", logService.getErrorLogTrend(start, end, periodType, appkey));
        return "log/trend";
    }

    @Worth(model = Worth.Model.ErrorLog, function = "查看异常详情")
    @RequestMapping(value = "detail")
    public String detail(String uniqueKey, Model model) {
        if (!StringUtil.isBlank(uniqueKey)) {
            model.addAttribute("uniqueKey", uniqueKey);
            ParsedLog log = null;
            try {
                log = logService.getLogFromES(uniqueKey);
                if (log != null) {
                    model.addAttribute("isSetService", String.valueOf(cellAppkeyService.getCellAppkeys().contains(log.getAppkey())));
                    model.addAttribute("log", log);
                }
            } catch (Exception e) {
                logger.warn("cannot get log from ES ", e);
            }
        }
        return "log/detail";
    }

    @Worth(model = Worth.Model.ErrorLog, function = "查看监控配置")
    @RequestMapping(value = "configuration/list", method = RequestMethod.GET)
    public String getConfiguration(String appkey, Model model) {
        java.util.List<String> apps = ServiceCommon.apps();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("apps", apps);
        model.addAttribute("appkey", appkey);

        // use alias appkey
        appkey = AppkeyAlias.aliasAppkey(appkey);
        model.addAttribute("configuration", logService.getLogAlarmConfiguration(appkey));
        return "log/configuration/list";
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.ErrorLog, function = "添加监控配置")
    @RequestMapping(value = "configuration/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String addConfiguration(LogAlarmConfiguration configuration, Model model) {
        if (configuration == null || configuration.getBasicConfig() == null) {
            return JsonHelper.errorJson("配置信息为空");
        }
        // octo appkey
        String octoAppkey = configuration.getBasicConfig().getAppkey();
        configuration.getBasicConfig().setAlarmVirtualNode(octoAppkey);
        // use alias appkey
        configuration.getBasicConfig().setAppkey(AppkeyAlias.aliasAppkey(octoAppkey));
        Result result = logService.updateLogAlarmConfigWithOpsCheck(configuration, true);
        if (result.getIsSuccess()) {
            BorpClient.saveOpt(UserUtils.getUser(), ActionType.INSERT.getIndex(), octoAppkey,
                    EntityType.errorLogAddMonitor(), "default", "", "");
            return JsonHelper.dataJson("ok");
        } else {
            return JsonHelper.errorJson(result.getMsg());
        }
    }

    @Worth(model = Worth.Model.ErrorLog, function = "启动监控报警")
    @RequestMapping(value = "startAlarm", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String startAlarm(LogAlarmConfiguration configuration, Model model) {
        if (configuration == null || configuration.getBasicConfig() == null ||
                StringUtils.isBlank(configuration.getBasicConfig().getAppkey())) {
            model.addAttribute("configuration", configuration);
            return "/log/configuration/list";
        }
        String appkey = configuration.getBasicConfig().getAppkey();
        // 需获取Octo Appkey
        String octoAppkey = AppkeyAlias.octoAppkey(appkey);
        if (!logService.hasAuth(octoAppkey)) {
            return JsonHelper.errorJson("您对此服务没有操作权限");
        }

        configuration.getBasicConfig().setAlarmVirtualNode(octoAppkey);
        Result result = logService.updateLogAlarmConfigWithOpsCheck(configuration, true);
        if (result.getIsSuccess()) {
            int affectedLine = logService.updateStartAlarmTask(appkey);
            if (affectedLine < 1) {
                return JsonHelper.errorJson("变更Alarm任务状态失败");
            }
            BorpClient.saveOpt(UserUtils.getUser(), ActionType.UPDATE.getIndex(), octoAppkey,
                    EntityType.errorLogStartAlarm(), "default", "", "");
            return JsonHelper.dataJson("ok");
        } else {
            return JsonHelper.errorJson(result.getMsg());
        }
    }

    @Worth(model = Worth.Model.ErrorLog, function = "停止监控报警")
    @RequestMapping(value = "stopAlarm", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String stopAlarm(LogAlarmConfiguration configuration, Model model) {
        if (configuration == null || configuration.getBasicConfig() == null ||
                StringUtils.isBlank(configuration.getBasicConfig().getAppkey())) {
            model.addAttribute("configuration", configuration);
            return "/log/configuration/list";
        }
        String appkey = configuration.getBasicConfig().getAppkey();
        // 需获取Octo Appkey
        String octoAppkey = AppkeyAlias.octoAppkey(appkey);
        if (!logService.hasAuth(octoAppkey)) {
            return JsonHelper.errorJson("您对此服务没有操作权限");
        }

        configuration.getBasicConfig().setAlarmVirtualNode(octoAppkey);
        logService.updateLogAlarmConfig(configuration);
        int affectedLine = logService.updateStopAlarmTask(appkey);
        if (affectedLine < 1) {
            return JsonHelper.errorJson("变更Alarm任务状态失败");
        }
        BorpClient.saveOpt(UserUtils.getUser(), ActionType.UPDATE.getIndex(), octoAppkey,
                EntityType.errorLogStopAlarm(), "default", "", "");
        return JsonHelper.dataJson("ok");
    }

    @Worth(model = Worth.Model.ErrorLog, function = "重启监控报警")
    @RequestMapping(value = "restartAlarm", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String restartAlarm(LogAlarmConfiguration configuration, Model model) {
        if (configuration == null || configuration.getBasicConfig() == null ||
                StringUtils.isBlank(configuration.getBasicConfig().getAppkey())) {
            model.addAttribute("configuration", configuration);
            return "/log/configuration/list";
        }
        String appkey = configuration.getBasicConfig().getAppkey();
        // 需获取Octo Appkey
        String octoAppkey = AppkeyAlias.octoAppkey(appkey);
        if (!logService.hasAuth(octoAppkey)) {
            return JsonHelper.errorJson("您对此服务没有操作权限");
        }

        configuration.getBasicConfig().setAlarmVirtualNode(octoAppkey);
        Result result = logService.updateLogAlarmConfigWithOpsCheck(configuration, true);
        if (result.getIsSuccess()) {
            logService.updateLogAlarmConfig(configuration);
            int affectedLine = logService.updateRestartAlarmTask(appkey);
            if (affectedLine < 1) {
                return JsonHelper.errorJson("变更Alarm任务状态失败");
            }
            BorpClient.saveOpt(UserUtils.getUser(), ActionType.UPDATE.getIndex(), octoAppkey,
                    EntityType.errorLogRestartAlarm(), "default", "", "");
            return JsonHelper.dataJson("ok");
        } else {
            return JsonHelper.errorJson(result.getMsg());
        }
    }

    @Worth(model = Worth.Model.ErrorLog, function = "查看过滤规则")
    @RequestMapping(value = "filter/list", method = RequestMethod.GET)
    public String filterList(String appkey, Model model) {
        java.util.List<String> apps = ServiceCommon.appsByUser();
        appkey = ServiceCommon.getDefaultAppkey(apps, appkey);
        model.addAttribute("apps", apps);
        model.addAttribute("appkey", appkey);
        return "/log/filter/list";
    }

    @RequestMapping(value = "filter/getFilters", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getFilters(String appkey) {
        if (StringUtil.isBlank(appkey)) {
            return JsonHelper.errorJson("appkey is blank");
        }
        // use alias appkey
        appkey = AppkeyAlias.aliasAppkey(appkey);
        List<ErrorLogFilter> filterList = errorLogFilterService.selectByAppkey(appkey);
        return JsonHelper.dataJson(filterList);
    }

    @Worth(model = Worth.Model.ErrorLog, function = "查看过滤器详情")
    @RequestMapping(value = "filter/getFilter", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getFilter(Integer filterId) {
        if (filterId == null) {
            return JsonHelper.errorJson("filterId is null");
        }
        ErrorLogFilter filter = errorLogFilterService.selectByFilterId(filterId);
        if (filter == null) {
            return JsonHelper.errorJson("there is not filter, filterId=" + filterId);
        }
        return JsonHelper.dataJson(filter);
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.ErrorLog, function = "添加过滤器")
    @RequestMapping(value = "filter/add", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String addFilter(ErrorLogFilter filter) {
        String octoAppkey = filter.getAppkey();
        filter.setAppkey(AppkeyAlias.aliasAppkey(filter.getAppkey()));
        String msg = checkRules(filter.getRules());
        if (msg != null) {
            return JsonHelper.errorJson(msg);
        }
        Integer id = errorLogFilterService.insert(filter);
        BorpClient.saveOpt(UserUtils.getUser(), ActionType.INSERT.getIndex(), octoAppkey,
                EntityType.errorLogAddFilter(), "ErrorLogFilter", "", JsonUtil.toString(filter));
        return JsonHelper.dataJson("ok");
    }

    private String checkRules(String json) {
        if (json == null) {
            return "条件不能为空";
        }
        Map<String, List<String>> rules = null;
        try {
            rules = (Map<String, List<String>>) JSONObject.parseObject(json, Map.class);
        } catch (Exception e) {
            logger.warn("条件Json解析：" + json);
            return "条件格式不对";
        }
        for (Map.Entry<String, List<String>> entry : rules.entrySet()) {
            for (String rule : entry.getValue()) {
                try {
                    Pattern.compile(rule);
                } catch (Exception e) {
                    logger.warn("条件不符合正则规范：" + rule);
                    return "条件不符合正则规范：" + rule;
                }
            }
        }
        return null;
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.ErrorLog, function = "修改过滤器")
    @RequestMapping(value = "filter/update", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String updateFilter(ErrorLogFilter filter, Boolean oldEnabled) {
        String octoAppkey = filter.getAppkey();
        filter.setAppkey(AppkeyAlias.aliasAppkey(filter.getAppkey()));
        String msg = checkRules(filter.getRules());
        if (msg != null) {
            return JsonHelper.errorJson(msg);
        }
        ErrorLogFilter oldFilter = errorLogFilterService.selectByFilterId(filter.getId());
        errorLogFilterService.update(filter, oldEnabled);
        BorpClient.saveOpt(UserUtils.getUser(), ActionType.UPDATE.getIndex(), octoAppkey,
                EntityType.errorLogUpdateFilter(), "ErrorLogFilter", JsonUtil.toString(oldFilter), JsonUtil.toString(filter));
        return JsonHelper.dataJson("ok");
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.ErrorLog, function = "删除过滤器")
    @RequestMapping(value = "filter/delete", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String deleteFilter(Integer filterId, String appkey) {
        ErrorLogFilter oldFilter = errorLogFilterService.selectByFilterId(filterId);
        errorLogFilterService.delete(filterId);
        BorpClient.saveOpt(UserUtils.getUser(), ActionType.DELETE.getIndex(), appkey,
                EntityType.errorLogDeleteFilter(), "ErrorLogFilter", JsonUtil.toString(oldFilter), "");
        return JsonHelper.dataJson("ok");
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.ErrorLog, function = "过滤器排序")
    @RequestMapping(value = "filter/sort", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String sortFilters(@RequestParam("filterIds") List<Integer> filterIds, String appkey) {
        if (filterIds == null || filterIds.size() == 0) {
            return JsonHelper.dataJson("ok");
        }
        errorLogFilterService.updateSort(filterIds);
        BorpClient.saveOpt(UserUtils.getUser(), ActionType.UPDATE.getIndex(), appkey,
                EntityType.errorLogSortFilter(), "default", "", filterIds.toString());
        return JsonHelper.dataJson("ok");
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.ErrorLog, function = "启用过滤器")
    @RequestMapping(value = "filter/enable", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String enableFilter(Integer filterId, String appkey) {
        if (filterId == null) {
            return JsonHelper.errorJson("filterId is null");
        }
        errorLogFilterService.updateEnable(filterId);
        BorpClient.saveOpt(UserUtils.getUser(), ActionType.UPDATE.getIndex(), appkey,
                EntityType.errorLogEnableFilter(), "default", "", "filterId=" + filterId.toString());
        return JsonHelper.dataJson("ok");
    }

    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @Worth(model = Worth.Model.ErrorLog, function = "禁用过滤器")
    @RequestMapping(value = "filter/disable", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String disableFilter(Integer filterId, String appkey) {
        if (filterId == null) {
            return JsonHelper.errorJson("filterId is null");
        }
        errorLogFilterService.updateDisable(filterId);
        BorpClient.saveOpt(UserUtils.getUser(), ActionType.UPDATE.getIndex(), appkey,
                EntityType.errorLogDisableFilter(), "default", "", "filterId=" + filterId.toString());
        return JsonHelper.dataJson("ok");
    }

    @Worth(model = Worth.Model.ErrorLog, function = "按Set分类")
    @RequestMapping(value = "group/by/set", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String groupBySet(@RequestParam(value = "appkey") String appkey,
                              @RequestParam(value = "start") String start,
                              @RequestParam(value = "end") String end) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusHours(1) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
        // use alias appkey to get report
        appkey = AppkeyAlias.aliasAppkey(appkey);
        return JsonHelper.dataJson(ErrorLogStatisticDao.groupBySet(appkey, TimeProcessor.getMinuteStart((int) (startTime.getTime() / 1000)),
                TimeProcessor.getMinuteStart((int) (endTime.getTime() / 1000))));
    }

    @Worth(model = Worth.Model.ErrorLog, function = "按主机分类")
    @RequestMapping(value = "group/by/host", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String groupByHost(@RequestParam(value = "appkey") String appkey,
                              @RequestParam(value = "start") String start,
                              @RequestParam(value = "end") String end,
                              @RequestParam(value = "hostSet", defaultValue = "All") String hostSet,
                              @RequestParam(value = "filterId", defaultValue = "-1") int filterId,
                              @RequestParam(value = "exceptionName", defaultValue = "") String exceptionName) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusHours(1) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
        // use alias appkey to get report
        appkey = AppkeyAlias.aliasAppkey(appkey);
        return JsonHelper.dataJson(ErrorLogStatisticDao.groupByHost(appkey, TimeProcessor.getMinuteStart((int) (startTime.getTime() / 1000)),
                TimeProcessor.getMinuteStart((int) (endTime.getTime() / 1000)), hostSet, filterId, exceptionName));
    }

    @Worth(model = Worth.Model.ErrorLog, function = "按异常类型分类")
    @RequestMapping(value = "group/by/filter", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String groupByFilterId(@RequestParam(value = "appkey") String appkey,
                                  @RequestParam(value = "start") String start,
                                  @RequestParam(value = "end") String end,
                                  @RequestParam(value = "hostSet", defaultValue = "All") String hostSet,
                                  @RequestParam(value = "host", defaultValue = "All") String host) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusHours(1) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
        // use alias appkey to get report
        appkey = AppkeyAlias.aliasAppkey(appkey);
        return JsonHelper.dataJson(ErrorLogStatisticDao.groupByFilterId(appkey, TimeProcessor.getMinuteStart((int) (startTime.getTime() / 1000)),
                TimeProcessor.getMinuteStart((int) (endTime.getTime() / 1000)), hostSet, host));
    }

    @Worth(model = Worth.Model.ErrorLog, function = "按异常时间分类")
    @RequestMapping(value = "group/by/time", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String groupTime(@RequestParam(value = "appkey") String appkey,
                            @RequestParam(value = "start") String start,
                            @RequestParam(value = "end") String end,
                            @RequestParam(value = "hostSet", defaultValue = "All") String hostSet,
                            @RequestParam(value = "host", defaultValue = "All") String host,
                            @RequestParam(value = "filterId", defaultValue = "-1") int filterId,
                            @RequestParam(value = "exceptionName", defaultValue = "") String exceptionName) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusHours(1) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
        // use alias appkey to get report
        appkey = AppkeyAlias.aliasAppkey(appkey);
        return JsonHelper.dataJson(ErrorLogStatisticDao.groupByTime(appkey, TimeProcessor.getMinuteStart((int) (startTime.getTime() / 1000)),
                TimeProcessor.getMinuteStart((int) (endTime.getTime() / 1000)), hostSet, host, filterId, exceptionName));
    }

    @Worth(model = Worth.Model.ErrorLog, function = "查询异常信息")
    @RequestMapping(value = "get/error", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getLog(@RequestParam(value = "appkey") String appkey,
                         @RequestParam(value = "start") String start,
                         @RequestParam(value = "end") String end,
                         @RequestParam(value = "hostSet", defaultValue = "All") String hostSet,
                         @RequestParam(value = "host", defaultValue = "All") String host,
                         @RequestParam(value = "message", defaultValue = "") String message,
                         @RequestParam(value = "filterId", defaultValue = "-1") int filterId,
                         @RequestParam(value = "exceptionName", defaultValue = "Others") String exceptionName,
                         Page page) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Date startTime = new Date((start == null ? new DateTime().minusHours(1) : formatter.parseDateTime(start)).getMillis());
        Date endTime = new Date((end == null ? new DateTime() : formatter.parseDateTime(end)).getMillis());
        // use alias appkey to get report
        appkey = AppkeyAlias.aliasAppkey(appkey);
        return JsonHelper.dataJson(EsClient.scanner(appkey, hostSet, host, filterId, exceptionName, startTime.getTime(),
                endTime.getTime(), message, page), page);
    }

    @Auth(level = Auth.Level.OCTO, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "/route/config/adjust", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getRouteConfig() {
        ResultData<String> result = logService.adjustRouteConfig();
        if (!result.isSuccess()) {
            return JsonHelper.errorJson(result.getMsg());
        }
        return JsonHelper.dataJson(result.getData());
    }

    @Worth(model = Worth.Model.ErrorLog, function = "异常日志服务状态")
    @RequestMapping(value = "status", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getStatus(String appkey) {
        if (StringUtils.isBlank(appkey)) {
            return JsonHelper.errorJson("appkey is null");
        }
        ResultData<String> result = logService.getErrorLogStatus(appkey);
        return JsonHelper.dataJson(result.getData());
    }

    @Worth(model = Worth.Model.ErrorLog, function = "启动异常日志服务")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "start", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String startErrorLog(String appkey) {
        if (StringUtils.isBlank(appkey)) {
            return JsonHelper.errorJson("appkey is null");
        }
        int affectedLine = logService.startErrorLog(appkey);
        if (affectedLine < 1) {
            return JsonHelper.errorJson("启动异常日志服务失败");
        }
        BorpClient.saveOpt(UserUtils.getUser(), ActionType.UPDATE.getIndex(), appkey,
                EntityType.errorLogStart(), "default", "", "start");
        return JsonHelper.dataJson("ok");
    }

    @Worth(model = Worth.Model.ErrorLog, function = "停止异常日志服务")
    @Auth(level = Auth.Level.ADMIN, responseMode = Auth.ResponseMode.JSON)
    @RequestMapping(value = "stop", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String stopErrorLog(String appkey) {
        if (StringUtils.isBlank(appkey)) {
            return JsonHelper.errorJson("appkey is null");
        }
        int affectedLine = logService.stopErrorLog(appkey);
        if (affectedLine < 1) {
            return JsonHelper.errorJson("停止异常日志服务失败");
        }
        BorpClient.saveOpt(UserUtils.getUser(), ActionType.UPDATE.getIndex(), appkey,
                EntityType.errorLogStop(), "default", "", "stop");
        return JsonHelper.dataJson("ok");
    }
}
