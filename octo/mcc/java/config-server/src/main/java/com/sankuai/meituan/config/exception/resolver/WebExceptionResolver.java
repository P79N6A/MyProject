package com.sankuai.meituan.config.exception.resolver;

import com.google.common.collect.Sets;
import com.sankuai.meituan.config.constant.ParamName;
import com.sankuai.meituan.config.util.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WebExceptionResolver extends SimpleMappingExceptionResolver {
    private final Logger logger = LoggerFactory.getLogger(WebExceptionResolver.class);

    private Set<String> unHandleRequestDomain = Sets.newHashSet();

    private Set<Class<?>> knownException = Sets.newHashSet();

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request,
                                              HttpServletResponse response, Object handler, Exception ex) {
        printExceptionLog(request, ex);

        if (shouldHandleRequest(request)) {
            setApiErrorResponse(response, ex);
            return null;
        } else {
            return super.doResolveException(request, response, handler, ex);
        }
    }

    private void setApiErrorResponse(HttpServletResponse response, Exception ex) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        if (knownException.contains(ex.getClass())) {
            HttpUtil.setJsonResponse(response, ex.getMessage(), HttpServletResponse.SC_OK);
        }else{
            HttpUtil.setJsonResponse(response, ex.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean shouldHandleRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
	    Assert.notNull(uri);
        for (String domain : unHandleRequestDomain) {
            if (uri.startsWith(domain)) {
                return false;
            }
        }
        return true;
    }


    private void printExceptionLog(HttpServletRequest request, Exception ex) {
        String parameters = getRequestParameters(request);
        StringBuilder errorMessage = new StringBuilder().append("Error occurred when ").append(request.getMethod());
        appendClientId(request, errorMessage).append(" uri: ").append(request.getRequestURL());
        if (StringUtils.isNotEmpty(parameters)) {
            errorMessage = errorMessage.append("?").append(parameters);
        }
        if (!knownException.contains(ex.getClass())) {
            logger.error(errorMessage.toString(), ex);
        } else {
            logger.warn(errorMessage.toString(), ex);
        }
    }

    private StringBuilder appendClientId(HttpServletRequest request, StringBuilder errorMessage) {
        Object apiClientId = request.getAttribute(ParamName.CLIENT_ID);
        if (apiClientId != null) {
            errorMessage.append(" client id: ").append(apiClientId);
        }
        return errorMessage;
    }

    private String getRequestParameters(HttpServletRequest request) {
        StringBuilder parametersBuilder = new StringBuilder();

        @SuppressWarnings("unchecked")
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Iterator<String> iter = parameterMap.keySet().iterator(); iter.hasNext(); ) {
            String key = iter.next();
            if (key != null && !"".equals(key.trim())) {
                parametersBuilder.append(key).append("=").append(request.getParameter(key));
            }
            if (iter.hasNext()) {
                parametersBuilder.append("&");
            }
        }
        return parametersBuilder.toString();
    }

    public void setUnHandleRequestDomain(Set<String> unHandleRequestDomain) {
        checkApiDomain(unHandleRequestDomain);
        this.unHandleRequestDomain = unHandleRequestDomain;
    }

	public void setKnownException(Set<Class<?>> knownException) {
		this.knownException = knownException;
	}

	private void checkApiDomain(Set<String> handleApiDomain) {
        for (String domain : handleApiDomain) {
            if (StringUtils.contains(domain, "*")) {
                throw new RuntimeException(MessageFormat.format("{0}的配置出错,配置异常移动处理转换的url只支持字符串匹配,不支持正则等动态匹配!", WebExceptionResolver.class.getSimpleName()));
            }
        }
    }
}
