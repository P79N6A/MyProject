package com.sankuai.octo.mworth;

import com.sankuai.meituan.auth.util.UserUtils;
import com.sankuai.octo.mworth.common.model.OperationSourceType;
import com.sankuai.octo.mworth.common.model.Worth;
import com.sankuai.octo.mworth.common.model.WorthEvent;
import com.sankuai.octo.mworth.service.WorthEventSevice;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by zava on 15/12/9.
 * 服务价值拦截器
 */
@Component
@Aspect
public class WorthAspect {
    private static final Logger logger = LoggerFactory.getLogger(WorthAspect.class);

    @Autowired
    private WorthEventSevice worthEventSevice;

    @Pointcut("@annotation(com.sankuai.octo.mworth.common.model.Worth) || within(@com.sankuai.octo.mworth.common.model.Worth *)")
    public void requireWorth() {
    }

    @Around("requireWorth()")
    public Object aroundWorth(ProceedingJoinPoint point) throws Throwable {
        Worth worthAnno = getWorth(point);
        if (worthAnno != null) {
            long startTime = System.currentTimeMillis();
            Object result = point.proceed(point.getArgs());
            long endTime = System.currentTimeMillis();
            //类名
            String className = point.getTarget().getClass().getName();
            //方法名
            String methodName = point.getSignature().getName();
            String name = className + "." + methodName;
            worthEventSevice.saveAsyn(newEvent(worthAnno.project().getName(), worthAnno.model().getName(),worthAnno.function(), name, startTime, endTime));
            return result;
        } else {
            return point.proceed(point.getArgs());
        }


    }

    public WorthEvent newEvent(String project, String model,String functionDesc, String name, long startTime, long endTime) {
        WorthEvent event = new WorthEvent(project, model,functionDesc, name);
        event.setEndTime(endTime);
        event.setStartTime(startTime);
        event.setOperationSource(UserUtils.getUser().getLogin());
        event.setOperationSourceType(OperationSourceType.HUMAN);
        event.setTargetAppkey("");
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
