package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      EnviData用于描述envi命令返回数据的结构化：
 *      envi命令返回zk server所在机器上的环境变量信息
 * @enviMap:
 *      环境变量的key-value对
 */
public class EnviData {
    Map<String, String> enviMap = new HashMap<String, String>();

    public EnviData(Map<String, String> enviMap) {
        this.enviMap = enviMap;
    }

    public Map<String, String> getEnviMap() {
        return enviMap;
    }

    public void setEnviMap(Map<String, String> enviMap) {
        this.enviMap = enviMap;
    }

    @Override
    public String toString() {
        return "EnviData{" +
                "enviMap=" + enviMap +
                '}';
    }
}
