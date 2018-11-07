package com.meituan.service.mobile.thrift.servlet;

import com.meituan.service.mobile.thrift.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-2-4
 * Time: 下午5:05
 */
@Controller
@RequestMapping(value = "/genidl")
public class GenStructServlet {

    private IdlFile idlFile;
    private IdlStruct idlStruct;
    private IdlVariable idlVariable;

    private List<IdlVariable> idlVariableList;

    private String structName;
    private String variableName;
    private String variableType;
    private String variableValue;

    @RequestMapping(value = "/struct", method = RequestMethod.GET)
    public String showStruct(){
        return "/genidl/struct";
    }

    @RequestMapping(value = "/cleanStruct", method = RequestMethod.GET)
    public String cleanStruct(HttpSession session){
        IdlFile idlFile = (IdlFile)session.getAttribute("idlFile");
        idlFile.getStructList().clear();
        return "/genidl/struct";
    }

    @RequestMapping(value = "/struct", method = RequestMethod.POST)
    public String structFormHandle(HttpSession session, HttpServletRequest req){

        idlFile = (IdlFile) session.getAttribute("idlFile");


        idlStruct = new IdlStruct();
        idlVariableList = new ArrayList<IdlVariable>();

        Map map = req.getParameterMap();
        structName = ((String[])map.get("structName"))[0];
        idlStruct.setStructName(structName);


        int size = map.size()/3;
        for(int i = 1; i <= size; i++){
            variableName = ((String[])map.get("variableName"+i))[0];
            variableType = ((String[])map.get("variableType"+i))[0];
            variableValue = ((String[])map.get("variableValue"+i))[0];

            idlVariable = new IdlVariable();
            idlVariable.setName(variableName);
            idlVariable.setType(variableType);
            idlVariable.setValue(variableValue);
            idlVariableList.add(idlVariable);
        }

        idlStruct.setVariableList(idlVariableList);
        idlFile.getStructList().add(idlStruct);
        return "/genidl/struct";
    }
}
