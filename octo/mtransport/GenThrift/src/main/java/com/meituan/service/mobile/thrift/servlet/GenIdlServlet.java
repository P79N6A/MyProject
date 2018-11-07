package com.meituan.service.mobile.thrift.servlet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-2-4
 * Time: 下午4:38
 */
@Controller
@RequestMapping(value = "/genidl")
public class GenIdlServlet {

    //ajax请求idl文件内容
    @RequestMapping(value = "/idl", method = RequestMethod.GET)
    public String showIdl(){
        return "/genidl/idl";
    }

}
