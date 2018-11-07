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
 * Time: 下午5:12
 */
@Controller
@RequestMapping(value = "/genidl")
public class GenServiceServlet {

    private IdlFile idlFile;
    private IdlService idlService;
    private IdlMethod idlMethod;
    private IdlParameter idlParameter;

    private List<IdlMethod> idlMethodList;
    private List<IdlParameter> idlParameterList;

    private String serviceName;
    private String methodName;
    private String methodType;

    private String parameterName;
    private String parameterType;

    @RequestMapping(value = "/service", method = RequestMethod.GET)
    public String showService(){
        return "/genidl/service";
    }

    @RequestMapping(value = "/cleanService", method = RequestMethod.GET)
    public String cleanService(HttpSession session){
        IdlFile idlFile = (IdlFile)session.getAttribute("idlFile");
        idlFile.getServiceList().clear();
        return "/genidl/service";
    }

    @RequestMapping(value = "/service", method = RequestMethod.POST)
    public String serviceFormHandle(HttpSession session, HttpServletRequest req){

        idlFile = (IdlFile) session.getAttribute("idlFile");


        idlService = new IdlService();
        idlMethodList = new ArrayList<IdlMethod>();


        Map map = req.getParameterMap();


        serviceName = ((String[])map.get("serviceName"))[0];
        idlService.setServiceName(serviceName);


        for(int i = 1 ;; i++){
            if(map.get("methodName"+i) != null){
                methodName = ((String[])map.get("methodName"+i))[0];
                methodType = ((String[])map.get("methodType"+i))[0];
                idlParameterList = new ArrayList<IdlParameter>();

                for(int j = 1 ;; j++){
                    if(map.get("parameterName"+i+j) != null){
                        parameterName = ((String[])map.get("parameterName"+i+j))[0];
                        parameterType = ((String[])map.get("parameterType"+i+j))[0];

                        if(parameterType.equals("int"))
                            parameterType = "i32";

                        idlParameter = new IdlParameter();
                        idlParameter.setName(parameterName);
                        idlParameter.setType(parameterType);

                        idlParameterList.add(idlParameter);
                    }
                    else
                        break;
                }
                if(methodType.equals("int"))
                    methodType = "i32";

                idlMethod = new IdlMethod();
                idlMethod.setName(methodName);
                idlMethod.setType(methodType);
                idlMethod.setParameterList(idlParameterList);

                idlMethodList.add(idlMethod);


            }
            else
                break;
        }

        idlService.setMethodList(idlMethodList);
        idlFile.getServiceList().add(idlService);

        return "/genidl/service";
    }
}
