package com.sankuai.octo.errorlog.service;

import com.sankuai.octo.errorlog.dao.LogAlarmConfigDao;
import com.sankuai.octo.errorlog.model.LogAlarmConfig;
import com.sankuai.octo.errorlog.model.LogAlarmConfigExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-10-17
 */
@Service
public class LogAlarmConfigService {

    private final static int MIN_GAP_SECONDS = 60;
    @Resource
    private LogAlarmConfigDao logAlarmConfigDao;

    public LogAlarmConfig getByAppkey(String appkey) {
        if (appkey == null) {
            return null;
        }
        LogAlarmConfigExample example = new LogAlarmConfigExample();
        example.or().andAppkeyEqualTo(appkey);
        List<LogAlarmConfig> logAlarmConfigList = logAlarmConfigDao.selectByExample(example);
        return logAlarmConfigList.isEmpty() ? null : logAlarmConfigList.get(0);
    }

    public int insertOrUpdate(LogAlarmConfig config) {
        if (config == null) {
            return 0;
        }
        if (config.getGapSeconds() < MIN_GAP_SECONDS) {
            config.setGapSeconds(MIN_GAP_SECONDS);
        }
        if (config.getEnabled() == null) {
            config.setEnabled(false);
        }
        return logAlarmConfigDao.insertOrUpdateLogAlarmConfig(config);
    }

    public int updateStartAlarmTask(String appkey) {
        return logAlarmConfigDao.updateStartAlarmTask(appkey);
    }

    public int updateStopAlarmTask(String appkey) {
        return logAlarmConfigDao.updateStopAlarmTask(appkey);
    }


    public int updateRestartAlarmTask(String appkey) {
        return logAlarmConfigDao.updateRestartAlarmTask(appkey);
    }

    public int delete(String appkey) {
        if (appkey == null) {
            return 0;
        }
        LogAlarmConfigExample example = new LogAlarmConfigExample();
        example.or().andAppkeyEqualTo(appkey);
        return logAlarmConfigDao.deleteByExample(example);
    }
}
