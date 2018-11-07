package com.sankuai.meituan.config.listener;

import com.sankuai.meituan.config.annotation.MtConfig;
import com.sankuai.meituan.config.util.AnnotationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * 注解支持的一般监听类
 * <p/>
 * Created by oulong on 14-5-20.
 */
public class AnnotationConfigChangeListener implements IConfigChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationConfigChangeListener.class);
    private Class config;

    public AnnotationConfigChangeListener(Class config) {
        this.config = config;
    }

    @Override
    public void changed(String key, String oldValue, String newValue) {
        Field[] fields = config.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.isAnnotationPresent(MtConfig.class)) {
                    MtConfig configParam = field.getAnnotation(MtConfig.class);
                    if (key.equals(configParam.key())) {
                        field.setAccessible(true);
                        field.set(config, AnnotationUtil.transferValueType(field, newValue));
                    }
                }
            } catch (Exception e) {
                LOG.error("Failed to annotation",e);
            }
        }
    }
}
