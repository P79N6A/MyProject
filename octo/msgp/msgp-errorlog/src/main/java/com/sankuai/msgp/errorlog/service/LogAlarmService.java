package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.common.model.errorlog.ParsedLog;
import com.sankuai.msgp.common.utils.client.Messager;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.errorlog.domain.LogAlarmConfiguration;
import com.sankuai.msgp.errorlog.pojo.LogAlarmConfig;
import com.sankuai.msgp.errorlog.pojo.LogAlarmSeverityConfig;
import com.sankuai.msgp.errorlog.task.AlarmTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogAlarmService {
    private static final Logger logger = LoggerFactory.getLogger(LogAlarmService.class);


    private Map<String, AlarmTask> alarmTaskMap;

    @Autowired
    private LogAlarmConfigService logAlarmConfigService;

    @Autowired
    private LogAlarmSeverityConfigService logAlarmSeverityConfigService;

    @Autowired
    private ErrorLogFilterService errorLogFilterService;

    @Autowired
    private ErrorLogRouteCfgService routeCfgService;


    public LogAlarmService() {
        alarmTaskMap = new ConcurrentHashMap<>();
    }

    /**
     * 开机启动AlarmTask
     * ErrorLogRouteCfgService init解析完配置后调用
     *
     * @param errorLogRouteCfgService 存在和routeCfgService的循环依赖，导致init中调用时routeCfgService注入为null, 此处传入对象
     */
    public void startAlarmTasks(ErrorLogRouteCfgService errorLogRouteCfgService) {
        List<LogAlarmConfig> configs = logAlarmConfigService.getEnabledLogAlarm();
        for (LogAlarmConfig config : configs) {
            String appkey = config.getAppkey();
            if (errorLogRouteCfgService.isRouteAppkey(appkey) && config.getEnabled()) {
                LogAlarmConfiguration configuration = new LogAlarmConfiguration();
                configuration.setBasicConfig(config);

                LogAlarmSeverityConfig severityConfig = logAlarmSeverityConfigService.getByAppkey(appkey);
                configuration.setSeverityConfig(severityConfig);

                AlarmTask alarmTask = new AlarmTask(configuration, errorLogFilterService);
                alarmTaskMap.put(appkey, alarmTask);
            }
        }
        logger.info("Start alarm task, appkey={}", alarmTaskMap.keySet());
        if (alarmTaskMap.keySet().isEmpty()) {
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志]服务重启节点报警Task为空, ip=" + CommonHelper.getLocalIp());
        }
    }

    /**
     * 当主机处理Appkey有变动时, 更新AlarmTask
     *
     */
    public void updateAlarmTasks() {
        List<LogAlarmConfig> configs = logAlarmConfigService.getEnabledLogAlarm();
        for (LogAlarmConfig config : configs) {
            String appkey = config.getAppkey();
            if (routeCfgService.isRouteAppkey(appkey) && config.getEnabled()) {
                if (alarmTaskMap.containsKey(appkey)) {
                    continue;
                }
                LogAlarmConfiguration configuration = new LogAlarmConfiguration();
                configuration.setBasicConfig(config);

                LogAlarmSeverityConfig severityConfig = logAlarmSeverityConfigService.getByAppkey(appkey);
                configuration.setSeverityConfig(severityConfig);

                AlarmTask alarmTask = new AlarmTask(configuration, errorLogFilterService);
                alarmTaskMap.put(appkey, alarmTask);
                logger.info("Alarm task added {}", appkey);
            } else if (alarmTaskMap.containsKey(appkey)) {
                alarmTaskMap.remove(appkey).stop();
                logger.info("Alarm task removed {}", appkey);
            }
        }
    }

    public AlarmTask updateStartAlarmTask(String appkey) {
        AlarmTask alarmTask = null;
        try {
            if (alarmTaskMap.containsKey(appkey)) {
                // 出现DB未更新情况，加log，异常捕获排查
                int affectRow = logAlarmConfigService.updateEnable(appkey);
                int finAffectRow = logAlarmConfigService.finishAlarmOperation(appkey);
                logger.info("start AlarmTask: appkey={}, db update affect={},{}", appkey, affectRow, finAffectRow);
                return alarmTaskMap.get(appkey);
            }

            LogAlarmConfiguration configuration = getLogAlarmConfiguration(appkey);
            if (configuration == null) {
                logger.error("cannot get configuration of " + appkey);
                return null;
            }
            configuration.getBasicConfig().setEnabled(true);
            alarmTask = new AlarmTask(configuration, errorLogFilterService);
            alarmTaskMap.put(appkey, alarmTask);

            // 出现DB未更新情况，加log，异常捕获排查
            int affectRow = logAlarmConfigService.updateEnable(appkey);
            int finAffectRow = logAlarmConfigService.finishAlarmOperation(appkey);
            logger.info("start AlarmTask: appkey={}, db update affect={},{}", appkey, affectRow, finAffectRow);
        } catch (Exception e) {
            logger.error("start AlarmTask fail, appkey={}", appkey, e);
        }
        return alarmTask;
    }

    public boolean updateStopAlarmTask(String appkey) {
        try {
            if (!alarmTaskMap.containsKey(appkey)) {
                // 避免db未变更
                int affectRow = logAlarmConfigService.updateDisable(appkey);
                int finAffectRow = logAlarmConfigService.finishAlarmOperation(appkey);
                logger.info("stop AlarmTask: appkey={}, db update affect={},{}", appkey, affectRow, finAffectRow);
                return true;
            }
            AlarmTask alarmTask = alarmTaskMap.remove(appkey);
            alarmTask.stop();

            int affectRow = logAlarmConfigService.updateDisable(appkey);
            int finAffectRow = logAlarmConfigService.finishAlarmOperation(appkey);
            logger.info("stop AlarmTask: appkey={}, db update affect={},{}", appkey, affectRow, finAffectRow);
        } catch (Exception e) {
            logger.error("stop AlarmTask fail, appkey={}", appkey, e);
        }
        return true;
    }

    public AlarmTask updateRestartAlarmTask(String appkey) {
        logger.info("restart AlarmTask: appkey=" + appkey);
        if (alarmTaskMap.containsKey(appkey)) {
            updateStopAlarmTask(appkey);
        }
        return updateStartAlarmTask(appkey);
    }


    public LogAlarmConfiguration getLogAlarmConfiguration(String appkey) {
        if (StringUtils.isBlank(appkey)) {
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

    public void addLog(ParsedLog log) {
        String appkey = log.getAppkey();
        AlarmTask alarmTask = alarmTaskMap.get(appkey);
        if (alarmTask != null) {
            alarmTask.addLog(log);
        }
    }
}