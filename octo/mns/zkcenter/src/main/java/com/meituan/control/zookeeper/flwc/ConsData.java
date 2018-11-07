package com.meituan.control.zookeeper.flwc;

import java.util.HashSet;
import java.util.Set;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      ConsData用于描述cons命令返回数据的结构化：
 *      cons命令返回当前与zk server连接的所有客户端信息
 * @connectedClientSet:
 *      一组客户端连接的信息，参见ConnectedClient的定义
 */
public class ConsData {
    private Set<ConnectedClient> connectedClientSet = new HashSet<ConnectedClient>();

    public ConsData(Set<ConnectedClient> connectedClientSet) {
        this.connectedClientSet = connectedClientSet;
    }

    public Set<ConnectedClient> getConnectedClientSet() {
        return connectedClientSet;
    }

    public void setConnectedClientSet(Set<ConnectedClient> connectedClientSet) {
        this.connectedClientSet = connectedClientSet;
    }

    @Override
    public String toString() {
        return "ConsData{" +
                "connectedClientSet=" + connectedClientSet +
                '}';
    }
}
