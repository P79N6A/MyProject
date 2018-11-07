package com.sankuai.octo.msgp.utils;

/**
 * Created by zava on 16/1/26.
 */

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class HttpServletRequestReplacedFilter implements Filter {

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO

    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        String uri = ((HttpServletRequest) request).getRequestURI();
        if(request instanceof HttpServletRequest && !ServletFileUpload.isMultipartContent((HttpServletRequest)request)) {
            if(!"/mt-sso".equals(uri)){
                requestWrapper = new BodyReaderHttpServletRequestWrapper((HttpServletRequest) request);
            }
        }

        HttpServletResponse res = (HttpServletResponse)response ;

        if(uri.indexOf("stream") >-1 ){
            res.addHeader("Access-Control-Allow-Origin", "http://release.octo.test.sankuai.info");
            res.addHeader("Access-Control-Allow-Methods", "GET,OPTIONS");
            res.addHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
            res.addHeader("X-Accel-Buffering", "no");
            res.addHeader("Access-Control-Allow-Credentials","true");
        }
        if(null == requestWrapper) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(requestWrapper, response);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // TODO

    }

}