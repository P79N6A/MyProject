package com.meituan.service.mobile.thrift.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meituan.service.mobile.thrift.model.MISInfo;
import com.meituan.service.mobile.thrift.param.Settings;
import com.meituan.service.mobile.thrift.utils.CommonFunc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-2-5
 * Time: 下午2:11
 */
@Controller
public class MISLoginServlet {

    @Autowired
    private Settings settings;

    private static final String AUTH_URL = "http://api.sso-in.sankuai.com/api/session/";
    private static final String LOGOUT_URL = "http://api.sso-in.sankuai.com/api/logout/";
    private static final String CLIENT_ID = "genthrift";
    private static final String CLIENT_SECRET = "433f03a58799c632b30e1a7f552912b9";

    @RequestMapping(value = "/auth")
    public String authHandle(HttpServletRequest req, HttpSession session) {

        if (req.getParameter("SID") == null)
            return "login";
        String SID = req.getParameter("SID");
        session.setAttribute("SID", SID);

        //{"data":{"login":"gaosheng","name":"高升","id":40127}}
        String response = CommonFunc.getWithBA(AUTH_URL + SID, CLIENT_ID, CLIENT_SECRET);
        //解析json
        JSONObject jsonObject = JSON.parseObject(response);
        String json = jsonObject.getJSONObject("data").toJSONString();
        MISInfo misInfo = JSON.parseObject(json, MISInfo.class);

        if (null == misInfo.getLogin() || null == misInfo.getName() || 0 == misInfo.getId())
            return "login";

        session.setAttribute("misInfo", misInfo);
        session.setAttribute("login", new Boolean(true));
        return "redirect:/";

    }

    @RequestMapping(value = "/logout")
    public String logoutHandle(HttpServletRequest req, HttpSession session) {
        if (session.getAttribute("SID") != null) {

            String SID = (String) session.getAttribute("SID");

            //{ data : "ok" }
            CommonFunc.httpRequest(LOGOUT_URL + SID);
            session.setAttribute("misInfo", null);
            session.setAttribute("login", new Boolean(false));

        }
        return "redirect:/";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getResult() {
        return "redirect:https://sso.sankuai.com/auth?service="+ settings.getServerIP() +"/auth";
    }

}
