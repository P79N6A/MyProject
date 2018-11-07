package com.meituan.service.mobile.mtthrift.util;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-6
 * Time: 下午4:01
 */
public class ThriftClassNameGenerator {

    public static String generateArgsClassName( String serviceName, String methodName ) {
        return ThriftUtil.generateMethodArgsClassName(serviceName, methodName);
    }

    public static String generateResultClassName( String serviceName, String methodName ) {
        return ThriftUtil.generateMethodResultClassName(serviceName, methodName);
    }

}
