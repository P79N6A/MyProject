package com.sankuai.octo.msgp.utils;

import com.meituan.jmonitor.JMonitor;
import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.octo.mworth.common.model.OperationSourceType;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.common.model.WorthEvent;
import com.sankuai.octo.mworth.service.mWorthEventService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by zava on 16/1/26.
 * 如果不喜欢注解:
 *
 * @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)") 拦截所有的 http 请求,注意 配置 project,model
 */
@Component
@Aspect
@Order(5)
public class WorthAspect {

    private static final Logger LOG = LoggerFactory.getLogger(WorthAspect.class);

    @Pointcut("@annotation(com.sankuai.octo.mworth.common.model.Worth) || within(@com.sankuai.octo.mworth.common.model.Worth *)")
    public void requireWorth() {
    }

    @Around("requireWorth()")
    public Object aroundWorth(ProceedingJoinPoint point) throws Throwable {
        Worth worthAnno = getWorth(point);
        if (null != worthAnno) {
            JMonitor.kpiForCount("pv.worth");
            long startTime = System.currentTimeMillis();
            Object result = point.proceed(point.getArgs());
            long endTime = System.currentTimeMillis();
            //类名
            String className = point.getTarget().getClass().getName();
            //方法名
            String methodName = point.getSignature().getName();
            String name = className + "." + methodName;
            String function = StringUtils.isBlank(worthAnno.function()) ? "其他" : worthAnno.function();
            try {
                mWorthEventService.put(newEvent(worthAnno.project().getName(), worthAnno.model().getName(), function, name, startTime, endTime));
            } catch (Exception e) {
                LOG.error("保存服务价值时间失败,methodName" + name + ",Args" + point.getArgs(), e);
            }
            return result;
        } else {
            return point.proceed(point.getArgs());
        }
    }

    public WorthEvent newEvent(String project, String model, String functionDesc, String name, long startTime, long endTime) {
        WorthEvent event = new WorthEvent(project, model, functionDesc, name);
        event.setEndTime(endTime);
        event.setStartTime(startTime);
        if (null == UserUtils.getUser()) {
            event.setOperationSource((String) ThreadContext.get("requestIp"));
            event.setOperationSourceType(OperationSourceType.SYSTEM);
        } else {
            event.setOperationSource(UserUtils.getUser().getLogin());
            event.setOperationSourceType(OperationSourceType.HUMAN);
        }
        String appkey = (String) ThreadContext.get("appkey");
        event.setTargetAppkey(appkey);
        event.setSignid("");
        return event;
    }

    private Worth getWorth(ProceedingJoinPoint point) {
        // 优先取method注解
        if (point.getSignature() instanceof MethodSignature) {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            if (method != null && method.getAnnotation(Worth.class) != null) {
                return method.getAnnotation(Worth.class);
            }
        }
        // 其次取class注解
        Worth classWorth = point.getTarget().getClass().getAnnotation(Worth.class);
        return classWorth;
    }
}
