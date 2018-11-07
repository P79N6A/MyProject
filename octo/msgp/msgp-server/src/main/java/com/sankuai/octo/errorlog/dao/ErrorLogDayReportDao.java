package com.sankuai.octo.errorlog.dao;

import com.sankuai.octo.errorlog.model.ErrorLogDayReport;
import com.sankuai.octo.errorlog.model.ErrorLogDayReportExample;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ErrorLogDayReportDao {

    List<ErrorLogDayReport> selectByExample(ErrorLogDayReportExample example);
}