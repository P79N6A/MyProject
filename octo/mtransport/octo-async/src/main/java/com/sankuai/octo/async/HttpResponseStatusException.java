package com.sankuai.octo.async;

import java.net.URI;

/**
 * http响应状态异常
 * Created by wangchao23 on 2016-07-18.
 */
public class HttpResponseStatusException extends HttpCommandException {
    private int statusCode; //状态码
    private String reasonPhrase;    //原因说明

    public HttpResponseStatusException(URI uri, int statusCode,
                                       String reasonPhrase, String message) {
        super(uri, message);
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    @Override
    public String getMessage() {
        return super.getMessage()
                + ". statusCode=" + statusCode
                + " reasonPhrase=" + reasonPhrase;
    }
}
