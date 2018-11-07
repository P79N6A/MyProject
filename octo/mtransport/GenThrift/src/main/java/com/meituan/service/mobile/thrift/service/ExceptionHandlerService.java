package com.meituan.service.mobile.thrift.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-2-3
 * Time: 下午4:39
 */
@Component
public class ExceptionHandlerService implements HandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerService.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler, Exception e) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("e", e);
        logger.error(e.getMessage(), e);

        //表单上传异常
        if(e instanceof MissingServletRequestParameterException)
            return new ModelAndView("formException", model);

        return new ModelAndView("error", model);
    }
}
