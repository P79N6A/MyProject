package com.meituan.control.zookeeper.http;

/**
 * User: jinmengzhe
 * Date: 2015-05-25
 */
import java.util.HashMap;


public class MtHttpResponse {
    private int resultCode = -100;
    private HashMap<String, String> headers = new HashMap<String, String>();
    private byte[] contentData = null;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getContentData() {
        return contentData;
    }

    public void setContentData(byte[] contentData) {
        this.contentData = contentData;
    }
}