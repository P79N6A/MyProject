package com.sankuai.octo.errorlog.service;

import com.sankuai.octo.errorlog.dao.ErrorLogDayReportDao;
import com.sankuai.octo.errorlog.model.ErrorLogDayReport;
import com.sankuai.octo.errorlog.model.ErrorLogDayReportExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-12-19
 */
@Service
public class ErrorLogDayReportService {
    @Resource
    private ErrorLogDayReportDao errorLogDayReportDao;

    public List<ErrorLogDayReport> select(String appkey, Date startTime, Date stopTime) {
        ErrorLogDayReportExample example = new ErrorLogDayReportExample();
        ErrorLogDayReportExample.Criteria criteria = example.or();
        if (appkey != null) {
            criteria.andAppkeyEqualTo(appkey);
        }
        if (startTime != null) {
            criteria.andDtGreaterThanOrEqualTo(startTime);
        }
        if (stopTime != null) {
            criteria.andDtLessThanOrEqualTo(stopTime);
        }
        return errorLogDayReportDao.selectByExample(example);
    }
}
