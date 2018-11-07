package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.errorlog.dao.LogAlarmSeverityConfigDao;
import com.sankuai.msgp.errorlog.pojo.LogAlarmSeverityConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class LogAlarmSeverityConfigService {
    @Resource
    private LogAlarmSeverityConfigDao logAlarmSeverityConfigDao;

    public LogAlarmSeverityConfig getByAppkey(String appkey) {
        if (appkey == null) {
            return null;
        }

        List<LogAlarmSeverityConfig> logAlarmSeverityConfigList = logAlarmSeverityConfigDao.searchBy(appkey);
        return logAlarmSeverityConfigList.isEmpty() ? null : logAlarmSeverityConfigList.get(0);
    }

    public int insertOrUpdate(LogAlarmSeverityConfig severityConfig) {
        if (severityConfig == null) {
            return 0;
        }
        return logAlarmSeverityConfigDao.save(severityConfig);
    }
}
