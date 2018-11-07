package com.meituan.service.mobile.thrift.servlet;

import com.meituan.service.mobile.thrift.model.IdlFile;
import com.meituan.service.mobile.thrift.model.IdlNamespace;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-2-4
 * Time: 下午2:42
 */
@Controller
@RequestMapping(value = "/genidl")
public class GenNamespaceServlet {

    private IdlFile idlFile;
    private IdlNamespace idlNamespace;

    private String language;
    private String namespace;

    @RequestMapping(value = "/namespace", method = RequestMethod.GET)
    public String showNamespace(){
        return "/genidl/namespace";
    }

    @RequestMapping(value = "/cleanNamespace", method = RequestMethod.GET)
    public String cleanNamespace(HttpSession session){
        IdlFile idlFile = (IdlFile)session.getAttribute("idlFile");
        idlFile.getNamespaceList().clear();
        return "/genidl/namespace";
    }

    @RequestMapping(value = "/namespace", method = RequestMethod.POST)
    public String namespaceFormHandle(HttpSession session, HttpServletRequest req){

        idlFile = (IdlFile) session.getAttribute("idlFile");

        Map map = req.getParameterMap();

        int size = map.size()/2;
        for(int i = 1; i <= size; i++){
            language = ((String[])map.get("language"+i))[0];
            namespace = ((String[])map.get("namespace"+i))[0];

            idlNamespace = new IdlNamespace();
            if(language.equals("c++"))
                language = "cpp";
            idlNamespace.setLanguage(language);
            idlNamespace.setNamespace(namespace);
            idlFile.getNamespaceList().add(idlNamespace);
        }
        return "/genidl/namespace";
    }



}
