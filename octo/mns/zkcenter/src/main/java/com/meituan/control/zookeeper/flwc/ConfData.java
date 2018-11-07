package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      ConfData用户描述conf命令返回数据的结构化：
 *      conf命令返回zk server的一组配置信息。
 * @configMap:
 *      zookeeper配置项的key-value对。
 */
public class ConfData {
    private Map<String, String> configMap = new HashMap<String, String>();

    public ConfData(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    @Override
    public String toString() {
        return "ConfData{" +
                "configMap=" + configMap +
                '}';
    }
}
