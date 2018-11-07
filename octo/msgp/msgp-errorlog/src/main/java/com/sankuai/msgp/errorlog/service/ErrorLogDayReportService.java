package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.errorlog.dao.ErrorLogDayReportDao;
import com.sankuai.msgp.errorlog.pojo.ErrorLogCount;
import com.sankuai.msgp.errorlog.pojo.ErrorLogDayReport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-12-19
 */
@Service
public class ErrorLogDayReportService {
    @Autowired
    private ErrorLogDayReportDao errorLogDayReportDao;

    public List<ErrorLogCount> searchBy(Date dt) {
        return errorLogDayReportDao.searchBy(new java.sql.Date(dt.getTime()));
    }

    public List<ErrorLogDayReport> searchDailyCount(Date dt, int days) {
        return errorLogDayReportDao.searchDailyCount(new java.sql.Date(dt.getTime()), days);
    }

}