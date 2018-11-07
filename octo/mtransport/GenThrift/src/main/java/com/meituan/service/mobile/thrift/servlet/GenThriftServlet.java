package com.meituan.service.mobile.thrift.servlet;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.meituan.jmonitor.JMonitor;
import com.meituan.service.mobile.thrift.manager.IdlVersionManager;
import com.meituan.service.mobile.thrift.model.GenThriftForm;
import com.meituan.service.mobile.thrift.model.MISInfo;
import com.meituan.service.mobile.thrift.param.Settings;
import com.meituan.service.mobile.thrift.result.GenThriftFlag;
import com.meituan.service.mobile.thrift.result.GenThriftResult;
import com.meituan.service.mobile.thrift.service.GenThriftService;
import com.meituan.service.mobile.thrift.service.IdlVersionService;
import com.meituan.service.mobile.thrift.utils.CommonFunc;
import com.meituan.service.mobile.thrift.utils.ShellProcess;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-1-30
 * Time: 上午11:32
 */

@Controller
public class GenThriftServlet {

    private static final Logger logger = LoggerFactory.getLogger(GenThriftServlet.class);

    @Autowired
    private Settings settings;

    @Autowired
    private IdlVersionManager idlVersionManager;

    private String timestamp;
    private GenThriftService genThriftService;
    private GenThriftResult result;

    private IdlVersionService idlVersionService;
    private String appkey;
    private String filename;
    private String uid;


    //设置首页显示
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView showHome(Model model, HttpSession session) {

        JMonitor.add("genthrift.pv");

        //没有登录,先登录
        if (session.getAttribute("login") == null || !(Boolean) session.getAttribute("login")) {
            return new ModelAndView("login");
        }

        //添加页面表单对象
        model.addAttribute("genThriftForm", new GenThriftForm());
        return new ModelAndView("genThrift");
    }

    //处理解析首页提交的表单
    @RequestMapping(value = "/gen", method = RequestMethod.POST)
    public String homeFormHandle(@RequestParam("idlFiles") MultipartFile[] idlFiles,
                                 @ModelAttribute("genThriftForm") GenThriftForm genThriftForm,
                                 HttpSession session) throws Exception {

        appkey = genThriftForm.getAppkey();
        filename = genThriftForm.getFilename();

        Transaction transaction = Cat.newTransaction("genthrift", filename);
        transaction.addData("login=", ((MISInfo)session.getAttribute("misInfo")).getLogin());
        Cat.logEvent("thrift-version", genThriftForm.getThriftVersion(), Event.SUCCESS, "");
        Cat.logEvent("thrift-language", genThriftForm.getLanguages().get(0), Event.SUCCESS, "size="+genThriftForm.getLanguages().size());

        //获取系统时间作为文件名
        timestamp = Long.toString(System.currentTimeMillis());
        logger.info("\n\n" + timestamp);

        //创建文件的根路径
        String fileDir = settings.getCommonDir() + timestamp + "/";
        String mkDir = "mkdir " + fileDir;
        ShellProcess.runShell(mkDir);

        //若没有包含idlfiles,则报错,解决:返回异常提示页面
        for (MultipartFile idlFile : idlFiles) {
            if (idlFile.isEmpty()) {
                logger.warn("Upload files is null!");
            } else {
                FileUtils.copyInputStreamToFile(idlFile.getInputStream(), new File(fileDir, idlFile.getOriginalFilename()));
            }
        }

        //调用GenThriftService的genThrift方法
        genThriftService = new GenThriftService(genThriftForm, timestamp, settings);
        result = genThriftService.service();

        session.setAttribute("result", result);
        session.setAttribute("settings", settings);
        session.setAttribute("file", timestamp);

        transaction.setStatus(Transaction.SUCCESS);
        transaction.complete();

        //执行出错
        if (GenThriftFlag.RUNERROR == result.getFlag()) {
            return "redirect:/";
        } else {
            //说明没有编译错误
            if (!appkey.contains("简单使用")) {

                uid = ((MISInfo) session.getAttribute("misInfo")).getLogin();
                idlVersionService = new IdlVersionService(timestamp, appkey, uid, filename, settings, idlVersionManager);
                String diff = idlVersionService.checkDiff();
                if ("ok".equals(diff)) {
                    idlVersionService.addIdlVersion(1, "原始版本");
                    return "redirect:/success";
                } else if ((diff != null) && (!diff.trim().equals(""))) {
                    //跳转至显示diff异同的界面
                    session.setAttribute("diff", diff);
                    return "redirect:/diff";
                }
            }
            return "redirect:/success";
        }

    }

    @RequestMapping(value = "/help", method = RequestMethod.GET)
    public ModelAndView showHelp() {
        return new ModelAndView("help");
    }

    @RequestMapping(value = "/success", method = RequestMethod.GET)
    public ModelAndView showSuccess() {
        //下载之前执行一下整理工作
        genThriftService.afterGen();
        return new ModelAndView("success");
    }

    @RequestMapping(value = "/diff", method = RequestMethod.GET)
    public ModelAndView showDiff() {
        return new ModelAndView("diff");
    }

    @RequestMapping(value = "/addRemark", method = RequestMethod.POST)
    public String addRemark(HttpServletRequest request, HttpSession session) {
        String remark = request.getParameter("remark").trim();
        uid = ((MISInfo) session.getAttribute("misInfo")).getLogin();

        idlVersionService = new IdlVersionService(timestamp, appkey, uid, filename, settings, idlVersionManager);
        //获取最新版的版本号
        int version = idlVersionService.getVersion();
        idlVersionService.addIdlVersion(version + 1, remark);
        return "redirect:/success";
    }


    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String deleteGenFile() {
        String rmFile = "rm -rf " + settings.getCommonDir() + timestamp;
        ShellProcess.runShell(rmFile);
        return "redirect:/";
    }

    @RequestMapping(value = "/genByAppKey", method = RequestMethod.POST)
    public ModelAndView genByAppKey(HttpServletRequest request, HttpSession session) {
        String appkey = request.getParameter("appkey");
        int num = idlVersionManager.existAppKey(appkey);
        if (num == 0)
            return null;
        int version = idlVersionManager.getVersionByAppkey(appkey);
        String file = idlVersionManager.getFileByAppKeyAndVersion(appkey, version);
        session.setAttribute("settings", settings);
        session.setAttribute("file", file);
        ModelAndView mv = new ModelAndView();
        mv.setViewName("success");
        return mv;
    }

    @RequestMapping(value = "/exist", method = RequestMethod.GET)
    public void existAppKey(@RequestParam String appkey, HttpServletResponse response) {
        int num = idlVersionManager.existAppKey(appkey);
        if (num > 0) {
            try {
                response.getWriter().print("Download the appkey files");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/result", method = RequestMethod.GET)
    public ModelAndView getResult() {
        return new ModelAndView("result");
    }

}
