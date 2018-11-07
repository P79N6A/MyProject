package com.meituan.service.mobile.mtthrift.client.cell;


import org.aopalliance.intercept.MethodInvocation;
import java.lang.reflect.Method;


public class RouterMetaData {

    private MethodInvocation methodInvocation;
    private Method method;
    private Object[] args;

    public RouterMetaData() {
    }

    public RouterMetaData(MethodInvocation methodInvocation, Method method, Object[] args) {
        this.methodInvocation = methodInvocation;
        this.method = method;
        this.args = args;
    }

    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public void setMethodInvocation(MethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
