package com.sankuai.octo.async;

import java.net.URI;

/**
 * http超时异常
 * Created by wangchao23 on 2016-07-18.
 */
public class HttpTimeoutGatewayException extends HttpCommandException {
    private long costTime;  //实际消耗的时间
    private long thresholdTime; //设定的超时阈值

    public HttpTimeoutGatewayException(URI uri, String message, Throwable cause) {
        super(uri, message, cause);
    }

    public HttpTimeoutGatewayException(URI uri, String message, Throwable cause,
                                       long costTime) {
        super(uri, message, cause);
        this.costTime = costTime;
    }

    public HttpTimeoutGatewayException(URI uri, String message, Throwable cause,
                                       long costTime, long thresholdTime) {
        super(uri, message, cause);
        this.costTime = costTime;
        this.thresholdTime = thresholdTime;
    }

    @Override
    public String getMessage() {
        if (costTime == 0) {
            return super.getMessage();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(super.getMessage())
                .append(". costTime=")
                .append(costTime);
        if (thresholdTime != 0) {
            builder.append(" thresholdTime=")
                    .append(thresholdTime);
        }
        return builder.toString();
    }
}
