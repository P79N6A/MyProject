package com.sankuai.octo.errorlog.service;

import com.dianping.zebra.util.StringUtils;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.HttpUtil;
import com.sankuai.msgp.common.utils.client.EsClient;
import com.sankuai.octo.errorlog.dao.ErrorLogServiceStatusDao;
import com.sankuai.octo.errorlog.dao.ErrorLogStatisticDao;
import com.sankuai.octo.errorlog.model.*;
import com.sankuai.octo.msgp.serivce.AppkeyAuth;
import com.sankuai.octo.msgp.utils.Auth;
import com.sankuai.octo.msgp.utils.Result;
import com.sankuai.octo.msgp.utils.ResultData;
import com.sankuai.octo.msgp.utils.remote.OpsUtil;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    private Configuration hBaseConfiguration;
    private final String TASK_HOST_CONFIG = "error_log_task_host";
    private final String APPKEY_FIXED_BLACKLIST_CFG = "appkey.blacklist.fixed";

    @Resource
    private LogAlarmConfigService logAlarmConfigService;

    @Resource
    private LogAlarmSeverityConfigService logAlarmSeverityConfigService;

    @Resource
    private ErrorLogDayReportService errorLogDayReportService;

    @Resource
    private ErrorLogServiceStatusDao serviceStatusDao;

    /**
     * 页面启动Alarm
     *
     * @param appkey
     * @return
     */
    public int updateStartAlarmTask(String appkey) {
        return logAlarmConfigService.updateStartAlarmTask(appkey);
    }

    /**
     * 页面停止Alarm
     *
     * @param appkey
     * @return
     */
    public int updateStopAlarmTask(String appkey) {
        return logAlarmConfigService.updateStopAlarmTask(appkey);
    }

    /**
     * 页面重启Alarm
     *
     * @param appkey
     * @return
     */
    public int updateRestartAlarmTask(String appkey) {
        return logAlarmConfigService.updateRestartAlarmTask(appkey);
    }

    /**
     * 通过唯一主键在ES查询异常log
     *
     * @param uniqueKey
     * @return
     * @throws Exception
     */
    public ParsedLog getLogFromES(String uniqueKey) throws Exception {
        ParsedLog parsedLog = null;
        try {
            Map<String, Object> result = EsClient.getLogByUniqueKey(uniqueKey);
            if (result != null) {
                parsedLog = parseESResult(result);
            }
        } catch (Exception e) {
            throw e;
        }
        return parsedLog;
    }

    public LogAlarmConfiguration getLogAlarmConfiguration(String appkey) {
        if (appkey == null || appkey.trim().isEmpty()) {
            return null;
        }
        LogAlarmConfiguration configuration = new LogAlarmConfiguration();
        LogAlarmConfig config = logAlarmConfigService.getByAppkey(appkey);
        if (config == null) {
            return null;
        }
        configuration.setBasicConfig(config);
        LogAlarmSeverityConfig severityConfig = logAlarmSeverityConfigService.getByAppkey(appkey);
        configuration.setSeverityConfig(severityConfig);
        return configuration;
    }

    public Result updateLogAlarmConfigWithOpsCheck(LogAlarmConfiguration configuration, boolean doOpsCheck) {
        Result result = new Result();
        if (configuration == null || configuration.getBasicConfig() == null) {
            return result.failure("配置信息为空");
        }
        if (doOpsCheck) {
            // 检查octo appkey在服务树上是否有绑定，绑定的appkey最迟一天后有虚拟节点
            String octoAppkey = configuration.getBasicConfig().getAlarmVirtualNode();
            ResultData<Boolean> hasBindSrvTreeRet = OpsUtil.hasBindSrvTree(octoAppkey);
            if (!hasBindSrvTreeRet.isSuccess()) {
                return result.failure(hasBindSrvTreeRet.getMsg());
            }
        }
        logAlarmConfigService.insertOrUpdate(configuration.getBasicConfig());
        configuration.getSeverityConfig().setAppkey(configuration.getBasicConfig().getAppkey());
        logAlarmSeverityConfigService.insertOrUpdate(configuration.getSeverityConfig());
        return result.success();
    }

    public Result updateLogAlarmConfig(LogAlarmConfiguration configuration) {
        return updateLogAlarmConfigWithOpsCheck(configuration, false);
    }

    public Integer getErrorCount(String appkey, Date startTime, Date stopTime) {
        return ErrorLogStatisticDao.getErrorCount(appkey, startTime, stopTime);
    }

    public Map<String, Integer> getErrorLogTrend(Date startTime, Date stopTime, Integer periodType, String appkey) {

        long start = startTime.getTime();
        long stop = stopTime.getTime();

        long period = 3600000 * 24;
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd 00:00:00");

        if (periodType != null && periodType.equals(2)) { // 月
            period = period * 27;
            df = new SimpleDateFormat("yyyy/MM/01 00:00:00");
        }

        Set<String> keyRangeSet = new HashSet<String>();
        while (start <= stop) {
            String time = df.format(new Date(start));
            keyRangeSet.add(time);
            start += period;
        }
        keyRangeSet.add(df.format(stopTime));

        Map<String, Integer> report = new HashMap<String, Integer>();
        for (String timeKey : keyRangeSet) {
            report.put(timeKey, 0);
        }

        List<ErrorLogDayReport> errorLogDayReportList = errorLogDayReportService.select(appkey, startTime, stopTime);
        for (ErrorLogDayReport errorLogDayReport : errorLogDayReportList) {
            String timeKey = df.format(errorLogDayReport.getDt());
            report.put(timeKey, report.get(timeKey) + errorLogDayReport.getLogCount());
        }

        return report;
    }

    /**
     * 手动触发路由调整
     *
     * @return
     */
    public ResultData<String> adjustRouteConfig() {
        ResultData<String> result = new ResultData<>();
        // 读取ErrorLog任务主机配置
        MtConfigClient errorLogConfigClient = new MtConfigClient();
        //octo上申请的appkey
        errorLogConfigClient.setAppkey("com.sankuai.inf.octo.errorlog");
        //配置实例的标识(id),必须在服务进程内全局唯一
        errorLogConfigClient.setId("errorLogConfigClient");
        errorLogConfigClient.setModel("v2");
        // 初始化client
        errorLogConfigClient.init();
        String taskHostCfg = errorLogConfigClient.getValue(TASK_HOST_CONFIG);
        if (StringUtils.isBlank(taskHostCfg)) {
            return result.failure("异常日志任务主机配置为空");
        }
        String[] cfgStrs = taskHostCfg.replaceAll("\\s", "").split(":");
        if (cfgStrs.length < 2) {
            return result.failure("异常日志任务主机配置无IP, 配置格式是:hostname:ip");
        }
        String taskHostIp = cfgStrs[1];
        String url = "http://" + taskHostIp + ":8080/route/config/adjust";

        result.setSuccResult(HttpUtil.getResult(url));
        return result;
    }

    public ResultData<String> getErrorLogStatus(String appkey) {
        ResultData<String> result = new ResultData<>();
        ErrorLogServiceStatus status = serviceStatusDao.getAppkeyStatus(appkey);
        if (status == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -3);
            Date start = cal.getTime();

            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 0);
            Date end = cal.getTime();

            int logCount = getErrorCount(appkey, start, end);
            if (logCount > 0) {
                if (isInFixedBlacklist(appkey)) {
                    serviceStatusDao.updateAppkeyStatus(new ErrorLogServiceStatus(appkey, ErrorLogServiceStatus.ServiceStatus.STOP.name()));
                    result.setSuccResult(ErrorLogServiceStatus.ServiceStatus.STOP.name());
                } else {
                    // 三天内有日志, 且不在固定黑名单中，则设置为启动状态
                    serviceStatusDao.updateAppkeyStatus(new ErrorLogServiceStatus(appkey, ErrorLogServiceStatus.ServiceStatus.START.name()));
                    result.setSuccResult(ErrorLogServiceStatus.ServiceStatus.START.name());
                }
            } else {
                serviceStatusDao.updateAppkeyStatus(new ErrorLogServiceStatus(appkey, ErrorLogServiceStatus.ServiceStatus.STOP.name()));
                result.setSuccResult(ErrorLogServiceStatus.ServiceStatus.STOP.name());
            }
        } else {
            // 避免手动修改黑名单导致服务禁用
            if (isInFixedBlacklist(appkey)) {
                serviceStatusDao.updateAppkeyStatus(new ErrorLogServiceStatus(appkey, ErrorLogServiceStatus.ServiceStatus.STOP.name()));
                result.setSuccResult(ErrorLogServiceStatus.ServiceStatus.STOP.name());
            } else {
                result.setSuccResult(status.getServiceStatus());
            }
        }
        return result;
    }

    public int startErrorLog(String appkey) {
        deleteFixedBlacklist(appkey);
        return serviceStatusDao.updateAppkeyStatus(new ErrorLogServiceStatus(appkey, ErrorLogServiceStatus.ServiceStatus.START.name()));
    }

    public int stopErrorLog(String appkey) {
        addFixedBlacklist(appkey);
        return serviceStatusDao.updateAppkeyStatus(new ErrorLogServiceStatus(appkey, ErrorLogServiceStatus.ServiceStatus.STOP.name()));
    }


    public ResultData<List<String>> getErrorLogStartAppkey(String day) {
        ResultData<List<String>> result = new ResultData<>();
        List<String> appkeys = null;
        try {
            if (StringUtils.isBlank(day)) {
                appkeys = serviceStatusDao.getAllStartAppkey();
            } else {
                appkeys = serviceStatusDao.getStartAppkeys(day);
            }
        } catch (Exception e) {
            result.failure("查询失败: " + e.getMessage());
        }
        result.setSuccResult(appkeys);
        return result;
    }

    private boolean isInFixedBlacklist(String appkey) {
        MtConfigClient topologyCfgClient = getTopologyCfgClient();

        String blackListStr = topologyCfgClient.getValue(APPKEY_FIXED_BLACKLIST_CFG);
        if (StringUtils.isBlank(blackListStr)) {
            return false;
        }
        return blackListStr.contains(appkey);
    }

    private boolean addFixedBlacklist(String appkey) {
        MtConfigClient topologyCfgClient = getTopologyCfgClient();

        String blackListStr = topologyCfgClient.getValue(APPKEY_FIXED_BLACKLIST_CFG);
        blackListStr = blackListStr == null ? "" : blackListStr;
        if (!blackListStr.contains(appkey)) {
            String newValue;
            if (StringUtils.isBlank(blackListStr)) {
                newValue = appkey;
            } else {
                newValue = blackListStr + "," + appkey;
            }
            topologyCfgClient.setValue(APPKEY_FIXED_BLACKLIST_CFG, newValue);
            return true;
        }
        return false;
    }

    private boolean deleteFixedBlacklist(String appkey) {
        MtConfigClient topologyCfgClient = getTopologyCfgClient();

        String blackListStr = topologyCfgClient.getValue(APPKEY_FIXED_BLACKLIST_CFG);
        if (blackListStr == null || !blackListStr.contains(appkey)) {
            return false;
        }
        Set appkeyBlackSet = new HashSet<>(Arrays.asList(blackListStr.replaceAll("\\s+", "").split(",")));
        if (appkeyBlackSet.remove(appkey)) {
            topologyCfgClient.setValue(APPKEY_FIXED_BLACKLIST_CFG, setToString(appkeyBlackSet));
            return true;
        }
        return false;
    }

    private MtConfigClient getTopologyCfgClient() {
        MtConfigClient topologyCfgClient = new MtConfigClient();
        //octo上申请的appkey
        topologyCfgClient.setAppkey("com.sankuai.inf.octo.errorlog.topology");
        //配置实例的标识(id),必须在服务进程内全局唯一
        topologyCfgClient.setId(String.valueOf(System.currentTimeMillis()));
        topologyCfgClient.setModel("v2");

        // 初始化client
        topologyCfgClient.init();
        return topologyCfgClient;
    }

    /**
     * 异常日志因为存在别名Appkey，别名Appkey无法通过@Auth注解进行权限认证
     *
     * @param appkey
     * @return
     */
    public boolean hasAuth(String appkey) {
        User user = UserUtils.getUser();
        return AppkeyAuth.hasAuth(appkey, Auth.Level.ADMIN.getValue(), user);
    }

    private ParsedLog parseESResult(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return null;
        }
        ParsedLog parsedLog = new ParsedLog();
        parsedLog.setUniqueKey(getMapValue(result, "error_unique_key"));
        parsedLog.setAppkey(getMapValue(result, "error_appkey"));
        try {
            Long logTime = Long.parseLong(result.get("error_time").toString());
            parsedLog.setLogTime(new Date(logTime));
        } catch (Exception e) {
            logger.warn("Time parse fail, logTime={}", result.get("error_time"), e);
        }
        parsedLog.setHost(getMapValue(result, "error_host"));
        parsedLog.setLocation(getMapValue(result, "error_location"));
        parsedLog.setMessage(getMapValue(result, "error_message"));
        parsedLog.setException(getMapValue(result, "error_exception"));
        return parsedLog;
    }

    private String getMapValue(Map<String, Object> map, String key) {
        return map.get(key) != null ? map.get(key).toString() : "";
    }

    private String setToString(Set appkeyBlackSet) {
        if (appkeyBlackSet == null || appkeyBlackSet.isEmpty()) {
            return "";
        } else {
            String setString = appkeyBlackSet.toString();
            return setString.substring(1, setString.length() - 1);
        }
    }
}
