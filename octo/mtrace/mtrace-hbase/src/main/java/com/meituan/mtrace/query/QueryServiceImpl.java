package com.meituan.mtrace.query;

import com.meituan.mtrace.common.Call;
import com.meituan.mtrace.common.Span;
import com.meituan.mtrace.hbase.CloudTableStoreService;
import com.meituan.mtrace.hbase.Env;
import com.meituan.mtrace.hbase.IStoreService;

import java.util.*;

/**
 * @author zhangzhitong
 * @created 9/23/15
 */
public class QueryServiceImpl implements IQueryService {
    private IStoreService storeService = new CloudTableStoreService();

    @Override
    public TraceCombo getTraceComboByTraceId(long traceId, Env env) {
        // 获取span信息
        List<Span> spans = storeService.getSpansByTraceId(traceId, env);
        if (spans == null || spans.isEmpty()) {
            return null;
        }
        TraceCombo traceCombo = new TraceCombo();
        TraceSummary tSummary = traceCombo.traceSummary;
        // 对span进行合并放入已spanId为key的map中
        Map<String, SpanTreeEntry> spansMap = mergeSpans(spans);

        for (Map.Entry<String, SpanTreeEntry> entry : spansMap.entrySet()) {
            // 调用链概况
            Call call = entry.getValue().getCall();
            if (call.isRoot()) {
                tSummary.setTraceId(call.getTraceId());
                tSummary.setDuration(call.getClientDuration());
                tSummary.setStartTs(call.getClientStart());
            }
            if (tSummary.getDepth() < call.getDepth()) {
                tSummary.setDepth(call.getDepth());
            }
            traceCombo.getTraceSummary().addServiceCount(entry.getValue().getCall().getServerEp().getAppKey());
            String parentId = Span.getParentSpanId(entry.getKey());
            if (parentId != null) {
                SpanTreeEntry parentSpanEntry = spansMap.get(parentId);
                if (parentSpanEntry != null) {
                    parentSpanEntry.addChild(entry.getValue());
                }
            }
            // 调用链数据
            traceCombo.addSpanTreeEntry(entry.getValue());
        }
        return traceCombo;
    }

    @Override
    public List<TraceOverView> getTracesByServiceSpanName(String name, String spanName, long ts, int limit, Env env) {

        List<List<Span>> traces = storeService.getSpansByServiceSpanName(name, spanName, ts, limit, env);
        if (traces == null || traces.isEmpty()) {
            return null;
        }
        List<TraceOverView> results = new LinkedList<TraceOverView>();
        for (List<Span> trace : traces) {
            if (trace != null && !trace.isEmpty()) {
                TraceOverView tov = new TraceOverView();
                Map<String, Call> callMap = new HashMap<String, Call>();
                for (Span span : trace) {
                    String spanId = span.getSpanId();
                    if (!callMap.containsKey(spanId)) {
                        callMap.put(spanId, new Call(span));
                    } else {
                        callMap.get(spanId).mergeSpan(span);
                    }
                }
                Map<String, ServItem> servInfos = new HashMap<String, ServItem>();
                for (Map.Entry<String, Call> entry : callMap.entrySet()) {
                    Call call = entry.getValue();
                    if (call.getSpanId().equals("0")) {
                        tov.setTraceId(call.getTraceId());
                        tov.setDuration(call.getClientDuration());
                        tov.setStartTs(call.getClientStart());
                        if (call.getServerEp() != null)
                            tov.setRootService(call.getServerEp().getAppKey());
                    }

                    if (call.getServerEp() != null && call.getServerEp().getAppKey() != null) {
                        String servName = call.getServerEp().getAppKey();
                        if (servInfos.containsKey(servName)) {
                            ServItem temp = servInfos.get(servName);
                            temp.count++;
                            temp.duration += call.getClientDuration();
                        } else {
                            ServItem si = new ServItem();
                            servInfos.put(servName, new ServItem(call.getClientDuration(), 1));
                        }
                    }
                }
                tov.setServices(servInfos);
                results.add(tov);
            }
        }
        return limit < results.size() ? results.subList(0, limit) : results;
    }

    private Map<String, SpanTreeEntry> mergeSpans(List<Span> spans) {
        if (spans == null || spans.isEmpty()) {
            return null;
        }
        Map<String, SpanTreeEntry> mergeSpans = new TreeMap<String, SpanTreeEntry>(new SpanIdComparator());
        /**
         * TODO need merge client server side spans
         */
        for (Span span : spans) {
            if (!mergeSpans.containsKey(span.getSpanId())) {
                mergeSpans.put(span.getSpanId(), new SpanTreeEntry(span));
            } else {
                mergeSpans.get(span.getSpanId()).getCall().mergeSpan(span);
            }
        }
        return mergeSpans;
    }
}
