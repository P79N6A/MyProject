package com.sankuai.octo;

import org.joda.time.DateTime;
import org.junit.Test;

public class TimeTest {
    @Test
    public void testInit() {
        DateTime endTime = new DateTime();
        DateTime  startTime = endTime.withTimeAtStartOfDay();
        System.out.println(endTime);
        System.out.println(startTime);
    }
}
