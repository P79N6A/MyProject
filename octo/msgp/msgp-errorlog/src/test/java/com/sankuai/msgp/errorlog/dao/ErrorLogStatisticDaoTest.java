package com.sankuai.msgp.errorlog.dao;

import com.sankuai.msgp.errorlog.ApplicationTest;
import com.sankuai.msgp.errorlog.entity.ErrorLogStatisticQuery;
import com.sankuai.msgp.errorlog.pojo.TimeCount;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ErrorLogStatisticDaoTest extends ApplicationTest {
    @Autowired
    private ErrorLogStatisticDao errorLogStatisticDao;

    @Test
    public void testGroupByTime(){
        ErrorLogStatisticQuery query = new ErrorLogStatisticQuery("com.sankuai.inf.logCollector", 1473077640, 1473238980, "All", -1, "");
        List<TimeCount> list  = errorLogStatisticDao.groupByTime(query);
        System.out.println(list);
    }
}
