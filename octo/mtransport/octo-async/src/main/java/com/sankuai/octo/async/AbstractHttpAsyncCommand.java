package com.sankuai.octo.async;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.meituan.jmonitor.JMonitor;
import com.meituan.mtrace.Span;
import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;

/**
 * 统一的http异步请求命令
 * （目前只封装了Get方法,未来可以加入Post方法）
 * Created by wangchao23 on 2016-07-18.
 */
public abstract class AbstractHttpAsyncCommand<T> extends AbstractAsyncCommand<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpAsyncCommand.class);
    private String appkey;
    private String remoteAppkey;
    private Span span;

    public void setCallTree(String appkey, String remoteAppkey) {
        this.appkey = appkey;
        this.remoteAppkey = remoteAppkey;
    }

    @Override
    public T execute() throws ExecutionException {
        try {
            return queue().get();
        } catch (InterruptedException e) {
            throw new HttpCommandException(null, "http async get interrupted", e);
        }
    }

    public ListenableFuture<T> queue() {
        HttpRequestBase request = buildRequest();
        SettableFuture<T> future = SettableFuture.create();
        if (request != null) {
            FutureCallback<HttpResponse> callback = assembleCallBack(future, request, System.currentTimeMillis());
            storeSpan(request);
            getClient().execute(request, callback);
        } else {
            getFallback(future, new IllegalArgumentException("request is null..."));
        }
        return future;
    }

    private void storeSpan(HttpRequestBase request) {
        TraceParam param = new TraceParam(getClass().getName());
        Span serverSpan = Tracer.getServerTracer().getSpan();
        if (serverSpan != null) {
            param.setTraceId(serverSpan.getTraceId());
            param.setSpanId(serverSpan.getSpanId());
        }
        param.setLocalAppKey(appkey);
        param.setRemoteAppKey(this.remoteAppkey);
        if (request != null && request.getURI() != null) {
            param.setRemoteIp(request.getURI().getHost());
            param.setRemotePort(request.getURI().getPort());
        }
        param.setInfraName("octo-async");
        span = Tracer.clientSendAsync(param);
    }

    // 构建CallBack
    private FutureCallback<HttpResponse> assembleCallBack(final SettableFuture<T> future, final HttpRequestBase request,
                                                          final long startTime) {
        return new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                // 校验响应状态码
                StatusLine status = httpResponse.getStatusLine();
                // 暂时不区分状态码
                if (span != null) {
                    span.setStatus(Tracer.STATUS.SUCCESS);
                    Tracer.clientRecvAsync(span);
                }
                if (status.getStatusCode() >= 300) {
                    doHttpStatusError(request, future, startTime, status);
                } else {
                    // 响应码正确,处理响应
                    final HttpEntity entity = httpResponse.getEntity();
                    doSuccess(request, future, startTime, entity);
                }
            }

            @Override
            public void failed(Exception e) {
                if (e instanceof SocketTimeoutException) {
                    if (span != null) {
                        span.setStatus(Tracer.STATUS.TIMEOUT);
                        Tracer.clientRecvAsync(span);
                    }
                    doTimeout(request, future, startTime, (SocketTimeoutException) e);
                } else {
                    if (span != null) {
                        span.setStatus(Tracer.STATUS.EXCEPTION);
                        Tracer.clientRecvAsync(span);
                    }
                    future.setException(e);
                    doFailed(request, future, startTime, "async http failed callback", e);
                }
            }

            @Override
            public void cancelled() {
                if (span != null) {
                    span.setStatus(Tracer.STATUS.EXCEPTION);
                    Tracer.clientRecvAsync(span);
                }
                doCancelled(request, future, startTime);
            }
        };
    }

    // 处理成功响应
    private void doSuccess(final HttpRequestBase request, final SettableFuture<T> future, long startTime, final HttpEntity entity) {
        JMonitor.add(getClass().getName() + ".success", System.currentTimeMillis() - startTime);
        LOG.debug("success execute uri={} cost {}", request.getURI(), (System.currentTimeMillis() - startTime));
        asyncCommandExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    future.set(extractFromEntity(entity));
                } catch (Exception e) {
                    LOG.warn("failed to complete http async future. uri={}", request.getURI(), e);
                } finally {
                    EntityUtils.consumeQuietly(entity);
                }
            }
        });
    }

    /**
     * ================ 可供子类覆盖的方法（用于自定义命令行为） =================
     */
    // 异步客户端（子类必须实现），可同host共享实例
    public abstract HttpAsyncClient getClient();

    // 构造请求
    public abstract HttpRequestBase buildRequest();

    // 从响应中抽取结果对象（子类必须实现）
    public abstract T extractFromEntity(HttpEntity entity);

    // 处理请求失败
    private void doFailed(final HttpRequestBase request, final SettableFuture<T> future, long startTime,
                          String msg, Exception e) {
        reportErrorAndFallback(future, startTime
                , new HttpCommandException(request.getURI()
                , msg + ", cost " + (System.currentTimeMillis() - startTime)
                , e));
    }

    // 处理请求取消
    private void doCancelled(final HttpRequestBase request, final SettableFuture<T> future, long startTime) {
        reportErrorAndFallback(future, startTime
                , new HttpCommandException(request.getURI()
                , "http async request cancelled, cost " + (System.currentTimeMillis() - startTime)));
    }

    // 处理请求超时
    private void doTimeout(final HttpRequestBase request, final SettableFuture<T> future,
                           long startTime, SocketTimeoutException e) {
        reportErrorAndFallback(future, startTime
                , new HttpTimeoutGatewayException(request.getURI()
                , "http async request timeout, cost " + (System.currentTimeMillis() - startTime)
                , e));
    }

    // 处理响应状态码异常
    private void doHttpStatusError(final HttpRequestBase request, final SettableFuture<T> future,
                                   long startTime, StatusLine statusLine) {
        reportErrorAndFallback(future, startTime
                , new HttpResponseStatusException(request.getURI()
                , statusLine.getStatusCode()
                , statusLine.getReasonPhrase(),
                "http async request status error, cost " + (System.currentTimeMillis() - startTime)));
    }

    //上报错误信息到JMonitor,并进行真正的异常处理
    private void reportErrorAndFallback(SettableFuture<T> future, long startTime, Exception e) {
        JMonitor.add(getClass().getName() + ".error", System.currentTimeMillis() - startTime);
        getFallback(future, e);
    }

    //统一错误处理,默认行为是把异常设置到future中,子类可以覆盖此行为
    public void getFallback(SettableFuture<T> future, Exception e) {
        future.setException(e);
    }
}
