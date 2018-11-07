package com.meituan.mtrace.http.client;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

/**
 * User: YangXuehua
 * Date: 14-1-8
 * Time: 下午4:12
 * 带连接池、且不保存cookie的httpclient
 * @since httpcomponents httpclient 4.0
 */
@ThreadSafe
public class ThreadSafeNoCookieHttpClient extends DefaultNoCookieHttpClient {
    public ThreadSafeNoCookieHttpClient(ThreadSafeClientConnManager conman, org.apache.http.params.HttpParams params) {
        super(createClientConnectionManager(conman), params);
    }

    public ThreadSafeNoCookieHttpClient(ThreadSafeClientConnManager conman) {
        super(createClientConnectionManager(conman));
    }

    public ThreadSafeNoCookieHttpClient(org.apache.http.params.HttpParams params) {
        super(createClientConnectionManager(null),params);
    }

    public ThreadSafeNoCookieHttpClient() {
        super(createClientConnectionManager(null));
    }
    
    private static ThreadSafeClientConnManager createClientConnectionManager(ThreadSafeClientConnManager conman) {
        if(conman==null){
            conman = new ThreadSafeClientConnManager();
        }
        return conman;
    }
}
