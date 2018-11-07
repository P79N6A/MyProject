package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.errorlog.ApplicationTest;
import com.sankuai.msgp.errorlog.pojo.ErrorLogCount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
public class ErrorLogDayReportServiceTest extends ApplicationTest {
    @Autowired
    private ErrorLogDayReportService errorLogDayReportService;
    @Test
    public void testSelecty() throws Exception {
        Date date = new Date(System.currentTimeMillis()  - 86400*1000L);
        for (int i = 0; i < 10000; i++) {
            List<ErrorLogCount> data = errorLogDayReportService.searchBy(date);
            Thread.sleep(2000);
            System.out.println(data.size());
        }
        Thread.sleep(50000);
    }
}
