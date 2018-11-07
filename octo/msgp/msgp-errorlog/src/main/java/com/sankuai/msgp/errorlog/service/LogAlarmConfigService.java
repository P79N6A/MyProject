package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.errorlog.dao.LogAlarmConfigDao;
import com.sankuai.msgp.errorlog.pojo.LogAlarmConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class LogAlarmConfigService {

    private static final int MIN_GAP_SECONDS = 30;

    @Resource
    private LogAlarmConfigDao logAlarmConfigDao;

    public List<LogAlarmConfig> getEnabledLogAlarm() {
        return logAlarmConfigDao.getEnabledLogAlarm();
    }

    public LogAlarmConfig getByAppkey(String appkey) {
        if (appkey == null) {
            return null;
        }
        List<LogAlarmConfig> logAlarmConfigList = logAlarmConfigDao.search(appkey, null);
        return logAlarmConfigList.isEmpty() ? null : logAlarmConfigList.get(0);
    }

    public int updateEnable(String appkey) {
        if (StringUtils.isBlank(appkey)) {
            return 0;
        }
        return logAlarmConfigDao.updateBy(true, appkey);
    }

    public int updateDisable(String appkey) {
        if (StringUtils.isBlank(appkey)) {
            return 0;
        }
        return logAlarmConfigDao.updateBy(false,appkey);
    }

    public int finishAlarmOperation(String appkey) {
        if (StringUtils.isBlank(appkey)) {
            return 0;
        }
        return logAlarmConfigDao.updateOperFinish(appkey);
    }
}