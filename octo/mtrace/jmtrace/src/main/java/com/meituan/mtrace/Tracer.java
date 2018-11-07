package com.meituan.mtrace;

public class Tracer {

    public enum STATUS {
        SUCCESS(0), TIMEOUT(1), EXCEPTION(2), DROP(3);

        private int value;

        STATUS(int i) {
            this.value = i;
        }

        public int getValue() {
            return value;
        }
    }

    protected Tracer() {}

    /**
     * 获取traceId,必须在server处理请求的线程中调用, 否则会返回null
     *
     * @return traceId string
     */
    public static String id() {
        return ServerTracer.getInstance().getTraceId();
    }

    public static String getRemoteAppKey() {
        return ServerTracer.getInstance().getRemoteAppKey();
    }

    /**
     * 获取appKey,必须在server处理请求的线程中调用, 否则会返回null
     * @return appkey string
     */
    public static String getAppKey() {
        return ServerTracer.getInstance().getAppKey();
    }


    public static ClientTracer getClientTracer() {
        return ClientTracer.getInstance();
    }

    public static ServerTracer getServerTracer() {
        return ServerTracer.getInstance();
    }

    /**
     * Client Send : 作为调用方在发起调用前埋点，表示该次调用的client端追踪开始
     *
     * @param traceParam 追踪参数
     * @return 调用上下文
     */
    public static Span clientSend(TraceParam traceParam) {
        ClientTracer clientTracer = ClientTracer.getInstance();
        return clientTracer.record(traceParam);
    }

    public static Span clientSendAsync(TraceParam traceParam) {
        ClientTracer clientTracer = ClientTracer.getInstance();
        return clientTracer.recordAsync(traceParam);
    }

    /**
     * Client Recieve : 作为调用方在接收调用返回结果后埋点，表示该次调用的client端追踪结束
     */
    public static Span clientRecv() {
        return ClientTracer.getInstance().flush();
    }

    public static Span clientRecvAsync(Span span) {
        return ClientTracer.getInstance().flushAsync(span);
    }

    /**
     * Server Recieve : 作为被调用方在接收调用请求后埋点，表示该次调用的server端追踪开始
     *
     * @param traceParam 追踪参数
     * @return 调用上下文
     */
    public static Span serverRecv(TraceParam traceParam) {
        ServerTracer serverTracer = ServerTracer.getInstance();
        return serverTracer.record(traceParam);
    }

    /**
     * Server Send : 作为被调用方在返回调用结果前埋点，表示该次调用的server端追踪结束
     */
    public static Span serverSend() {
        return ServerTracer.getInstance().flush();
    }

    public static void addAnnotation(String key, String value) {
        getServerTracer().addAnnotation(key, value);
    }
    // ----------------Deprecated-------------- //
    @Deprecated
    public static void setDebug(boolean debug) {
    }

    @Deprecated
    public static void setThreshold(String appkey, String spanname, int threshold) {
        AbstractTracer.getSlowQueryFilter().setThreshold(appkey, spanname, threshold);
    }

    @Deprecated
    public static void setThreshold(String appkey, int threshold) {
        AbstractTracer.getSlowQueryFilter().setThreshold(appkey, threshold);
    }

    @Deprecated
    public static void recordDropRequest(String appkey, String remoteAppkey, String spanname) {
        //AbstractTracer.getDropCounter().countDrop(appkey, remoteAppkey, spanname);
    }


}
