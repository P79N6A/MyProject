package com.sankuai.meituan.config.interceptorfilter;


import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.meituan.config.constant.OperatorType;
import com.sankuai.meituan.config.model.Operator;
import com.sankuai.meituan.config.util.HttpUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

public class ApiOperationRecordInterceptor extends AbstractOperationRecordInterceptor{

    @Resource
    private HttpServletRequest request;

    @Override
    public Operator createOperator() {
        String ip = HttpUtil.getRealIp(request);
        String hostName = StringUtils.isEmpty(ip)?"":ProcessInfoUtil.getHostInfoByIp(ip);
        return new Operator(ip, hostName, OperatorType.API);
    }
}
