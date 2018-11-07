package com.sankuai.meituan.config.interceptorfilter;

import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.service.ConfigNodeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Aspect
@Order(0)
public class ThriftAccessLogFilter{
	public static final Logger LOGGER = LoggerFactory.getLogger(ThriftAccessLogFilter.class);



	@Resource
    ConfigNodeService configNodeService;

    @Around("execution(public * com.sankuai.meituan.config.thrift.service.impl.*.*(..))")
    public Object doLog(ProceedingJoinPoint pjp) throws TException {
        long startTime = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            doAccessLog(pjp);
            return result;
        }catch (IllegalArgumentException e) { //一般由org.springframework.util.Assert这类检查动作抛出,一般不用管
            LOGGER.warn(MessageFormatter.arrayFormat("thrift调用失败, useTime:[{}], class:[{}], method:[{}], params:[{}]", new Object[]{System.currentTimeMillis() - startTime, pjp.getTarget().getClass(), pjp.getSignature().getName(), pjp.getArgs()}).getMessage(), e);
            throw new TException(e.getMessage());
        }catch (MtConfigException configException) {
            LOGGER.warn(MessageFormatter.arrayFormat("thrift调用失败, useTime:[{}], class:[{}], method:[{}], params:[{}]", new Object[]{System.currentTimeMillis() - startTime, pjp.getTarget().getClass(), pjp.getSignature().getName(), pjp.getArgs()}).getMessage(), configException);
            throw new TException(configException.getMessage());
        }catch(Throwable throwable) {
            LOGGER.warn(MessageFormatter.arrayFormat("thrift调用失败, useTime:[{}], class:[{}], method:[{}], params:[{}]", new Object[]{System.currentTimeMillis() - startTime, pjp.getTarget().getClass(), pjp.getSignature().getName(), pjp.getArgs()}).getMessage(), throwable);
            throw new TException(throwable.getMessage());
        }
    }

    public void doAccessLog(ProceedingJoinPoint pjp){
        String ip = ClientInfoUtil.getClientIp();
        if (!StringUtils.isEmpty(ip)) {
            LOGGER.info("client ip = {}, client appkey = {}, method = {}", ip, ClientInfoUtil.getClientAppKey(), pjp.getSignature().getName());
        }
    }

}
