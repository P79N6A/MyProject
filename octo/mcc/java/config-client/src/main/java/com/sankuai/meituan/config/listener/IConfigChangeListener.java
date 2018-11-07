package com.sankuai.meituan.config.listener;

/**
 * 配置内容变更监听器
 *
 * @author yangguo03
 * @version 1.0
 * @created 14-4-25
 */
public interface IConfigChangeListener {

    /**
     * 配置改变时的回调方法
     */
    void changed(String key, String oldValue, String newValue);
}
