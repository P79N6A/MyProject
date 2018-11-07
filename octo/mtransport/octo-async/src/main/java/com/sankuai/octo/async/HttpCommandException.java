package com.sankuai.octo.async;

import java.net.URI;

/**
 * http请求异常
 * Created by wangchao23 on 2016-07-18.
 */
public class HttpCommandException extends RuntimeException {
    private URI uri;

    public HttpCommandException(URI uri, String message) {
        super(message);
        this.uri = uri;
    }

    public HttpCommandException(URI uri, String message, Throwable cause) {
        super(message, cause);
        this.uri = uri;
    }

    @Override
    public String getMessage() {
        String msg = super.getMessage();
        Throwable cause = getCause();
        if (cause != null) {
            msg += ". Cause by "
                    + cause.getClass().getSimpleName()
                    + ": "
                    + cause.getMessage();
        }
        if (uri != null) {
            msg += ". uri="
                    + uri.getScheme()
                    + "://"
                    + uri.getHost()
                    + uri.getPath();
        }
        return msg;
    }
}
