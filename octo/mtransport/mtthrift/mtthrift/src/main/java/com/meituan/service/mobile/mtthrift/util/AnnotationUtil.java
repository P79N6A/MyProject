package com.meituan.service.mobile.mtthrift.util;

import com.facebook.swift.codec.metadata.ReflectionHelper;
import com.facebook.swift.service.ThriftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by jiguang on 15/7/28.
 */
public class AnnotationUtil {
    private final static Logger logger = LoggerFactory
            .getLogger(AnnotationUtil.class);


    public static boolean detectThriftAnnotation(Class<?> serviceClass){
        boolean annotated = false;
        Set<ThriftService> serviceAnnotations = ReflectionHelper
                .getEffectiveClassAnnotations(
                        serviceClass, ThriftService.class);
        if(null == serviceAnnotations || serviceAnnotations.size() == 0) {
            logger.debug("find no thrift annotation service");
        } else if( serviceAnnotations.size() == 1) {
            logger.debug("find annotated thrift service!");
            annotated = true;
        } else if(serviceAnnotations.size() > 1) {
            logger.error("Service class" + serviceClass.getName() +
                    "has multiple conflicting @ThriftService annotations:"
                    + serviceAnnotations);
            annotated = true;
        }
        return annotated;

    }
}
