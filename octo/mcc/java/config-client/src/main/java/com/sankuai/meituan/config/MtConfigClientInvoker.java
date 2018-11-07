package com.sankuai.meituan.config;

import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.meituan.config.listener.IGlobalConfigChangeListener;

import java.util.Map;
import java.util.Set;

public interface MtConfigClientInvoker {
    void init() throws MtConfigException;

    void destroy();

    void addListener(String key, IConfigChangeListener listener);

    void removeListener(String key, IConfigChangeListener listener);

    String getValue(String key);

    Map<String, String> getAllKeyValues();

    Set<String> getAllKeys();

    Boolean setValue(String key, String value);

    @Deprecated
    Boolean setValue(String key, String value, String nodeName);

    void setPullPeriod(long pullPeriod);

    void setScanBasePackage(String scanBasePackage);

    void setGlobalConfigChangeListener(IGlobalConfigChangeListener globalConfigChangeListener);

    void setId(String id);
}
