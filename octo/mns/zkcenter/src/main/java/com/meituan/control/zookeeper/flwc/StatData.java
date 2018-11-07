package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      StatData用于描述stat命令返回数据的结构化：
 *      stat命令返回2方面信息: 1) 与当前server连接的client集合 2) 当前server的统计信息
 *      --stat返回的内容大致可以理解为包含了cons和srvr两个命令的返回
 * @clientSet:
 *      与zk server连接的客户端集合
 *      类似cons返回
 * @statisticMap:
 *      zk server的统计信息
 *      类似srvr返回
 */
public class StatData {
    // 复用cons的表示方法
    private Set<ConnectedClient> clientSet = new HashSet<ConnectedClient>();
    // 统计项
    private Map<String, String> statisticMap = new HashMap<String, String>();

    public StatData(Set<ConnectedClient> clientSet, Map<String, String> statisticMap) {
        this.clientSet = clientSet;
        this.statisticMap = statisticMap;
    }


    public Set<ConnectedClient> getClientSet() {
        return clientSet;
    }

    public void setClientSet(Set<ConnectedClient> clientSet) {
        this.clientSet = clientSet;
    }

    public Map<String, String> getStatisticMap() {
        return statisticMap;
    }

    public void setStatisticMap(Map<String, String> statisticMap) {
        this.statisticMap = statisticMap;
    }

    @Override
    public String toString() {
        return "StatData{" +
                "clientSet=" + clientSet +
                ", statisticMap=" + statisticMap +
                '}';
    }
}
