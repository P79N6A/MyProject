package com.sankuai.octo.msgp.filter;

import com.google.common.base.Joiner;
import com.sankuai.meituan.common.util.NetUtil;
import com.sankuai.msgp.common.config.MsgpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

public class MsgpApiAuthFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(MsgpApiAuthFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("init MsgpApiAuthFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String auth = "";
        try {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            HttpServletResponse servletResponse = (HttpServletResponse) response;
            String requestURL = servletRequest.getRequestURL().toString();
            if (requestURL.contains("api/scanner/report") || requestURL.contains("/api/monitor/alive")) {
                chain.doFilter(request, response);
                return;
            }
            String host = servletRequest.getHeader("host");
            String uri = URLDecoder.decode(servletRequest.getRequestURI(), "UTF-8");
            String remoteIp = NetUtil.getRealIp(servletRequest);
            String method = servletRequest.getMethod();
            String dateStr = servletRequest.getHeader("Date");
            auth = servletRequest.getHeader("Authorization");
            if (StringUtils.isEmpty(auth)) {
                log.info(Joiner.on(", ").useForNull("null").join("non authorization", host, remoteIp, servletRequest.getRequestURL(), servletRequest.getQueryString(), method));
                servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                String clientId = "";
                try {
                    clientId = auth.substring(auth.indexOf(" ") + 1, auth.indexOf(":"));
                } catch (Exception e) {
                    log.info("MSGP API auth error! auth:" + auth, e);
                    if (StringUtils.isEmpty(clientId)) {
                        servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
                String secret = getSecret(clientId);
                if (!StringUtils.hasText(secret)) {
                    log.info(Joiner.on(", ").useForNull("null").join("client forbidden", host, clientId, auth, uri, method, dateStr));
                    servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                } else {
                    String auth2 = com.sankuai.meituan.common.security.AuthUtil.getAuthorization(uri, method, dateStr, clientId, secret);
                    if (auth.equals(auth2)) {
                        log.info(Joiner.on(", ").useForNull("null").join("auth ok", host, clientId, secret, auth, auth2, uri, method, dateStr));
                    } else {
                        log.info(Joiner.on(", ").useForNull("null").join("auth fail", host, clientId, secret, auth, auth2, uri, method, dateStr, remoteIp));
                        servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            log.error("MSGP API auth error! auth:" + auth, e);
        }
        chain.doFilter(request, response);
    }

    private String getSecret(String clientId) {
        Map<String, String> client_secret = MsgpConfig.getClientSecret("msgp=b535efb74b52d3d202cb96d2e239b454");
        if (client_secret.containsKey(clientId)) {
            return client_secret.get(clientId);
        } else {
            return "";
        }
    }

    public void destroy() {
        throw new UnsupportedOperationException();
    }

}