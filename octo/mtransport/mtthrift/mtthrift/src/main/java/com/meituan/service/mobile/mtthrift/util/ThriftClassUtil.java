package com.meituan.service.mobile.mtthrift.util;

import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * Created by jiguang on 15/3/9.
 */
public class ThriftClassUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftClassUtil.class);

    public static Class<?> getSynIfaceInterface(Class<?> thriftClass) {
        Class<?>[] classes = thriftClass.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && c.isInterface() && c.getSimpleName().equals("Iface")) {
                return c;
            }
        throw new IllegalArgumentException("thrift class must contain Sub Interface of Iface:");
    }

    public static Class<TProcessor> getProcessorClass(Class<?> thriftClass) {
        Class<?>[] classes = thriftClass.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && !c.isInterface() && c.getSimpleName().equals("Processor")) {
                return c;
            }
        throw new IllegalArgumentException("thrift class must contain sub class of Processor");
    }

    public static Constructor<TProcessor> getProcessorConstructorIface(Class<?> thriftClass) {
        try {
            return getProcessorClass(thriftClass).getConstructor(getSynIfaceInterface(thriftClass));
        } catch (Exception e) {
            throw new IllegalArgumentException("thriftClass must contain sub class of Processor with Constructor(Iface.class)", e);
        }
    }

    public static boolean validateThriftClass(Class<?> thriftClass) {
        try {
            getProcessorConstructorIface(thriftClass);
        } catch (Exception e) {
            LOG.debug("validateThriftClass failed...", e);
            return false;
        }
        return true;
    }
}
