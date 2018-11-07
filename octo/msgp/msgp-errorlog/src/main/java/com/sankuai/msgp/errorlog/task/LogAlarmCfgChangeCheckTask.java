package com.sankuai.msgp.errorlog.task;

import com.sankuai.msgp.errorlog.dao.LogAlarmConfigDao;
import com.sankuai.msgp.errorlog.pojo.LogAlarmConfig;
import com.sankuai.msgp.errorlog.service.ErrorLogRouteCfgService;
import com.sankuai.msgp.errorlog.service.LogAlarmService;
import com.sankuai.msgp.errorlog.util.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Scope("singleton")
@Component
public class LogAlarmCfgChangeCheckTask {
    private final Logger logger = LoggerFactory.getLogger(LogAlarmCfgChangeCheckTask.class);

    @Resource
    private LogAlarmService logAlarmService;

    @Autowired
    private ErrorLogRouteCfgService routeCfgService;

    @Autowired
    private LogAlarmConfigDao logAlarmConfigDao;

    static final int CHECK_INTERVAL_SECONDS = 30;

    enum TaskOperType {
        START, STOP, RESTART, NO;

        public static TaskOperType getOperType(String type) {
            switch (type) {
                case "START":
                    return START;
                case "STOP":
                    return STOP;
                case "RESTART":
                    return RESTART;
                default:
                    return NO;
            }
        }
    }

    @PostConstruct
    public void init() {
        ScheduledExecutorService obtainAppkeyInfo = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("CheckAlarmCfgChange", true));

        // 30s更新一次
        obtainAppkeyInfo.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("checkAlarmCfgChange running");
                    checkAlarmCfgChange();
                } catch (Exception e) {
                    logger.error("checkAlarmCfgChange failed", e);
                }
            }
        }, 0L, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void checkAlarmCfgChange() {
        List<LogAlarmConfig> toExecAlarmTasks = logAlarmConfigDao.selectAlarmTaskToExecCfg();
        for (LogAlarmConfig toExecAlarmTask : toExecAlarmTasks) {
            String appkey = toExecAlarmTask.getAppkey();
            if (!routeCfgService.isRouteAppkey(appkey)) {
                continue;
            }
            TaskOperType operType = TaskOperType.getOperType(toExecAlarmTask.getTaskOperType());
            logger.info("Alarm task changed: {}, operType={}", appkey, operType);
            switch (operType) {
                case START:
                    logAlarmService.updateStartAlarmTask(appkey);
                    break;
                case STOP:
                    logAlarmService.updateStopAlarmTask(appkey);
                    break;
                case RESTART:
                    logAlarmService.updateRestartAlarmTask(appkey);
                    break;
                default:
                    logger.warn("Appkey={} alarm oper type not match, operType={}", appkey, operType);
            }
        }
    }
}