package com.sankuai.meituan.config.interceptorfilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by liangchen on 2017/9/28.
 */
public class HttpAccessLogInterceptor extends HandlerInterceptorAdapter {
    public static final Logger LOGGER = LoggerFactory.getLogger(HttpAccessLogInterceptor.class);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        if (null != request) {
            LOGGER.info("client ip = {}, server port = {}, url = {}", request.getRemoteAddr(), request.getServerPort(),request.getRequestURI());
        }
    }
}
