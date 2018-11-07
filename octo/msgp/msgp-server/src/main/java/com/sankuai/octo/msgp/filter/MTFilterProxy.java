package com.sankuai.octo.msgp.filter;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.msgp.common.config.MsgpConfig;
import com.sankuai.msgp.common.utils.client.TairClient;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class MTFilterProxy implements Filter {
    private final Logger log = LoggerFactory.getLogger(MsgpApiAuthFilter.class);
    private final Boolean useSSO = new Boolean(MsgpConfig.get("use_sso", "true"));
//    private final Boolean useSSO = false;
    private final String tip_prefix = "display_tip_";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (useSSO) {
            ServletContext servletContext = filterConfig.getServletContext();
            DelegatingFilterProxy filter = new DelegatingFilterProxy("mtFilter");
            filter.setTargetFilterLifecycle(true);
            servletContext.addFilter("mtFilter", filter).addMappingForUrlPatterns(null, false, "/*");
            log.info("init sso filter");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!useSSO) {
            UserUtils.unbindUser();
            UserUtils.unbindUserMenus();
            User user = new User();
            user.setId(2008028);
            user.setLogin("monkey");
            user.setName("monkey(monkey)");
//            user.setId(2130494);
//            user.setLogin("zhangyun16");
//            user.setName("张昀");
            UserUtils.bind(user);
            request.setAttribute("s", user);
            request.setAttribute("__user__", user);
            request.setAttribute("__currentUser__", user);
            request.setAttribute("_currentUser", user);
        }
        request.setAttribute("isOffline", CommonHelper.isOffline());
        chain.doFilter(request, response);
    }

    public void destroy() {
        log.info("destroy");
    }

}
