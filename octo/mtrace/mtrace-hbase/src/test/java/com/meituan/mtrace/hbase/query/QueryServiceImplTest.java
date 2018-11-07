package com.meituan.mtrace.hbase.query;

import com.meituan.mtrace.hbase.Env;
import com.meituan.mtrace.query.*;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author zhangzhitong
 * @created 9/24/15
 */
public class QueryServiceImplTest extends TestCase {
    private IQueryService queryService = new QueryServiceImpl();

    public void testGetTraceComboByTraceId() {
        try {
            TraceCombo combo = queryService.getTraceComboByTraceId(-2810484237706037160L, Env.Prod);
            if (combo != null) {
                TraceSummary tSummary = combo.getTraceSummary();
                System.out.println(tSummary);
                for (SpanTreeEntry entry : combo.getSpanTreeEntries()) {
                    System.out.println(entry.getCall());
                }
            } else {
                System.out.println("Combo is null");
            }
        } catch (Exception e) {
        }
    }

    public void testGetTracesByServiceSpanName() {
        try {
            List<TraceOverView> results = queryService.getTracesByServiceSpanName("com.meituan.mtrace.test.MtraceTestA", "MtraceTestA.call", System.currentTimeMillis(), 10, Env.Prod);
            System.out.println(results);
        } catch (Exception e) {
        }

    }
}
