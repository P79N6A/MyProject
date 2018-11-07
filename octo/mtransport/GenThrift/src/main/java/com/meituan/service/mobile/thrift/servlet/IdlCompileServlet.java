package com.meituan.service.mobile.thrift.servlet;

import com.meituan.service.mobile.thrift.model.GenThriftForm;
import com.meituan.service.mobile.thrift.model.IdlFile;
import com.meituan.service.mobile.thrift.param.Settings;
import com.meituan.service.mobile.thrift.result.GenThriftFlag;
import com.meituan.service.mobile.thrift.result.GenThriftResult;
import com.meituan.service.mobile.thrift.service.GenThriftService;
import com.meituan.service.mobile.thrift.utils.CommonFunc;
import com.meituan.service.mobile.thrift.utils.ShellProcess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-2-4
 * Time: 下午5:30
 */
@Controller
@RequestMapping(value = "/genidl")
public class IdlCompileServlet {

    private static final Logger logger = LoggerFactory.getLogger(IdlCompileServlet.class);

    private String idlFile;

    //.thrift默认文件名
    private static final String filename = "MyIdl";
    private String timestamp;
    private GenThriftService genThriftService;
    private GenThriftForm genThriftForm;
    private GenThriftResult result;

    @Autowired
    private Settings settings;

    @RequestMapping(value = "/compile", method = RequestMethod.GET)
    public String showCompile(){
        return "/genidl/compile";
    }

    @RequestMapping(value = "/cleanidl", method = RequestMethod.GET)
    public String cleanCompile(HttpSession session){
        IdlFile idlFile = (IdlFile)session.getAttribute("idlFile");
        idlFile.getNamespaceList().clear();
        idlFile.getConstantList().clear();
        idlFile.getServiceList().clear();
        idlFile.getStructList().clear();
        return "/genidl/compile";
    }

    @RequestMapping(value = "/compile", method = RequestMethod.POST)
    public String compileFormHandle(HttpSession session, HttpServletRequest req){

        //获取系统时间作为文件名
        timestamp = Long.toString(System.currentTimeMillis());
        logger.info("\n\n\n" + timestamp);

        //创建文件的根路径
        String rootDir = settings.getCommonDir() + timestamp + "/";
        String mkdir = "mkdir " + rootDir;
        ShellProcess.runShell(mkdir);

        idlFile = req.getParameter("idlFile");
        genThriftForm = new GenThriftForm();
        genThriftForm.setFilename(filename);
        //genThriftForm.setLanguage(req.getParameter("language"));
        genThriftForm.setThriftVersion(req.getParameter("thriftVersion"));


        //输出IDL文件
        CommonFunc.writeFile(rootDir + filename + ".thrift", idlFile);

        //调用业务逻辑处理函数
        genThriftService = new GenThriftService(genThriftForm, timestamp, settings);

        result = genThriftService.service();

        session.setAttribute("result", result);
        session.setAttribute("settings", settings);
        session.setAttribute("file", timestamp);

        if (GenThriftFlag.RUNERROR == result.getFlag())
            return "redirect:/genidl/compile";
        else
            return "redirect:/success";
    }




}
