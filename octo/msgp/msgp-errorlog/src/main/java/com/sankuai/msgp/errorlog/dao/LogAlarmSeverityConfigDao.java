package com.sankuai.msgp.errorlog.dao;

import com.sankuai.msgp.errorlog.pojo.LogAlarmSeverityConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAlarmSeverityConfigDao {

    @Select({
        "select",
        "id, appkey, ok, warning, error, disaster",
        "from log_alarm_severity_config",
        "where appkey = #{appkey,jdbcType=INTEGER}"
    })
    @ResultMap("BaseResultMap")
    List<LogAlarmSeverityConfig> searchBy(@Param("appkey")String appkey);

    int save(LogAlarmSeverityConfig logAlarmSeverityConfig);


}