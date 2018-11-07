package com.meituan.service.mobile.mtthrift.util;

import com.facebook.swift.codec.metadata.ReflectionHelper;
import com.facebook.swift.service.ThriftService;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-6
 * Time: 下午3:58
 */
public class ThriftUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThriftUtil.class);

    public static boolean isSupportedThrift(Class<?> clazz) {
        return (isAnnotation(clazz) || isIDL(clazz));
    }

    public static boolean isAnnotation(Class<?> clazz) {
        Set<ThriftService> serviceAnnotations = ReflectionHelper
                .getEffectiveClassAnnotations(
                        clazz, ThriftService.class);

        if (serviceAnnotations.size() == 1) {
            return true;
        } else if (serviceAnnotations.size() > 1) {

            logger.error("Service class" + clazz.getName() +
                    "has multiple conflicting @ThriftService annotations:"
                    + serviceAnnotations);

        }
        return false;
    }

    public static boolean isIDL(Class<?> clazz) {
        String name = clazz.getName();
        int index = name.indexOf("$");
        String clazzType;
        if (index < 0) {
            return false;
        }
        clazzType = name.substring(0, index);
        Class<?> claz = null;
        try {
            claz = ClassLoaderUtil.loadClass(clazzType);
        } catch (ClassNotFoundException e) {
            return false;
        }

        Class<?>[] classes = claz.getClasses();

        Set<String> classNames = new HashSet<String>();

        for (Class c : classes) {
            classNames.add(c.getSimpleName());
        }

        if (classNames.contains("Iface") && classNames.contains("AsyncIface")
                && classNames.contains("Client") && classNames.contains("AsyncClient")
                && classNames.contains("Processor")) {
            return true;
        }

        return false;
    }


    public static String generateSetMethodName(String fieldName) {

        return new StringBuilder(16)
                .append("set")
                .append(Character.toUpperCase(fieldName.charAt(0)))
                .append(fieldName.substring(1))
                .toString();

    }

    public static String generateGetMethodName(String fieldName) {
        return new StringBuffer(16)
                .append("get")
                .append(Character.toUpperCase(fieldName.charAt(0)))
                .append(fieldName.substring(1))
                .toString();
    }

    public static String generateBoolMethodName(String fieldName) {
        return new StringBuffer(16)
                .append("is")
                .append(Character.toUpperCase(fieldName.charAt(0)))
                .append(fieldName.substring(1))
                .toString();
    }

    public static String generateMethodArgsClassName(String serviceName, String methodName) {

        return new StringBuilder(32)
                .append(serviceName)
                .append("$")
                .append(methodName)
                .append("_args")
                .toString();


    }

    public static String generateMethodResultClassName(String serviceName, String methodName) {

        return new StringBuilder(32)
                .append(serviceName)
                .append("$")
                .append(methodName)
                .append("_result")
                .toString();

    }


    public static  Object getDefaultResult(MethodInvocation methodInvocation) {
        Class<?> returnType = methodInvocation.getMethod().getReturnType();
        if (returnType.isPrimitive()) {
            String returnTypeName = returnType.getSimpleName();
            if ("boolean".equals(returnTypeName)) {
                return false;
            } else if ("char".equals(returnTypeName)) {
                return '0';
            } else if ("byte".equals(returnTypeName)) {
                return 0;
            } else if ("short".equals(returnTypeName)) {
                return 0;
            } else if ("int".equals(returnTypeName)) {
                return 0;
            } else if ("long".equals(returnTypeName)) {
                return 0;
            } else if ("float".equals(returnTypeName)) {
                return 0;
            } else if ("double".equals(returnTypeName)) {
                return 0;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
