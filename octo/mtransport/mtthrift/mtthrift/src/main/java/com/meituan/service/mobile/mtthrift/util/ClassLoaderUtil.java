package com.meituan.service.mobile.mtthrift.util;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-6
 * Time: 下午4:00
 */
public class ClassLoaderUtil {

    public static Class loadClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return org.apache.commons.lang.ClassUtils.getClass(classLoader, className);
    }

    public static Class loadClass(String className) throws ClassNotFoundException {
        return loadClass(null, className);
    }

    public static ClassLoader getCurrentClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            return classLoader;
        }
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        if (currentLoader != null) {
            return currentLoader;
        }
        return ClassLoaderUtil.class.getClassLoader();
    }

}
