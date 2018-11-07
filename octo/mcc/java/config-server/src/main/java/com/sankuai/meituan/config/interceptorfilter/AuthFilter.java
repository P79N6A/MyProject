package com.sankuai.meituan.config.interceptorfilter;

import com.sankuai.meituan.config.service.SpaceConfigService;
import com.sankuai.meituan.config.service.UserService;
import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.meituan.filter.util.User;
import com.sankuai.meituan.filter.util.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.sankuai.meituan.config.model.Setting.ENABLE_SSO;

/**
 * 采用黑名单方式进行鉴权，不在黑名单中的URL都不需要鉴权
 *
 * @author yangguo03
 * @version 1.0
 * @created 14-5-6
 */
public class AuthFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(AuthFilter.class);

    @Resource
    private UserService userService;
    @Resource
    private SpaceConfigService spaceConfigService;
    private static boolean isEnableSSO = true;

    private static final Set<String> accessWhiteList = new ConcurrentHashSet<String>();


    public static void setAccessWhiteList(String usernames){

        if(StringUtils.isNotEmpty(usernames)){
            String[] usernameArray = usernames.split(",");
            Set<String> accessWhiteListTmp = new HashSet<String>();
            for(String item:usernameArray){
                if(StringUtils.isNotEmpty(item)){
                    accessWhiteListTmp.add(item.trim());
                }
            }
            if(!accessWhiteListTmp.isEmpty()){
                LOG.info("the old access whitelist = {}", accessWhiteList);
                accessWhiteList.clear();
                accessWhiteList.addAll(accessWhiteListTmp);
                LOG.info("the new access whitelist = {}", accessWhiteList);

            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String spacePath = NodeNameUtil.getMCCSettingSpacePath();
        isEnableSSO = spaceConfigService.getBoolSetting(spacePath, ENABLE_SSO, true);
        LOG.info("isEnableSSO = {}", isEnableSSO);
        if (isEnableSSO) {
            ServletContext context = filterConfig.getServletContext();
            DelegatingFilterProxy filterProxy = new DelegatingFilterProxy("mtFilter");
            filterProxy.setTargetFilterLifecycle(true);
            context.addFilter("mtFilter", filterProxy).addMappingForUrlPatterns(null, false, "/*");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (!isEnableSSO) {
            UserUtils.bind(UserService.defaultAdminUser);
        }

        // 获取url
        String url = req.getRequestURI();
        if (!isAccessAllow(url)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // do nothing
    }

    private Boolean isAccessAllow(String url) {
        User user = UserUtils.getUser();
        if (user == null) {
            // 交由UPM控制，不需要登录的都可以访问
            return Boolean.TRUE;
        }
        Integer userId = user.getId();

        if (StringUtils.isBlank(url)) {
            return Boolean.TRUE;
        }

        // 超级管理员
        if (userService.isConfigAdmin(userId)) {
            return Boolean.TRUE;
        }

        // 需要超级管理员权限
        if (url.startsWith("/config/spaces/") && !url.equals("/config/spaces/list")) {  // 空间增删
            return Boolean.FALSE;
        }
        if (url.startsWith("/config/user/admin")) {   // 超级管理员配置
            return Boolean.FALSE;
        }
        // 空间管理员
        if (url.startsWith("/config/space/") && !url.startsWith("/config/space/sample/")) {
                String[] urlSplits = url.split("/");
                if (urlSplits.length < 4) {     // 非法URL
                    return Boolean.FALSE;
                }
                String spaceName = urlSplits[3];
                //不是白名单的人员，需要限制配置的更新，新增，删除操作
                if (!accessWhiteList.contains(user.getLogin())) {
                    if (url.contains("/node/update") || url.contains("/node/add") || url.contains("/node/delete"))
                        return Boolean.FALSE;
                }
                if (!userService.isSpaceAdmin(spaceName, userId)) {
                    return Boolean.FALSE;
                }
            }
        return Boolean.TRUE;

    }

}
