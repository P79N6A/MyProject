package com.meituan.mtrace.query;

import java.util.Comparator;

/**
 * @author zhangzhitong
 * @created 9/23/15
 */

public class SpanIdComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        String[] r1 = s1.split("\\.");
        String[] r2 = s2.split("\\.");
        int i = 0;
        while(true) {
            if (i >= r1.length || i >= r2.length) {
                if (r1.length > r2.length) {
                    return 1;
                } else if (r1.length == r2.length) {
                    return 0;
                } else {
                    return -1;
                }
            }
            if (!r1[i].equals(r2[i])) {
                return (Integer.valueOf(r1[i]) > Integer.valueOf(r2[i])) ? 1 : -1;
            }
            ++i;
        }

    }
}
