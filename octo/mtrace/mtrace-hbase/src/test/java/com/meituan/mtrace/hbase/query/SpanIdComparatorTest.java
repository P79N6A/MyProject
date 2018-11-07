package com.meituan.mtrace.hbase.query;

import com.meituan.mtrace.query.SpanIdComparator;
import junit.framework.TestCase;

import java.util.*;

/**
 * @author zhangzhitong
 * @created 9/25/15
 */
public class SpanIdComparatorTest extends TestCase {

    private void compareSpanIds(String[] spanIds) {
        List<String> spanList = Arrays.asList(spanIds);
        System.out.println(spanList);
        Collections.sort(spanList, new SpanIdComparator());
        for (String span : spanList) {
            System.out.println(span);
        }

    }

    public void testComparetor1() {
        System.out.println(this.getClass().getName() + " " + this.getName());
        String[] spanIds = {"0", "0.2.1", "0.2", "0.1", "0.2.2"};
        compareSpanIds(spanIds);
    }

    public void testComparetor2() {
        System.out.println(this.getClass().getName() + " " + this.getName());
        String[] spanIds = {"0", "0.11.1", "0.1.1", "0.2", "0.1.31", "0.1.1.1.1.1", "0.2.1234"};
        compareSpanIds(spanIds);
    }
}
