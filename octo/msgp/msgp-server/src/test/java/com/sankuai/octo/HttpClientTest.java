package com.sankuai.octo;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created by zava on 16/1/22.
 */
public class HttpClientTest {
    public static void main(String[] args) throws Exception
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(getCookie("skmtutc","anXjmKK6IqOWzqWlwKU2MYHkBKBCvAVjRPLo+XhY3cGN5AblSMZAI/qD8mcxKB0P3mf/Xw7XvfCVRNcjv4/xhA==-n1AR+pA8p31RiXQggI0gPqZfVnE="));
        cookieStore.addCookie(getCookie("SID", "570th7p1bt26ksolu59n401164"));
        cookieStore.addCookie(getCookie("misId","hanjiancheng"));
        cookieStore.addCookie(getCookie("misId.sig","R6t6zhbvKEhWCv0U1TVz6SuDRQk"));
        cookieStore.addCookie(getCookie("userId","64137"));
        cookieStore.addCookie(getCookie("userId.sig","rTbO-Kqsm5A1-tTXf40dHyv7PiY"));
        cookieStore.addCookie(getCookie("userName","%E9%9F%A9%E5%BB%BA%E6%88%90"));
        cookieStore.addCookie(getCookie("userName.sig","oDdXTbNSdFWkL8A51ojyIOMsgmM"));
        cookieStore.addCookie(getCookie("__mta","149770889.1445523886869.1445523886869.1452239177404.2"));
        cookieStore.addCookie(getCookie("_ga","GA1.2.1016736437.1445859649"));
        cookieStore.addCookie(getCookie("ssoid","816af6900c4942de94142e297cba9b63"));
        cookieStore.addCookie(getCookie("JSESSIONID","15hytutkzkqyswfrme8hmssj8"));
        httpClient.setCookieStore(cookieStore);
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,2000);//连接时间
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,2000);//数据传输时间
        // 创建Get方法实例
        HttpGet httpgets = new HttpGet("http://octo.sankuai.com/service/com.sankuai.inf.logCollector/provider?type=1&env=3&pageNo=1&pageSize=100");
        List<Cookie> cookies = cookieStore.getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            System.out.println("Local cookie: " + cookies.get(i));
        }
        try {
            HttpResponse response = httpClient.execute(httpgets);
            printResponse(response);
            String setCookie = response.getFirstHeader("Set-Cookie")
                    .getValue();
            System.out.println(setCookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     public static BasicClientCookie getCookie(String name,String value){
         BasicClientCookie cookie = new BasicClientCookie(name,value);
         cookie.setDomain("octo.sankuai.com");
         cookie.setVersion(0);
         cookie.setDomain("octo.sankuai.com");
         cookie.setPath("/");
         return cookie;
     }
    public static void printResponse(HttpResponse httpResponse)
            throws ParseException, IOException {
        // 获取响应消息实体
        HttpEntity entity = httpResponse.getEntity();
        // 响应状态
        System.out.println("status:" + httpResponse.getStatusLine());
        System.out.println("headers:");
        HeaderIterator iterator = httpResponse.headerIterator();
        while (iterator.hasNext()) {
            System.out.println("\t" + iterator.next());
        }
        // 判断响应实体是否为空
        if (entity != null) {
            String responseString = EntityUtils.toString(entity);
            System.out.println("response length:" + responseString.length());
            System.out.println("response content:"
                    + responseString.replace("\r\n", ""));
        }
    }

}
