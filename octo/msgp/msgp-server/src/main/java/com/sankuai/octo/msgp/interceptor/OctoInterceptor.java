package com.sankuai.octo.msgp.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meituan.jmonitor.JMonitor;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.meituan.common.util.NetUtil;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.msgp.common.config.MsgpConfig;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.serivce.DashboardService;
import com.sankuai.octo.msgp.serivce.Setting;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.utils.ThreadContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OctoInterceptor implements HandlerInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(OctoInterceptor.class);
    private static final Pattern patternFix = Pattern.compile("^(com.(sankuai|meituan))[.]*.*");
    private static final String QA_AUTH_TRIGGER = "qa_trigger";
    private static boolean trigger = Boolean.parseBoolean(MsgpConfig.get(QA_AUTH_TRIGGER, "false"));

    static {
        MsgpConfig.addListener(QA_AUTH_TRIGGER, new IConfigChangeListener() {
            @Override
            public void changed(String s, String oldValue, String newValue) {
                LOG.info("config[{}] changed from {} to {}", QA_AUTH_TRIGGER, oldValue, newValue);
                trigger = Boolean.parseBoolean(newValue);
            }
        });
    }

    /**
     * appkey 获取规则 url path 里 获取 com.sankuai/com.meituan
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //新的权限控制方法，获取appkey
        String appkeyFix = getAppkey(request);
        /** 保存线程上下文*/
        ThreadContext.remove("appkey");
        ThreadContext.put("appkey", appkeyFix);
        String requestIp = NetUtil.getRealIp(request);
        ThreadContext.remove("requestIp");
        ThreadContext.put("requestIp", requestIp);
        //增加这两个上下午信息是为了值班人鉴权功能
        ThreadContext.put("uri", request.getRequestURI());
        ThreadContext.put("method", request.getMethod());
        JMonitor.kpiForCount("pv");
        if (StringUtils.isNotBlank(appkeyFix)) {
            ServiceCommon.putSessionAppkey(appkeyFix);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            User user = UserUtils.getUser();
            if (user != null && user.getId() != 0) {
                List<Map<String, String>> shortcuts = Setting.getShortcutList(user.getLogin());
                modelAndView.addObject("shortcuts", shortcuts);
                List<Map<String, Object>> menus = DashboardService.menus();
                modelAndView.addObject("menus", menus);

                Integer operationMenuId = (Integer) request.getAttribute("_currentMenuId");
                String currentURI = request.getRequestURI();
                for (Map<String, Object> parentMenuFields : menus) {
                    List<Map<String, Object>> subMenus = (List<Map<String, Object>>) parentMenuFields.get("menus");
                    for (Map<String, Object> menuFields : subMenus) {
                        Integer pid = (Integer) parentMenuFields.get("id");
                        Integer id = (Integer) menuFields.get("id");

                        String url = (String) menuFields.get("url");
                        if (url.indexOf("?") > 0) {
                            url = url.substring(0, url.indexOf("?"));
                        }
                        if (operationMenuId != null && operationMenuId != 0 && id == operationMenuId) {
                            request.setAttribute("_currentMenuId", operationMenuId);
                            request.setAttribute("_currentParentMenuId", pid);
                            return;
                        }
                        if (currentURI.startsWith(url)) {
                            request.setAttribute("_currentMenuId", id);
                            request.setAttribute("_currentParentMenuId", pid);
                            return;
                        }
                    }
                }
            }
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    private String getAppkey(HttpServletRequest request) {
        String uri = request.getRequestURI().toString();

        String arr[] = uri.split("\\/");
        String appkeyFix = "";
        for (int i = 0; i < arr.length; i++) {
            Matcher matcher = patternFix.matcher(arr[i]);
            if (matcher.matches()) {
                appkeyFix = arr[i];
                return appkeyFix;
            }
        }
        // appkey 是参数/一级变量参数
        if (StringUtil.isBlank(appkeyFix)) {
            try {
                appkeyFix = request.getParameter("appkey");
                if (!StringUtil.isBlank(appkeyFix)) {
                    return appkeyFix;
                }
            } catch (Exception e) {
                LOG.error("get appkey from Parameter error", e);
            }
        }
        // appkey 是二级变量参数, 作为子pojo类变量
        if (StringUtil.isBlank(appkeyFix)) {
            try {
                Map<String, String[]> parameterMap = request.getParameterMap();
                for (String paramName : parameterMap.keySet()) {
                    if (paramName.matches(".*\\.appkey")) {
                        appkeyFix = request.getParameter(paramName);
                        if (!StringUtil.isBlank(appkeyFix)) {
                            return appkeyFix;
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("get appkey from Parameter error", e);
            }
        }

        String contentType = request.getContentType();
        String method = request.getMethod();
        if (StringUtil.isBlank(appkeyFix) &&
                StringUtil.isNotBlank(contentType)
                && !"GET".equalsIgnoreCase(method.toUpperCase())
                && (contentType.toLowerCase().indexOf("application/x-www-form-urlencoded") > -1 ||
                contentType.toLowerCase().indexOf("application/json") > -1)) {
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            try {
                BufferedReader br = request.getReader();
                while ((line = br.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String tmp = stringBuilder.toString();
                if (StringUtils.isNotBlank(tmp)) {
                    Map<String, String> readerMap = (Map<String, String>) JsonHelper.toObject(tmp, Map.class);
                    appkeyFix = readerMap.get("appkey");
                } else {
                    LOG.info("参数为空,url:" + request.getRequestURI());
                }
            } catch (Exception e) {
                LOG.error("get appkey from BufferedReader error,data:" + stringBuilder.toString(), e);
            }
        }
        return appkeyFix;
    }

    private String getEnv(HttpServletRequest request) {
        String env = "";
        try {
            env = request.getParameter("env");
            if (StringUtil.isNotBlank(env)) {
                env = formatEnv(env);
            }
        } catch (Exception e) {
            LOG.error("get appkey from Parameter error");
        }

        if (StringUtil.isBlank(env)) {
            try {
                Map<String, String[]> parameterMap = request.getParameterMap();
                for (String paramName : parameterMap.keySet()) {
                    if (paramName.matches(".*\\.env")) {
                        env = request.getParameter(paramName);
                        if (!StringUtil.isBlank(env)) {
                            env = formatEnv(env);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("get appkey from Parameter error");
            }
        }
        if (StringUtil.isBlank(env)) {
            try {
                String params = IOUtils.toString(request.getInputStream());
                JSONObject o = JSON.parseObject(params);
                String temp = o.get("env").toString();
                if (StringUtil.isNotBlank(o.get("env"))) {
                    env = formatEnv(temp);
                }
            } catch (Exception e) {
                LOG.error("get appkey from Parameter error");
            }
        }

        return env != null ? env : "";
    }

    private String formatEnv(String env) {
        switch (env) {
            case "1":
                env = "test";
                break;
            case "2":
                env = "stage";
                break;
            case "3":
                env = "prod";
                break;
        }
        return env;
    }

}
