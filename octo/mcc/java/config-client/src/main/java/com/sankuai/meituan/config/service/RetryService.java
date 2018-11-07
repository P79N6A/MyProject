package com.sankuai.meituan.config.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO 以后有需要可以参考com.netflix.curator.RetryPolicy做一个重试策略的机制
public class RetryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryService.class);

    public <RESULT_TYPE> RESULT_TYPE doWithRetry(Supplier<RESULT_TYPE> invoker, Predicate<RESULT_TYPE> isSuccess, Function<RESULT_TYPE, ? extends RuntimeException> callWhenFail) {
        RESULT_TYPE lastResult = null;
        for (int tryTimes = 0; tryTimes < 2; ++ tryTimes) {
            if(tryTimes>0){
                try{
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    LOGGER.warn("Thread sleep fail.",e);
                }
            }
            lastResult = invoker.get();
            if (isSuccess.apply(lastResult)) {
                return lastResult;
            }
        }
        throw callWhenFail.apply(lastResult);
    }
}
