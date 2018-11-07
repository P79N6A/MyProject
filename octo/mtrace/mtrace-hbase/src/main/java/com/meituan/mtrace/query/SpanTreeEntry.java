package com.meituan.mtrace.query;

import com.meituan.mtrace.common.Call;
import com.meituan.mtrace.common.Span;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhangzhitong
 * @created 9/24/15
 */
public class SpanTreeEntry {
    private Call call;
    private int depth;
    private List<SpanTreeEntry> children;

    public SpanTreeEntry(Span span) {
        this.call = new Call(span);
        this.depth = span.getDepth();
        this.children = new LinkedList<SpanTreeEntry>();
    }
    public void addChild(SpanTreeEntry child) {
       children.add(child);
    }

    public Call getCall() {
        return call;
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public List<SpanTreeEntry> getChildren() {
        return children;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
