package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      SrvrData用于描述srvr命令返回的结构化:
 *      srvr命令返回zk server的统计信息、如连接数/节点数等
 * @srvrMap:
 *      zk server统计信息的key-value对
 */
public class SrvrData {
    private Map<String, String> srvrMap = new HashMap<String, String>();

    public SrvrData(Map<String, String> srvrMap) {
        this.srvrMap = srvrMap;
    }

    public Map<String, String> getSrvrMap() {
        return srvrMap;
    }

    public void setSrvrMap(Map<String, String> srvrMap) {
        this.srvrMap = srvrMap;
    }

    @Override
    public String toString() {
        return "SrvrData{" +
                "srvrMap=" + srvrMap +
                '}';
    }
}
