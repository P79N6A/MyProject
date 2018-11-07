package com.meituan.service.mobile.thrift.filter;

import com.meituan.service.mobile.thrift.param.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-2-5
 * Time: 下午5:10
 */
public class MISLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private Settings settings;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {

        //访问/auth, /login不拦截
        if (request.getRequestURI().equals("/auth") || request.getRequestURI().equals("/login"))
            return true;

        HttpSession session = request.getSession();

        //未登录
        if (session.getAttribute("login") == null || !(Boolean) session.getAttribute("login")) {
            //记录访问的地址，便于登陆成功跳转
            session.setAttribute("serverIP", settings.getServerIP());
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {

    }
}






