package com.meituan.service.mobile.thrift.servlet;

import com.meituan.service.mobile.thrift.model.IdlConstant;
import com.meituan.service.mobile.thrift.model.IdlFile;
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
 * Time: 下午5:01
 */
@Controller
@RequestMapping(value = "/genidl")
public class GenConstantServlet {

    private IdlFile idlFile;
    private IdlConstant idlConstant;
    private String constantName;
    private String constantType;
    private String constantValue;

    @RequestMapping(value = "/constant", method = RequestMethod.GET)
    public String showConstant(){
        return "/genidl/constant";
    }

    @RequestMapping(value = "/cleanConstant", method = RequestMethod.GET)
    public String cleanConstant(HttpSession session){
        IdlFile idlFile = (IdlFile)session.getAttribute("idlFile");
        idlFile.getConstantList().clear();
        return "/genidl/constant";
    }

    @RequestMapping(value = "/constant", method = RequestMethod.POST)
    public String constantFormHandle(HttpSession session, HttpServletRequest req){

        idlFile = (IdlFile) session.getAttribute("idlFile");

        Map map = req.getParameterMap();
        int size = map.size()/3;
        for(int i = 1; i <= size; i++){
            constantName = ((String[])map.get("constantName"+i))[0];
            constantType = ((String[])map.get("constantType"+i))[0];
            constantValue = ((String[])map.get("constantValue"+i))[0];


            idlConstant = new IdlConstant();
            idlConstant.setName(constantName);
            idlConstant.setType(constantType);
            idlConstant.setValue(constantValue);
            idlFile.getConstantList().add(idlConstant);
        }

        return "/genidl/constant";
    }

}
