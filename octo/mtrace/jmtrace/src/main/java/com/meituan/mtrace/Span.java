package com.meituan.mtrace;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Span 调用链数据和性能数据的最小单元
 * Span 分为client span, server span, client span + server span = one rpc call
 */

public class Span {

    public enum SIDE {

        CLIENT(0), SERVER(1);

        private int value;

        SIDE(int i) {
            this.value = i;
        }

        public int getValue() {
            return value;
        }
    }

    private final String traceId;  // 一次请求的全局唯一id
    private final String spanId;   // 调用关系id, 标识一次trace中的某一次rpc调用, 签名方式命名, EG : 0, 0.1, 0.2, 01.1
    private AtomicInteger currentSpanNum = new AtomicInteger(0);    // 标识分配到了第几个span, 用于生成调用下游的spanId
    private String spanName = "";       // 调用接口的Class Name + "." + Method Name
    private final Endpoint local = new Endpoint("", "", 0);        // 本地的appKey, ip, port 信息
    private final Endpoint remote = new Endpoint("", "", 0);       // 远端的appKey, ip, port 信息
    private long start;            // 记录开始的时间
    private long end;              // 记录结束的时间
    private SIDE type;            // 是client span 还是 server span, 0 client, 1 server
    private Tracer.STATUS status = Tracer.STATUS.SUCCESS;            // 服务返回状态
    private boolean debug = false;           // 调用链数据采样字段, 1 采样, 0 不采样. 虽然是short类型，但是上层使用都是用成bool
    private boolean sample = false;        // 性能数据采样字段 null未标识，true采样，false 不采样, 1.0.6 版本之前使用，后续版本废弃
    private boolean async = false;
    private String infraName;      // 中间件名
    private String version;        // 中间件版本
    private int packageSize;       // 传输的包大小
    private final List<Annotation> annotations = new LinkedList<Annotation>();        //
    private final List<KVAnnotation> kvAnnotations = new LinkedList<KVAnnotation>();    //
    private String extend = "";    // 拓展字段

    /**
     * Span Constructor
     *
     * @param traceId 作为server接收请求时，从网络字节流中收到的TraceId
     * @param spanId  作为server接收请求时，从网络字节流中收到的SpanId
     */
    public Span(String traceId, String spanId, String spanName) {
        Validate.notBlank(traceId, "TraceId can't be null");
        this.traceId = traceId;
        this.spanId = Validate.isBlank(spanId) ? String.valueOf(0) : spanId;
        this.spanName = spanName;
    }

    /**
     * Span Constructor, traceId 生成为uuid 高64位与低64位异或
     */
    public Span(String spanName) {
        UUID uuid = UUID.randomUUID();
        long tid = uuid.getLeastSignificantBits() ^ uuid.getMostSignificantBits();
        this.traceId = String.valueOf(tid);
        this.spanId = String.valueOf(0);
        this.spanName = spanName;
    }

    public void setLocal(String appKey, String ip, int port) {
        if (Validate.notBlank(appKey)) {
            this.local.setAppkey(appKey);
        }
        if (Validate.notBlank(ip)) {
            this.local.setHost(ip);
        }
        this.local.setPort(port);
    }

    public String getLocalAppKey() {
        return local.getAppkey();
    }

    public String getLocalHost() {
        return local.getHost();
    }

    public int getLocalPort() {
        return local.getPort();
    }

    public void setRemote(String appKey, String ip, int port) {
        if (Validate.notBlank(appKey)) {
            this.remote.setAppkey(appKey);
        }
        if (Validate.notBlank(ip)) {
            this.remote.setHost(ip);
        }
        this.remote.setPort(port);
    }

    public String getRomoteAppKey() {
        return remote.getAppkey();
    }

    public String getRemoteHost() {
        return remote.getHost();
    }

    public int getRemotePort() {
        return remote.getPort();
    }

    public void setStatus(Tracer.STATUS status) {
        this.status = status;
    }

    public Tracer.STATUS getStatusCode() {
        return status;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        if (Validate.notBlank(spanName)) {
            this.spanName = spanName;
        }
    }


    private int nextSpanNum() {
        return currentSpanNum.incrementAndGet();
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        if (debug != null) {
            this.debug = debug;
        }
    }

    public boolean isSample() {
        return sample;
    }

    public void setSample(Boolean sample) {
        if (sample != null) {
            this.sample = sample;
        }
    }

    public void setType(SIDE type) {
        this.type = type;
    }

    public SIDE getType() {
        return type;
    }

    Span genNextSpan(String spanName) {
        final String clientSpanId = spanId + "." + nextSpanNum();
        Span next = new Span(traceId, clientSpanId, spanName);
        return next;
    }

    public int getCost() {
        return (int) (end - start);
    }

    @Deprecated
    public int getStatus() {
        return status.getValue();
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStart() {
        return start;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        if (Validate.notBlank(extend)) {
            this.extend = extend;
        }
    }

    public String getInfraName() {
        return infraName;
    }

    public void setInfraName(String infraName) {
        this.infraName = infraName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(int packageSize) {
        this.packageSize = packageSize;
    }

    public void addKvAnnotation(KVAnnotation kvAnnotation) {
        kvAnnotations.add(kvAnnotation);
    }

    public List<KVAnnotation> getKvAnnotations() {
        return kvAnnotations;
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Endpoint getLocal() {
        return local;
    }

    public Endpoint getRemote() {
        return remote;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder("Span(");
        sb.append("traceId:");
        sb.append(traceId);
        sb.append(", spanId:");
        sb.append(spanId);
        sb.append(", spanName:");
        sb.append(spanName);
        sb.append(", localAppKey:");
        sb.append(local);
        sb.append(", remoteAppKey:");
        sb.append(remote);
        sb.append(", start:");
        sb.append(start);
        sb.append(", end:");
        sb.append(end);
        sb.append(", type:");
        sb.append(type);
        sb.append(", status:");
        sb.append(status);
        sb.append(", debug:");
        sb.append(debug);
        sb.append(", sample:");
        sb.append(sample);
        sb.append(", infraName:");
        sb.append(infraName);
        sb.append(", version:");
        sb.append(version);
        sb.append(", packageSize:");
        sb.append(packageSize);
        sb.append(", Annotations:");
        sb.append(annotations);
        sb.append(", kvAnnotations:");
        sb.append(kvAnnotations);
        sb.append(")");
        return sb.toString();
    }

    @Deprecated
    void setServerReceived(Endpoint remote) {
        this.start = System.currentTimeMillis();
        this.type = SIDE.SERVER;
        this.remote.copy(remote);
    }

    @Deprecated
    void setServerSend(int status) {
        this.end = System.currentTimeMillis();
    }

    @Deprecated
    void setClientSent(Endpoint remote) {
        this.start = System.currentTimeMillis();
        this.type = SIDE.CLIENT;
        this.remote.copy(remote);
    }

    @Deprecated
    void setClientReceived(int status) {
        this.end = System.currentTimeMillis();
    }

    @Deprecated
    void setLocal(Endpoint local) {
        this.local.copy(local);
    }

    @Deprecated
    public void setDebug(short debug) {
        this.debug = (debug == 1);
    }

    @Deprecated
    public Boolean getSample() {
        return isSample();
    }

    @Deprecated
    public short getDebug() {
        return (short) (isDebug() ? 1 : 0);
    }

    @Deprecated
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("traceId").append("=").append(traceId);
        sb.append(" spanId").append("=").append(spanId);
        sb.append(" spanName").append("=").append(spanName);
        sb.append(" localAppkey").append("=").append(local != null ? local.getAppkey() : "");
        sb.append(" localHost").append("=").append(local != null ? local.getHost() : "");
        sb.append(" localPort").append("=").append(local != null ? local.getPort() : "");
        sb.append(" remoteAppkey").append("=").append(remote != null ? remote.getAppkey() : "");
        sb.append(" remoteHost").append("=").append(remote != null ? remote.getHost() : "");
        sb.append(" remotePort").append("=").append(remote != null ? remote.getPort() : "");
        sb.append(" start").append("=").append(start);
        sb.append(" cost").append("=").append(end - start);
        sb.append(" type").append("=").append(type);
        sb.append(" status").append("=").append(status);
        sb.append(" count").append("=").append(1);
        sb.append(" debug").append("=").append(debug);
        sb.append(" extend").append("=").append(extend);
        if (Validate.notBlank(extend)) {
            this.extend = extend;
        }
        return sb.toString();
    }
}
