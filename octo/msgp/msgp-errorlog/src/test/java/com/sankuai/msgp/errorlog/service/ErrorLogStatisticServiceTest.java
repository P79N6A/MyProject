package com.sankuai.msgp.errorlog.service;

import com.sankuai.msgp.common.utils.DateTimeUtil;
import com.sankuai.msgp.errorlog.pojo.ErrorLogStatistic;
import com.sankuai.msgp.errorlog.util.CommonUtil;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:applicationContext.xml",
        "classpath*:mybatis*.xml"
})
public class ErrorLogStatisticServiceTest {
    @Autowired
    private ErrorLogStatisticService errorLogStatisticService;

    @Test
    public void testAdd() throws Exception {
        for (int i = 0; i < 10000; i++) {
            int num = (int) (Math.random() * 100);
            ErrorLogStatistic errorLogStatistic =
                    new ErrorLogStatistic(0L,
                            CommonUtil.getMinuteStart((int) (System.currentTimeMillis() / 1000)),
                            "test" + num, "default_cell", "127.0.0." + num, num, "exception-" + num, 1, 0);
            errorLogStatisticService.saveStatistic(errorLogStatistic);
        }
        Thread.sleep(50000);

    }

    @Test
    public void tablePartition() {
        String date = "20170201";
        Date day = DateTimeUtil.parse(date, "yyyyMMdd");
        DateTime day_time = new DateTime(day.getTime());
        DateTime today = day_time;
        DateTime tomorrow = today;
        String partion_temp = "PARTITION p%s VALUES LESS THAN (%d) ENGINE = InnoDB,%n";
        StringBuffer buffer = new StringBuffer();
        for (int i = 1; i < 1000; i++) {
            tomorrow = today.plusDays(1);
            String str_day = DateTimeUtil.format(today.toDate(), "yyyyMMdd");
            int day_start = (int) (tomorrow.getMillis() / 1000);
            String par_tmp = String.format(partion_temp, str_day, day_start);
            buffer.append(par_tmp);
            today = tomorrow;
        }
        System.out.println(buffer.toString());
    }
}
