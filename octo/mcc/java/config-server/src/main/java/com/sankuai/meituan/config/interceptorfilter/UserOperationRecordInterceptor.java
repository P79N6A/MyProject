package com.sankuai.meituan.config.interceptorfilter;


import com.sankuai.meituan.config.constant.OperatorType;
import com.sankuai.meituan.config.model.Operator;
import com.sankuai.meituan.filter.util.User;
import com.sankuai.meituan.filter.util.UserUtils;

public class UserOperationRecordInterceptor extends AbstractOperationRecordInterceptor {
    @Override
    public Operator createOperator() {
        User user = UserUtils.getUser();
        return (null == user) ? null : new Operator(Integer.valueOf(user.getId()).toString(), user.getName(), OperatorType.USER);
    }
}
