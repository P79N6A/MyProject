package com.meituan.service.mobile.mtthrift.degrage;

import org.apache.thrift.TException;

/**
 * Author: caojiguang@gmail.com
 * Date: 15/8/13
 * Description:
 */
public class ServiceDegradeException extends TException {


    public ServiceDegradeException() {
        this("Service degrade exception");
    }

    public ServiceDegradeException(String msg) {
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
