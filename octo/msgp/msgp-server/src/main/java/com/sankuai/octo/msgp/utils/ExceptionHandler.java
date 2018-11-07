package com.sankuai.octo.msgp.utils;


import com.sankuai.msgp.common.utils.helper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zava on 16/4/11.
 */
public class ExceptionHandler implements HandlerExceptionResolver {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LOG.error("msgp error,url:" + request.getRequestURI()+",params:"+ JsonHelper.jsonStr(request.getParameterMap()), ex);
        LOG.info("msgp error,url:" + request.getRequestURI()+",params:"+ JsonHelper.jsonStr(request.getParameterMap()), ex);
        return new ModelAndView("error");
    }
}
