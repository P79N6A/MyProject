package com.meituan.mtrace;

public interface ITracer {

    String getTraceId();

    /**
     * Trace 记录开始
     * @param param 服务参数信息
     */
    public Span record(TraceParam param);

    /**
     * Trace 数据归档
     * @param status 服务返回状态
     */
    public Span flush();

    public void clearCurrentSpan();

    /**
     * 自定义埋点数据
     */
    public void addAnnotation(String value);
    public void addAnnotation(String value, int duration);
    public void addAnnotation(String key, String value);

}
