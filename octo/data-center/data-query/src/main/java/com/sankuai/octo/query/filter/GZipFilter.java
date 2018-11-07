package com.sankuai.octo.query.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GZipFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(GZipFilter.class);

    public void init(FilterConfig config) throws ServletException {
        log.info("init GZipFilter");

    }

    public void destroy() {


    }

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String acceptEncoding = request.getHeader("Accept-Encoding");
        //支持的编码方式

        if (acceptEncoding != null && acceptEncoding.toLowerCase().indexOf("gzip") != -1) {
            //如果客户浏览器支持GZIP格式，则使用GZIP压缩数据
            GZipResponseWrapper gzipRes = new GZipResponseWrapper(response);

            chain.doFilter(request, gzipRes);//doFilter,使用自定义的response
            gzipRes.finishResponse();//输出压缩数据
        } else {
            chain.doFilter(request, response);//否则不压缩
        }

    }


}