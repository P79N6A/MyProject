package com.meituan.dorado.common.extension;

import com.meituan.dorado.common.Role;
import com.meituan.dorado.common.RpcRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExtensionLoader {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private static Map<Class<?>, Object> extensionMap = new ConcurrentHashMap<Class<?>, Object>();

    private static Map<Class<?>, List<?>> extensionListMap = new ConcurrentHashMap<Class<?>, List<?>>();

    private ExtensionLoader() {
    }

    public static <T> T getExtension(Class<T> clazz) {
        T extension = (T) extensionMap.get(clazz);
        if (extension == null) {
            extension = newExtension(clazz);
            if (extension != null) {
                extensionMap.put(clazz, extension);
            } else {
                throw new RuntimeException("No implement of " + clazz.getName() + ", please check spi config");
            }
        }
        return extension;
    }

    public static <T> T getNewExtension(Class<T> clazz) {
        T extension = newExtension(clazz);
        if (extension == null) {
            throw new RuntimeException("No implement of " + clazz.getName() + ", please check spi config");
        }
        return extension;
    }

    public static <T> List<T> getExtensionList(Class<T> clazz) {
        List<T> extensions = (List<T>) extensionListMap.get(clazz);
        if (extensions == null) {
            extensions = newExtensionList(clazz);
            if (!extensions.isEmpty()) {
                extensionListMap.put(clazz, extensions);
            }
        }
        return extensions;
    }

    private static <T> T newExtension(Class<T> clazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
        for (T service : serviceLoader) {
            return service;
        }
        return null;
    }

    public static <T> List<T> newExtensionList(Class<T> clazz) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
        List<T> extensions = new ArrayList<T>();
        for (T service : serviceLoader) {
            extensions.add(service);
        }
        return extensions;
    }

    /**
     * 根据实现类名的前缀获取实现类
     *
     * @param clazz
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T getExtensionWithName(Class<T> clazz, String name) {
        List<T> extensions = (List<T>) extensionListMap.get(clazz);
        if (extensions == null) {
            extensions = newExtensionList(clazz);
            if (!extensions.isEmpty()) {
                extensionListMap.put(clazz, extensions);
            }
        }
        String interfaceName = clazz.getSimpleName();
        for (T extension : extensions) {
            String className = extension.getClass().getSimpleName();
            String subClassPrefix = className.substring(0, className.indexOf(interfaceName));
            if (name.equalsIgnoreCase(subClassPrefix)) {
                return extension;
            }
        }
        return null;
    }

    public static <T extends Role> List<T> getExtensionListByRole(Class<T> clazz, RpcRole role) {
        try {
            List<T> extensions = getExtensionList(clazz);
            List<T> result = new LinkedList<T>();
            for (T t : extensions) {
                if (t != null && t.getRole() == null) {
                    throw new RuntimeException("Class " + t.getClass().getName() + " rpcRole is null, please check getRole function.");
                }
                if (t != null && (t.getRole().equals(role) || t.getRole().equals(RpcRole.MULTIROLE))) {
                    result.add(t);
                }
            }
            return result;
        } catch (Throwable e) {
            logger.error("Get extensions of {} failed.", clazz.getName(), e);
            throw new RuntimeException(e);
        }
    }

    public static <T extends Role> T getExtensionByRole(Class<T> clazz, RpcRole role) {
        try {
            List<T> extensions = getExtensionList(clazz);
            for (T t : extensions) {
                if (t != null && t.getRole() == null) {
                    throw new RuntimeException("Class " + t.getClass().getName() + " rpcRole is null, please check getRole function.");
                }
                if (t != null && (t.getRole().equals(role) || t.getRole().equals(RpcRole.MULTIROLE))) {
                    return t;
                }
            }
            return null;
        } catch (Throwable e) {
            logger.error("Get extension of {} failed.", clazz.getName(), e);
            throw new RuntimeException(e);
        }
    }
}
