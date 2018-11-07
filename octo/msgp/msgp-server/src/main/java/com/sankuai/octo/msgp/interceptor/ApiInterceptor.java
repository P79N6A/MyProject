package com.sankuai.octo.msgp.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.common.io.IOUtils;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.domain.HttpAuthItemNode;
import com.sankuai.octo.msgp.service.BootstrapService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * @author uu
 * @description api的非get接口都会经过此权限过滤器
 * @date Created in 11:44 2018/5/2
 * @modified
 */
public class ApiInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ApiInterceptor.class);
    private static final String OCTO_SERVER_TOKEN = "X-OCTO-Server-Token";
    private static final List<String> adminURISuffixList = Arrays.asList("disable", "shutdown");

    /**
     * @param
     * @return
     * @description
     * @author zhanghui24
     * @date Created in 12:10 2018/5/2
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
//        if (!auth(request)) {
//            returnJson(response, JsonHelper.errorJson("鉴权未通过"));
//            return false;
//        }
        auth(request);
        return true;
    }

    /**
     * @param request
     * @return boolean
     * @description
     * @author zhanghui24
     * @date Created in 12:09 2018/5/2
     */
    private boolean auth(HttpServletRequest request) {
        try {
            String methodType = request.getMethod();
            // ignore get method, return true
            if ("GET".equalsIgnoreCase(methodType)) {
                return true;
            }

            String username = "", appkey = "";
            String uri = (null != request.getRequestURI()) ? request.getRequestURI() : "";
            if (StringUtils.contains(uri, "disable") || StringUtils.contains(uri, "shutdown")) {
                username = (null != request.getHeader("admin")) ? request.getHeader("admin") : "";
            } else {
                if (StringUtils.isBlank(username)) {
                    username = (null != request.getHeader("username")) ? request.getHeader("username") : "";
                }
                try {
                    BufferedReader reader = request.getReader();
                    String json = IOUtils.copyToString(reader);
                    JSONObject obj = JSONObject.parseObject(json);
                    if (obj != null) {
                        username = StringUtils.isBlank(username) ? obj.getString("username") : username;
                        appkey = StringUtils.isBlank(obj.getString("appkey")) ? "" : obj.getString("appkey").trim();
                    }
                    username = StringUtil.isBlank(username) ? "" : username.trim();
                } catch (Exception e) {
                    LOG.error("[请求体为空]:  username:{}, url:{}, appkey:{}", username, request.getRequestURI(), appkey);
                }

            }
            HttpAuthItemNode itemNode = BootstrapService.getUserTokenMap().get(username);
            // not exist in cache, return false
            if (null == itemNode || null == itemNode.getPatternList()) {
                User user = UserUtils.getUser();
                LOG.error("[获取鉴权信息失败:缺失token]:  username:{}, url:{}, appkey:{}", username, request.getRequestURI(), appkey);
                return false;
            }
            boolean isMatch = false;
            for (String pattern : itemNode.getPatternList()) {
                if (StringUtils.equals("*", pattern.trim()) || StringUtils.startsWith(appkey, pattern.trim())) {
                    isMatch = true;
                    break;
                }
            }
            if (isMatch) {
                String reqOctoServerToken = request.getHeader(OCTO_SERVER_TOKEN);
                String localToken = itemNode.getToken();
                // everything is perfect , return true
                if (StringUtils.isNotEmpty(reqOctoServerToken) && StringUtils.isNotEmpty(localToken) && StringUtils.equals(reqOctoServerToken, localToken)) {
                    return true;
                } else {
                    // the user do exist in cache, but its request token is wrong, return false
                    User user = UserUtils.getUser();
                    LOG.error("[获取鉴权信息失败:token不匹配]:  username:{}, url:{}, appkey:{}", username, request.getRequestURI(), appkey);
                    return false;
                }
            } else {
                // the user do exist in cache, but its pattern does not match with the request appkey, return false
                User user = UserUtils.getUser();
                LOG.error("[获取鉴权信息失败:appkey不匹配]:  username:{}, url:{}, appkey:{}", username, request.getRequestURI(), appkey);
                return false;
            }
        } catch (Exception e) {
            LOG.error("[ApiInterceptor auth error]: uri:{},error:{}", request.getRequestURI(), e);
        }
        return false;
    }

    /**
     * @param
     * @return
     * @description
     * @author zhanghui24
     * @date Created in 12:10 2018/5/2
     */
    private void returnJson(HttpServletResponse response, String json) throws Exception {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        try {
            writer = servletResponse.getWriter();
            writer.print(json);
            writer.flush();
        } catch (IOException e) {
            LOG.error("ApiInterceptor returnJson error", e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}