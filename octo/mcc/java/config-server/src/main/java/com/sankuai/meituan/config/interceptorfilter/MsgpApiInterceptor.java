package com.sankuai.meituan.config.interceptorfilter;

import com.sankuai.meituan.config.util.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 前端的服务api的拦截器,因为octo需要跨域访问,所以这些api要么加一个特别的token,要么走sso
 */
public class MsgpApiInterceptor extends HandlerInterceptorAdapter {
    public static final String FE_SERVER_TOKEN = "X-Fe-Server-Token";

    private String feServerToken = "ce4d91de46edf6c8e767189e4fa7a91e";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (! auth(request)) {
            HttpUtil.setJsonResponse(response, "认证不通过,无法调用mtconfig的服务", HttpServletResponse.SC_OK);
            return false;
        }

        return true;
    }

    private boolean auth(HttpServletRequest request) {
        String reqFeServerToken = request.getHeader(FE_SERVER_TOKEN);
        return StringUtils.equals(this.feServerToken,reqFeServerToken);
    }
}
