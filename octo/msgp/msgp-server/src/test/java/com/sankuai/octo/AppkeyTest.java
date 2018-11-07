package com.sankuai.octo;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zava on 16/1/21.
 */
public class AppkeyTest {
    static Pattern pattern = Pattern.compile("^(com.(sankuai|meituan))[.]*.*");
    @Test
    public void testAppkey() {
        String uri = "/service/com.sankuai.d.tsta2/a";
        String arr[] = uri.split("\\/");
        for(int i=0;i<arr.length;i++){
            Matcher matcher = pattern.matcher(arr[i]);
            if (matcher.matches()) {
                System.out.print(arr[i]);
            }
        }
    }
    @Test
    public void testTime() {
        for(int i =0;i<5;i++){
         Long[] times = getTime(i);
            System.out.println(times[0]+","+times[1]);

        }
    }
    private Long[] getTime(int count){
        Long[] times = new Long[2];
        long now = System.currentTimeMillis();
        times[0] = now - (5-count)*60000;
        times[1] = now - (4-count)*60000;
        return times;
    }
}
