package com.sankuai.msgp.errorlog.dao;

import com.sankuai.msgp.errorlog.pojo.LogAlarmConfig;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAlarmConfigDao {
    @Delete({
            "delete from log_alarm_config ",
            "where appkey = #{appkey,jdbcType=VARCHAR}"
    })
    int delete(@Param("appkey")String appkey);


    @Update({
            "update log_alarm_config ",
            "set enabled = #{enabled,jdbcType=BIT} ",
            "where appkey = #{appkey,jdbcType=VARCHAR}"
    })
    int updateBy(@Param("enabled")Boolean enabled,@Param("appkey")String appkey);

    List<LogAlarmConfig> search(@Param("appkey")String appkey,@Param("id")Integer id);

    List<LogAlarmConfig> selectAlarmTaskToExecCfg();

    List<LogAlarmConfig> getEnabledLogAlarm();

    int updateOperFinish(String appkey);

    List<LogAlarmConfig> selectAppkeyAlarmHost();

    int updateAlarmVirtualNode(@Param("octoAppkey")String octoAppkey, @Param("errorlogAppkey")String errorlogAppkey);
}