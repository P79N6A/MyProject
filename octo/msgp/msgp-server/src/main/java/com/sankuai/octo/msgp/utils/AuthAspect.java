package com.sankuai.octo.msgp.utils;

import com.meituan.jmonitor.JMonitor;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.meituan.auth.vo.User;
import com.sankuai.msgp.common.service.org.OrgSerivce;
import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.client.TairClient;
import com.sankuai.msgp.common.utils.helper.CommonHelper;
import com.sankuai.msgp.common.utils.helper.JsonHelper;
import com.sankuai.octo.msgp.controller.OncallController;
import com.sankuai.octo.msgp.serivce.AppkeyAuth;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import scala.Option;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthAspect {
    private static final Logger LOG = LoggerFactory.getLogger(AuthAspect.class);

    private static final List<String> AUTH_METHOD = Arrays.asList("PUT", "POST", "DELETE", "put", "post", "delete");

    private static final String provider_regex = "/service/\\b.*/provider/\\d.*";

    private static final String mcc_regex = "/serverOpt/\\b.*";


    @Pointcut("@annotation(Auth) || within(@Auth *)")
    public void requireAuth() {
    }

    @Around("requireAuth()")
    public Object auth(ProceedingJoinPoint point) throws Throwable {
        Auth auth = getAuth(point);
        if (auth != null) {
            JMonitor.kpiForCount("pv.auth");
            // 当前level
            Integer level = auth.level().getValue();
            // 获取用户信息和appkey
            User user = UserUtils.getUser();
            String appkey = (String) ThreadContext.get("appkey");
            Boolean hasAuth = AppkeyAuth.hasAuth(appkey, level, user);
            hasAuth = hasAuth || isOncall(appkey, user.getLogin());
            if (hasAuth) {
                Object response = point.proceed(point.getArgs());
                return response;
            } else {
                //区分view和json返回不同信息
                String className = point.getTarget().getClass().getName();
                String methodName = point.getSignature().getName();
                LOG.info("no auth " + user.toString() + "," + className + "." + methodName + ",appkey:" + appkey);
                if (auth.responseMode() == Auth.ResponseMode.VIEW) {
                    return "redirect:/manage/perDenied?appkey=" + appkey;
                } else {
                    return JsonHelper.errorJson("您对此服务没有操作权限");
                }
            }
        } else {
            return point.proceed(point.getArgs());
        }
    }

    private Auth getAuth(ProceedingJoinPoint point) {
        // 优先取method注解
        if (point.getSignature() instanceof MethodSignature) {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            if (method != null && method.getAnnotation(Auth.class) != null) {
                return method.getAnnotation(Auth.class);
            }
        }
        // 其次取class注解
        Auth classAuth = point.getTarget().getClass().getAnnotation(Auth.class);
        return classAuth;
    }

    private boolean isOncall(String appkey, String login) {
        String method = ThreadContext.get("method").toString().trim();
        if (StringUtil.isNotBlank(method) && AUTH_METHOD.contains(method)) {
            String uri = ThreadContext.get("uri").toString();
            if (Pattern.matches(provider_regex, uri) || Pattern.matches(mcc_regex, uri)) {
                Option<String> oncallStr = TairClient.get(OncallController.ONCALL_PRIFIX+appkey);
                if(oncallStr.isDefined()){
                    return oncallStr.get().contains(login);
                }
            }
            return false;
        } else {
            return false;
        }
    }
}