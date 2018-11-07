package com.sankuai.octo.errorlog.service;

import com.sankuai.octo.errorlog.dao.LogAlarmSeverityConfigDao;
import com.sankuai.octo.errorlog.model.LogAlarmSeverityConfig;
import com.sankuai.octo.errorlog.model.LogAlarmSeverityConfigExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-10-17
 */
@Service
public class LogAlarmSeverityConfigService {
    @Resource
    private LogAlarmSeverityConfigDao logAlarmSeverityConfigDao;

    public LogAlarmSeverityConfig getByAppkey(String appkey) {
        if (appkey == null) {
            return null;
        }
        LogAlarmSeverityConfigExample example = new LogAlarmSeverityConfigExample();
        example.or().andAppkeyEqualTo(appkey);
        List<LogAlarmSeverityConfig> logAlarmSeverityConfigList = logAlarmSeverityConfigDao.selectByExample(example);
        return logAlarmSeverityConfigList.isEmpty() ? null : logAlarmSeverityConfigList.get(0);
    }

    public int insertOrUpdate(LogAlarmSeverityConfig severityConfig) {
        if (severityConfig == null) {
            return 0;
        }
        return logAlarmSeverityConfigDao.insertOrUpdateLogAlarmSeverityConfig(severityConfig);
    }
}
