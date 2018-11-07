package com.meituan.mtrace.http.client;

import org.apache.http.annotation.ThreadSafe;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

/**
 * User: YangXuehua
 * Date: 14-1-8
 * Time: 下午4:12
 * 带连接池、且不保存cookie的httpclient
 * @since httpcomponents httpclient 4.2
 */
@ThreadSafe
public class PoolingNoCookieHttpClient extends DefaultNoCookieHttpClient {
    public PoolingNoCookieHttpClient(PoolingClientConnectionManager conman, org.apache.http.params.HttpParams params) {
        super(createClientConnectionManager(conman), params);
    }

    public PoolingNoCookieHttpClient(PoolingClientConnectionManager conman) {
        super(createClientConnectionManager(conman));
    }

    public PoolingNoCookieHttpClient(org.apache.http.params.HttpParams params) {
        super(createClientConnectionManager(null),params);
    }

    public PoolingNoCookieHttpClient() {
        super(createClientConnectionManager(null));
    }
    
    private static PoolingClientConnectionManager createClientConnectionManager(PoolingClientConnectionManager conman) {
        if(conman==null){
            conman = new PoolingClientConnectionManager();
        }
        return conman;
    }
}
