package com.meituan.mtrace.query;

import com.meituan.mtrace.hbase.Env;

import java.util.List;

/**
 * @author zhangzhitong
 * @created 9/23/15
 */
public interface IQueryService {
    /**
     *
     * @param traceId traceId
     * @param env env prod, stage or test
     * @return TraceCombo 包含调用链路概况 ＋ 跳用链路数据
     */
    public TraceCombo getTraceComboByTraceId(long traceId, Env env);

    /**
     *
     * @param name appKey
     * @param spanName Class Name + "." + Method Name
     * @param ts timestamp
     * @param limit limit num
     * @param env env prod, stage or test
     * @return 查询服务列表
     */
    public List<TraceOverView> getTracesByServiceSpanName(String name, String spanName, long ts, int limit, Env env);
}
