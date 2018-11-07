package com.sankuai.meituan.config.interceptorfilter;

import com.sankuai.meituan.config.anno.OperationRecord;
import com.sankuai.meituan.config.model.Operator;
import com.sankuai.meituan.config.service.OperationRecordService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

public abstract class AbstractOperationRecordInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractOperationRecordInterceptor.class);

    public static final ThreadLocal<Operator> operator = new ThreadLocal<Operator>();

    @Resource
    private OperationRecordService operationRecordService;

    public Object autoCreateOperation(ProceedingJoinPoint pjp, OperationRecord operationRecord) throws Throwable {
        Operator currentOperator = createOperator();
        operator.set(currentOperator);
        operationRecordService.createOperation(operationRecord, currentOperator);
        try {
            return pjp.proceed();
        } finally {
            operationRecordService.sendOperation();
        }
    }

    abstract Operator createOperator();
}
