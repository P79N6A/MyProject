package com.meituan.mtrace.http;

import com.meituan.mtrace.Endpoint;
import com.meituan.mtrace.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.restlet.Request;
import org.restlet.data.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author zhangxi
 * @created 14-1-6
 */
public class Helper {
    public static final String DEFAULT_URL_REGEX = "^/.*\\.(css|js|ico|gif|dmp|png|jpg)$";
    public static final String MTRACE_EXURL_REGEX = "mtrace.exurl.regex";
    public static final int FAIL = 10002;
    public static final int DEFAULT_HTTP_PORT = 80;

    private static final Logger LOG = LoggerFactory.getLogger(Helper.class);
    private static final String APP_KEY = "app.key";
    private static final String APP_HOST = "app.host";
    private static final String APP_PORT = "app.port";
    private static final String UNKNOWN = "unknownHost";
    private static Endpoint DEFAULT_LOCAL_ENDPOINT;
    private static String excludeUrlRegex;
    private static Pattern pattern;

    static {
        // init first
        String appkey = string(APP_KEY);
        String ip = getLocalIp();
        DEFAULT_LOCAL_ENDPOINT = new Endpoint(appkey, ip, intOr(APP_PORT, 0));

        if (excludeUrlRegex == null) {
            excludeUrlRegex = System.getProperty(Helper.MTRACE_EXURL_REGEX, Helper.DEFAULT_URL_REGEX);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("excludeUrlRegex = " + excludeUrlRegex);
        }
        try {
            pattern = Pattern.compile(excludeUrlRegex);
        } catch (Exception e) {
            LOG.warn("illegal url regex, " + excludeUrlRegex);
            pattern = Pattern.compile(Helper.DEFAULT_URL_REGEX);
        }
    }

    /**
     * init again from config
     *
     * @param appkey
     * @param port
     * @return
     */
    public static Endpoint initLocal(String appkey, int port) {
        String localKey = StringUtil.isBlank(appkey) ? string(APP_KEY) : appkey;
        String ip = getLocalIp();
        int localPort = port != 0 ? port : intOr(APP_PORT, 0);
        DEFAULT_LOCAL_ENDPOINT = new Endpoint(localKey, ip, localPort);
        return DEFAULT_LOCAL_ENDPOINT;
    }

    public static boolean matchExclude(String action) {
        return pattern.matcher(action).matches();
    }

    public static Endpoint getDefaultLocal() {
        return DEFAULT_LOCAL_ENDPOINT;
    }

    public static Endpoint getRemote(HttpServletRequest httpServletRequest) {
        String auth = httpServletRequest.getHeader("Authorization");
        String clientId = "";
        if (auth != null) {
            int start = auth.indexOf(' ') + 1;
            int end = auth.indexOf(':');
            if (start >= 0 && end >= 0) {
                clientId = auth.substring(start, end);
            }
        }
        String remoteIp = getRealIp(httpServletRequest);
        int remotePort = httpServletRequest.getRemotePort();
        return new Endpoint(clientId, remoteIp, remotePort);
    }

    public static String getField(HttpServletRequest request, String key) {
        String value = request.getHeader(key);
        if (StringUtil.isBlank(value)) {
            // TODO 存疑：不直接通过request.getParameter(key)读取参数，可能导致应用读取不到？
            // value = request.getParameter(key);
            value = getQueryParameter(request.getQueryString(), key);
            if (StringUtil.isBlank(value)) {
                value = getCookieValue(request, key);
            }
        }
        return value;
    }

    private static String getQueryParameter(String queryString, String key) {
        if (StringUtil.isBlank(queryString)) {
            return null;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            if (!StringUtil.isBlank(pair) && pair.startsWith(key)) {
                String[] array = pair.split("=");
                return array.length == 2 ? array[1] : null;
            }
        }
        return null;
    }

    public static String getField(Request request, String key) {
        Form requestHeaders = (Form) request.getAttributes().get("org.restlet.http.headers");
        String value = requestHeaders.getFirstValue(key);
        if (StringUtil.isBlank(value)) {
            value = request.getResourceRef().getQueryAsForm().getFirstValue(key);
            if (StringUtil.isBlank(value)) {
                value = request.getCookies().getFirstValue(key);
            }
        }
        return value;
    }

    public static Cookie getCookie(HttpServletRequest request, String cookieName) {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie cookie = getCookie(request, cookieName);
        return cookie == null ? null : cookie.getValue().trim();
    }

    private static String defaultHost() {
        if (!StringUtil.isBlank(string(APP_HOST))) {
            return string(APP_HOST, "");
        } else {
            return getLocalHostName();
        }
    }

    private static String getRealIp(HttpServletRequest request) {
        String ip = head(request, "X-Real-IP");
        if (ip != null && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = head(request, "X-Forwarded-For");
        if (ip != null) {
            int index = ip.indexOf(',');
            // 多次反向代理后会有多个IP值，第一个为真实IP。
            return (index >= 0) ? ip.substring(0, index) : ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip == null ? "unkown" : ip;
    }

    private static String head(HttpServletRequest req, String s) {
        return req.getHeader(s);
    }

    public static String getSpanname(ProceedingJoinPoint point) {
        StringBuilder sb = new StringBuilder(point.getTarget().getClass().getSimpleName());
        sb.append('.');
        Signature signature = point.getSignature();
        if (signature instanceof MethodSignature) {
            Method method = ((MethodSignature) signature).getMethod();
            sb.append(method.getName());
        }
        return sb.toString();
    }

    public static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    public static String getLocalIp() {
        Enumeration<NetworkInterface> networkInterface;
        try {
            networkInterface = NetworkInterface.getNetworkInterfaces();
            while (networkInterface.hasMoreElements()) {
                NetworkInterface ni = networkInterface.nextElement();
                Enumeration<InetAddress> inetAddress = ni.getInetAddresses();
                while (inetAddress.hasMoreElements()) {
                    InetAddress ia = inetAddress.nextElement();
                    if (ia instanceof Inet6Address)
                        continue; // ignore ipv6
                    String thisIp = ia.getHostAddress();
                    if (thisIp != null && !ia.isLoopbackAddress() && !thisIp.contains(":") && !thisIp.startsWith("127.0.")) {
                        return thisIp;
                    }
                }
            }
        } catch (SocketException e) {
            LOG.warn("can't getLocalIp", e);
        }
        return "127.0.0.1";
    }

    /**
     * 内网ip
     * 10.0.0.0~10.255.255.255
     * 192.168.0.0~192.168.255.255
     * 169.254.0.0~169.254.255.255
     * 172.16.0.0~172.31.255.255  性能考虑，先忽略这个网段
     *
     * @param ip
     * @return
     */
    private static boolean isIntranetIpv4(String ip) {
        if (ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("169.254.")) {
            return true;
        }
        return false;
    }

    private static int intOr(String key, int defaultValue) {
        String value = string(key);
        try {
            return StringUtil.isBlank(value) ? defaultValue : Integer.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String string(String key) {
        return System.getProperty(key, "");
    }

    private static String string(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}

