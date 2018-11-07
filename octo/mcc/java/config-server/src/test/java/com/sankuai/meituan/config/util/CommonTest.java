package com.sankuai.meituan.config.util;

import com.sankuai.meituan.config.exception.MtConfigException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
/**
 * Created by lhmily on 07/22/2016.
 */
public class CommonTest {
    private static final Logger LOG = LoggerFactory.getLogger(CommonTest.class);

    @Test
    public void testAuth() {
        String appkey = "com.sankuai.octo.tmy";
        String token = "aaa";
        String authPath = ZKPathBuilder.newBuilder().appendSpace(appkey).toPath();
        String userSignature = AuthUtil.hmacSHA1(token, authPath);
        String serverSignature = AuthUtil.hmacSHA1(token, authPath);
        org.junit.Assert.assertEquals(userSignature, serverSignature);
    }

    @Test
    public void testIP() {
        org.junit.Assert.assertNotEquals("127.0.0.1", Common.getLocalIP());
        System.out.println(Common.getOctoUrl());
        System.out.println(Class.class.getClass().getResource("/").getPath());
        System.out.println(Class.class.getResource("/"));
    }

    @Test
    public void testsingletonExecute() {
        TaskUtil.singletonExecute(new Runnable() {
            @Override
            public void run() {
                System.out.println("singletonExecute");
            }
        });
    }

    @Test
    public void testNodeNameUtil() {
        org.springframework.util.Assert.isTrue(NodeNameUtil.checkSpacePath("com.sankuai.octo.tmy"), "节点名字出错,请联系MtConfig负责人");
        try {
            NodeNameUtil.checkPath("/com.sankuai.octo.tmy/prod");
        }catch (MtConfigException e) {
            LOG.warn("testNodeNameUtil failed,", e);
        }
        try {
            NodeNameUtil.checkEnv("prod");
        }catch (MtConfigException e) {
            LOG.warn("testNodeNameUtil failed,", e);
        }
        try {
            NodeNameUtil.checkAppkey("com.sankuai.octo.tmy");
        }catch (MtConfigException e) {
            LOG.warn("testNodeNameUtil failed,", e);
        }
        try {
            NodeNameUtil.getSpacePath("com.sankuai.octo.tmy", "/com.sankuai.octo.tmy/prod");
        }catch (MtConfigException e) {
            LOG.warn("testNodeNameUtil failed,", e);
        }
        try {
            NodeNameUtil.checkKey("com!$lala");
        }catch (MtConfigException e) {
            LOG.warn("testNodeNameUtil failed,", e);
        }
    }

    @Test
    public void testHttpUtil() {
        //getRealIp(HttpServletRequest request)
        System.out.println(HttpUtil.getRealIp(new HttpServletRequest() {
            @Override
            public String getAuthType() {
                return null;
            }

            @Override
            public Cookie[] getCookies() {
                return new Cookie[0];
            }

            @Override
            public long getDateHeader(String name) {
                return 0;
            }

            @Override
            public String getHeader(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                return null;
            }

            @Override
            public int getIntHeader(String name) {
                return 0;
            }

            @Override
            public String getMethod() {
                return null;
            }

            @Override
            public String getPathInfo() {
                return null;
            }

            @Override
            public String getPathTranslated() {
                return null;
            }

            @Override
            public String getContextPath() {
                return null;
            }

            @Override
            public String getQueryString() {
                return null;
            }

            @Override
            public String getRemoteUser() {
                return null;
            }

            @Override
            public boolean isUserInRole(String role) {
                return false;
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public String getRequestedSessionId() {
                return null;
            }

            @Override
            public String getRequestURI() {
                return null;
            }

            @Override
            public StringBuffer getRequestURL() {
                return null;
            }

            @Override
            public String getServletPath() {
                return null;
            }

            @Override
            public HttpSession getSession(boolean create) {
                return null;
            }

            @Override
            public HttpSession getSession() {
                return null;
            }

            @Override
            public boolean isRequestedSessionIdValid() {
                return false;
            }

            @Override
            public boolean isRequestedSessionIdFromCookie() {
                return false;
            }

            @Override
            public boolean isRequestedSessionIdFromURL() {
                return false;
            }

            @Override
            public boolean isRequestedSessionIdFromUrl() {
                return false;
            }

            @Override
            public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
                return false;
            }

            @Override
            public void login(String username, String password) throws ServletException {

            }

            @Override
            public void logout() throws ServletException {

            }

            @Override
            public Collection<Part> getParts() throws IOException, ServletException {
                return null;
            }

            @Override
            public Part getPart(String name) throws IOException, ServletException {
                return null;
            }

            @Override
            public Object getAttribute(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return null;
            }

            @Override
            public String getCharacterEncoding() {
                return null;
            }

            @Override
            public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

            }

            @Override
            public int getContentLength() {
                return 0;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public ServletInputStream getInputStream() throws IOException {
                return null;
            }

            @Override
            public String getParameter(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return null;
            }

            @Override
            public String[] getParameterValues(String name) {
                return new String[0];
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return null;
            }

            @Override
            public String getProtocol() {
                return null;
            }

            @Override
            public String getScheme() {
                return null;
            }

            @Override
            public String getServerName() {
                return null;
            }

            @Override
            public int getServerPort() {
                return 0;
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return null;
            }

            @Override
            public String getRemoteAddr() {
                return null;
            }

            @Override
            public String getRemoteHost() {
                return null;
            }

            @Override
            public void setAttribute(String name, Object o) {

            }

            @Override
            public void removeAttribute(String name) {

            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public Enumeration<Locale> getLocales() {
                return null;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String path) {
                return null;
            }

            @Override
            public String getRealPath(String path) {
                return null;
            }

            @Override
            public int getRemotePort() {
                return 0;
            }

            @Override
            public String getLocalName() {
                return null;
            }

            @Override
            public String getLocalAddr() {
                return null;
            }

            @Override
            public int getLocalPort() {
                return 0;
            }

            @Override
            public ServletContext getServletContext() {
                return null;
            }

            @Override
            public AsyncContext startAsync() throws IllegalStateException {
                return null;
            }

            @Override
            public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
                return null;
            }

            @Override
            public boolean isAsyncStarted() {
                return false;
            }

            @Override
            public boolean isAsyncSupported() {
                return false;
            }

            @Override
            public AsyncContext getAsyncContext() {
                return null;
            }

            @Override
            public DispatcherType getDispatcherType() {
                return null;
            }
        }));

        org.junit.Assert.assertNotNull(HttpUtil.ipToHost("10.4.245.3"));

        System.out.println(HttpUtil.get("http://config.inf.test.sankuai.com/api/zkserverlist"));
        try {
            HttpUtil.setJsonResponse(new HttpServletResponse() {
                @Override
                public void addCookie(Cookie cookie) {
                    System.out.println(cookie);
                }

                @Override
                public boolean containsHeader(String name) {
                    return false;
                }

                @Override
                public String encodeURL(String url) {
                    return null;
                }

                @Override
                public String encodeRedirectURL(String url) {
                    return null;
                }

                @Override
                public String encodeUrl(String url) {
                    return null;
                }

                @Override
                public String encodeRedirectUrl(String url) {
                    return null;
                }

                @Override
                public void sendError(int sc, String msg) throws IOException {

                }

                @Override
                public void sendError(int sc) throws IOException {

                }

                @Override
                public void sendRedirect(String location) throws IOException {

                }

                @Override
                public void setDateHeader(String name, long date) {

                }

                @Override
                public void addDateHeader(String name, long date) {

                }

                @Override
                public void setHeader(String name, String value) {

                }

                @Override
                public void addHeader(String name, String value) {

                }

                @Override
                public void setIntHeader(String name, int value) {

                }

                @Override
                public void addIntHeader(String name, int value) {

                }

                @Override
                public void setStatus(int sc) {

                }

                @Override
                public void setStatus(int sc, String sm) {

                }

                @Override
                public int getStatus() {
                    return 0;
                }

                @Override
                public String getHeader(String name) {
                    return null;
                }

                @Override
                public Collection<String> getHeaders(String name) {
                    return null;
                }

                @Override
                public Collection<String> getHeaderNames() {
                    return null;
                }

                @Override
                public String getCharacterEncoding() {
                    return null;
                }

                @Override
                public String getContentType() {
                    return null;
                }

                @Override
                public ServletOutputStream getOutputStream() throws IOException {
                    return null;
                }

                @Override
                public PrintWriter getWriter() throws IOException {
                    return null;
                }

                @Override
                public void setCharacterEncoding(String charset) {

                }

                @Override
                public void setContentLength(int len) {

                }

                @Override
                public void setContentType(String type) {

                }

                @Override
                public void setBufferSize(int size) {

                }

                @Override
                public int getBufferSize() {
                    return 0;
                }

                @Override
                public void flushBuffer() throws IOException {

                }

                @Override
                public void resetBuffer() {

                }

                @Override
                public boolean isCommitted() {
                    return false;
                }

                @Override
                public void reset() {

                }

                @Override
                public void setLocale(Locale loc) {

                }

                @Override
                public Locale getLocale() {
                    return null;
                }
            }, "fail", 200);
        } catch (Exception e) {
            LOG.warn("Response is null");
        }

    }
}
