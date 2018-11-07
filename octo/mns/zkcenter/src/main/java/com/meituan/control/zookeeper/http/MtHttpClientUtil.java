package com.meituan.control.zookeeper.http;

import com.meituan.control.zookeeper.util.StreamUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


/**
 * User: jinmengzhe
 * Date: 2015-05-25
 */
public class MtHttpClientUtil {
    private static final String host = "http://192.168.60.199:8082";
    private static String defaultContentType = "text/xml";
    private static ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager();

    static {
        connManager.setDefaultMaxPerRoute(50);
        connManager.setMaxTotal(500);
    }

    public static MtHttpResponse executeHttpRequest(MtHttpRequest request) throws Exception {
        HttpClient httpClient = getHttpClientWithPool();
        HttpResponse response = null;
        try {
            if (request.getMethod().equalsIgnoreCase("put")) {
                HttpPut put = constructHttpPut(request);
                response = httpClient.execute(put, new BasicHttpContext());
            } else if (request.getMethod().equalsIgnoreCase("post")) {
                HttpPost post = constructHttpPost(request);
                response = httpClient.execute(post, new BasicHttpContext());
            } else if (request.getMethod().equalsIgnoreCase("get")) {
                HttpGet get = constructHttpGet(request);
                response = httpClient.execute(get, new BasicHttpContext());
            } else if (request.getMethod().equalsIgnoreCase("delete")) {
                HttpDelete delete = constructHttpDelete(request);
                response = httpClient.execute(delete, new BasicHttpContext());
            } else {
                throw new Exception("unknow method=" + request.getMethod());
            }

            MtHttpResponse mtHttpResponse = transfer2MtHttpResponse(response);
            return mtHttpResponse;
        } catch(Exception e) {
            throw new IOException(e);
        } finally {
            if (null != response) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }

    public static void showResponse(MtHttpResponse mtHttpResponse) {
        // print response code
        System.out.println("[response code]: " + mtHttpResponse.getResultCode());

        // print response headers
        System.out.println("\n[response headers]: ");
        HashMap<String , String> headers = mtHttpResponse.getHeaders();
        for (Entry<String, String> entry : headers.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        // print response data
        if (mtHttpResponse.getContentData() != null) {
            System.out.println("\n[response data]: ");
            System.out.println(new String(mtHttpResponse.getContentData()));
        }
    }

    private static MtHttpResponse transfer2MtHttpResponse(HttpResponse response) throws Exception{
        MtHttpResponse mtHttpResponse = new MtHttpResponse();
        // resultcode
        int resultCode = response.getStatusLine().getStatusCode();
        mtHttpResponse.setResultCode(resultCode);
        // header
        Header[] headers = response.getAllHeaders();
        HashMap<String, String> headersMap = new HashMap<String, String>();
        for (Header header : headers) {
            headersMap.put(header.getName(), header.getValue());
        }
        mtHttpResponse.setHeaders(headersMap);
        // content
        try {
            InputStream inputStream = response.getEntity().getContent();
            if (inputStream != null) {
                byte[] data = StreamUtil.inputStream2ByteArray(inputStream);
                if (data.length > 0) {
                    mtHttpResponse.setContentData(data);
                }
            } else {
                System.out.println("no response data");
            }
            return mtHttpResponse;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            EntityUtils.consume(response.getEntity());
        }
    }

    private static HttpClient getHttpClientWithPool() {
        HttpClient client = new DefaultHttpClient(connManager);
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "meituan-zk-control");
        client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 3 * 1000);
        client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2 * 1000);
        return client;
    }

    private static HttpPut constructHttpPut(MtHttpRequest request) {
        HashMap<String, String> parameters = request.getParameters();
        String url = request.getEndpoint() + buildQueryString(parameters);

        HttpPut put = new HttpPut(url);
        HashMap<String, String> headers = request.getHeaders();
        for (Entry<String, String> entry: headers.entrySet()) {
            put.addHeader(entry.getKey(), entry.getValue());
        }
        byte[] contentData = request.getContentData();
        if (contentData != null && contentData.length > 0) {
            ByteArrayInputStream bufferInput = new ByteArrayInputStream(contentData);
            InputStreamEntity entity = new InputStreamEntity(bufferInput, contentData.length);
            entity.setContentType(defaultContentType);
            entity.setChunked(true);
            put.setEntity(entity);
        }
        return put;
    }

    private static HttpPost constructHttpPost(MtHttpRequest request) {
        HashMap<String, String> parameters = request.getParameters();
        String url = request.getEndpoint() + buildQueryString(parameters);

        HttpPost post = new HttpPost(url);
        HashMap<String, String> headers = request.getHeaders();
        for (Entry<String, String> entry: headers.entrySet()) {
            post.addHeader(entry.getKey(), entry.getValue());
        }
        byte[] contentData = request.getContentData();
        if (contentData != null && contentData.length > 0) {
            ByteArrayInputStream bufferInput = new ByteArrayInputStream(contentData);
            InputStreamEntity entity = new InputStreamEntity(bufferInput, contentData.length);
            entity.setContentType(defaultContentType);
            entity.setChunked(true);
            post.setEntity(entity);
        }
        return post;
    }

    private static HttpGet constructHttpGet(MtHttpRequest request) {
        HashMap<String, String> parameters = request.getParameters();
        String url = request.getEndpoint() + buildQueryString(parameters);

        HttpGet get = new HttpGet(url);
        HashMap<String, String> headers = request.getHeaders();
        for (Entry<String, String> entry: headers.entrySet()) {
            get.addHeader(entry.getKey(), entry.getValue());
        }
        return get;
    }

    private static HttpDelete constructHttpDelete(MtHttpRequest request) {
        HashMap<String, String> parameters = request.getParameters();
        String url = request.getEndpoint() + buildQueryString(parameters);

        HttpDelete delete = new HttpDelete(url);
        HashMap<String, String> headers = request.getHeaders();
        for (Entry<String, String> entry: headers.entrySet()) {
            delete.addHeader(entry.getKey(), entry.getValue());
        }

        return delete;
    }

    private static String buildQueryString(HashMap<String, String> parameters) {
        StringBuilder queryString = new StringBuilder();
        boolean isFirst = true;
        if (null != parameters) {
            Iterator<Entry<String, String>> iter = parameters.entrySet().iterator();
            while(iter.hasNext()) {
                Entry<String, String> entry = iter.next();
                String k = entry.getKey();
                String v = entry.getValue();
                if (isFirst) {
                    queryString.append("?");
                    isFirst = false;
                } else {
                    queryString.append("&");
                }
                if (null == v) {
                    queryString.append(k);
                } else {
                    queryString.append(k + "=" + v);
                }
            }
        }
        return queryString.toString();
    }
}
