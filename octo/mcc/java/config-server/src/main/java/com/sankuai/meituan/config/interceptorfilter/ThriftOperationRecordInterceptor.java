package com.sankuai.meituan.config.interceptorfilter;


import com.meituan.service.mobile.mtthrift.util.ClientInfoUtil;
import com.sankuai.meituan.config.constant.OperatorType;
import com.sankuai.meituan.config.model.Operator;
import org.apache.commons.lang3.StringUtils;

public class ThriftOperationRecordInterceptor extends AbstractOperationRecordInterceptor {
    @Override
    public Operator createOperator() {
        String clientIP = ClientInfoUtil.getClientIp();
        return new Operator("-1", StringUtils.isEmpty(clientIP) ? "SgAgent" : clientIP, OperatorType.MTTHRIFT);
    }
}
