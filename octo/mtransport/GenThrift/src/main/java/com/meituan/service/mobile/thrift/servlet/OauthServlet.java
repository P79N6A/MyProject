package com.meituan.service.mobile.thrift.servlet;

import com.alibaba.fastjson.JSON;
import com.meituan.service.mobile.thrift.constant.Consts;
import com.meituan.service.mobile.thrift.model.OauthInfo;
import com.meituan.service.mobile.thrift.model.OauthResult;
import com.meituan.service.mobile.thrift.param.Settings;
import com.meituan.service.mobile.thrift.utils.CommonFunc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-5-20
 * Time: 下午8:18
 */
@Controller
public class OauthServlet {

    @Autowired
    private Settings settings;

    @RequestMapping(value = "/getCode", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public String oauthGet(@RequestParam(value = "code", required = false)String code, HttpSession session) {
        String content = null;
        if(code != null) {
            String url = settings.getOauthUrl() + "/oauth2/accesstoken?grant_type=authorization_code" +
                    "&code=" + code +
                    "&client_id=" + Consts.CLIENT_ID +
                    "&redirect_uri=" + settings.getServerIP() +
                    "&client_secret=" + settings.getClientSecret();
            content = CommonFunc.httpRequest(url);
            OauthResult oauthResult = JSON.parseObject(content, OauthResult.class);
            OauthInfo oauthInfo = oauthResult.getData();
            session.setAttribute("appkeys", oauthInfo.getScope());
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/oauth", method = RequestMethod.GET)
    public String showOauth() {
        StringBuffer sb = new StringBuffer();
        sb.append("redirect:");
        sb.append(settings.getOauthUrl());
        sb.append("/oauth2/authorize?response_type=code&client_id=");
        sb.append(Consts.CLIENT_ID);
        sb.append("&redirect_uri=");
        sb.append(settings.getServerIP());
        sb.append("/getCode");
        return sb.toString();
    }

}
