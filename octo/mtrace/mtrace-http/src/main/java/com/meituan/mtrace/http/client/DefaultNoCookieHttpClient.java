package com.meituan.mtrace.http.client;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.HttpContext;

/**
 * User: YangXuehua
 * Date: 14-1-8
 * Time: 下午4:12
 * @since httpcomponents httpclient 4.0
 */
@ThreadSafe
public class DefaultNoCookieHttpClient extends DefaultHttpClient {
    public DefaultNoCookieHttpClient(org.apache.http.conn.ClientConnectionManager conman, org.apache.http.params.HttpParams params) {
        super(conman, params);
    }

    public DefaultNoCookieHttpClient(org.apache.http.conn.ClientConnectionManager conman) {
        super(conman);
    }

    public DefaultNoCookieHttpClient(org.apache.http.params.HttpParams params) {
        super(params);
    }

    public DefaultNoCookieHttpClient() {
        super();

    }

    @Override
    public HttpContext createHttpContext() {
        HttpContext context = super.createHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
        return context;
    }
}
