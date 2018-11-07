package com.sankuai.octo.errorlog.dao;

import com.sankuai.octo.errorlog.model.LogAlarmConfig;
import com.sankuai.octo.errorlog.model.LogAlarmConfigExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface LogAlarmConfigDao {

    int deleteByExample(LogAlarmConfigExample example);

    List<LogAlarmConfig> selectByExample(LogAlarmConfigExample example);

    // add by yangguo03
    int insertOrUpdateLogAlarmConfig(LogAlarmConfig record);

    // add by emma
    int updateStartAlarmTask(String appkey);

    // add by emma
    int updateStopAlarmTask(String appkey);

    // add by emma
    int updateRestartAlarmTask(String appkey);
}