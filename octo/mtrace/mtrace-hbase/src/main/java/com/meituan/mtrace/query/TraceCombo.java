package com.meituan.mtrace.query;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhangzhitong
 * @created 9/23/15
 */
public class TraceCombo {
    TraceSummary traceSummary = new TraceSummary();
    List<SpanTreeEntry> spanTreeEntries = new LinkedList<SpanTreeEntry>();

    public TraceSummary getTraceSummary() {
        return traceSummary;
    }

    public List<SpanTreeEntry> getSpanTreeEntries() {
        return spanTreeEntries;
    }

    public void addSpanTreeEntry(SpanTreeEntry entry) {
        spanTreeEntries.add(entry);
    }
}
