package com.meituan.control.zookeeper.jmx;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jinmengzhe
 * Date: 2015-07-17
 */
public class ZkJmxDetail {
    private Map<String, String> inMemoryDataTreeMap = new HashMap<String, String>();
    private Map<String, String> leaderOrFollowerMap = new HashMap<String, String>();
    private Map<String, String> replicaMap = new HashMap<String, String>();
    private Map<String, String> replicatedServerMap = new HashMap<String, String>();

    public ZkJmxDetail(Map<String, String> inMemoryDataTreeMap, Map<String, String> leaderOrFollowerMap, Map<String, String> replicaMap, Map<String, String> replicatedServerMap) {
        this.inMemoryDataTreeMap = inMemoryDataTreeMap;
        this.leaderOrFollowerMap = leaderOrFollowerMap;
        this.replicaMap = replicaMap;
        this.replicatedServerMap = replicatedServerMap;
    }

    public Map<String, String> getInMemoryDataTreeMap() {
        return inMemoryDataTreeMap;
    }

    public void setInMemoryDataTreeMap(Map<String, String> inMemoryDataTreeMap) {
        this.inMemoryDataTreeMap = inMemoryDataTreeMap;
    }

    public Map<String, String> getLeaderOrFollowerMap() {
        return leaderOrFollowerMap;
    }

    public void setLeaderOrFollowerMap(Map<String, String> leaderOrFollowerMap) {
        this.leaderOrFollowerMap = leaderOrFollowerMap;
    }

    public Map<String, String> getReplicatedServerMap() {
        return replicatedServerMap;
    }

    public void setReplicatedServerMap(Map<String, String> replicatedServerMap) {
        this.replicatedServerMap = replicatedServerMap;
    }

    public Map<String, String> getReplicaMap() {
        return replicaMap;
    }

    public void setReplicaMap(Map<String, String> replicaMap) {
        this.replicaMap = replicaMap;
    }

    @Override
    public String toString() {
        return "ZkJmxDetail{" +
                "inMemoryDataTreeMap=" + inMemoryDataTreeMap +
                ", leaderOrFollowerMap=" + leaderOrFollowerMap +
                ", replicatedServerMap=" + replicatedServerMap +
                ", replicaMap=" + replicaMap +
                '}';
    }
}
