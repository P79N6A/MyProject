package com.sankuai.octo.errorlog.dao;

import com.sankuai.octo.errorlog.model.LogAlarmSeverityConfig;
import com.sankuai.octo.errorlog.model.LogAlarmSeverityConfigExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface LogAlarmSeverityConfigDao {

    int insert(LogAlarmSeverityConfig record);

    List<LogAlarmSeverityConfig> selectByExample(LogAlarmSeverityConfigExample example);

    int insertOrUpdateLogAlarmSeverityConfig(LogAlarmSeverityConfig record);
}