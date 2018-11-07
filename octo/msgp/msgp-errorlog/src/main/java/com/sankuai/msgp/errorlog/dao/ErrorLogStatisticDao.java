package com.sankuai.msgp.errorlog.dao;

import com.sankuai.msgp.errorlog.entity.ErrorLogStatisticQuery;
import com.sankuai.msgp.errorlog.pojo.*;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectKey;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorLogStatisticDao {

    int insert(ErrorLogStatistic record);

    int batchInsert(List<ErrorLogStatistic> errorLogStatistics);

    List<TimeCount> groupByTime(ErrorLogStatisticQuery query);

    Integer getErrorCount(ErrorLogStatisticQuery query);

}