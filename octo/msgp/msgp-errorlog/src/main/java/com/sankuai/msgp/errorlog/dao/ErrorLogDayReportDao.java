package com.sankuai.msgp.errorlog.dao;

import com.sankuai.msgp.errorlog.pojo.ErrorLogCount;
import com.sankuai.msgp.errorlog.pojo.ErrorLogDayReport;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorLogDayReportDao {

    List<ErrorLogCount> searchBy( @Param("dt")java.sql.Date dt);

    List<ErrorLogDayReport> searchDailyCount( @Param("dt")java.sql.Date dt, @Param("days")Integer days);

    int genDailyReportData(@Param("date")String date, @Param("startSeconds")Long startSeconds, @Param("endSeconds")Long endSeconds);

    int countOneDayData(String date);

    int deleteOneDayData(String date);

    List<ErrorLogCount> get3DaysAppkeyLogCount();

    int get3DaysLogCountSum();
}