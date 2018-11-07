package com.sankuai.meituan.config.listener;

import java.util.Map;

/**
 * client的全局配置变更监听器,client的任意一个配置项改变时都会触发
 */
public interface IGlobalConfigChangeListener {
	void changed(Map<String, String> oldData, Map<String, String> newData);
}
