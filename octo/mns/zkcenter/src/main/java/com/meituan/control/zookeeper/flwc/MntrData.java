package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      MntrData用于描述mntr命令返回数据的结构化:
 *      mntr命令返回zk server的一些监控信息-以key-value对的形式
 * @monitorMap:
 *      监控项-监控值的key-value对
 */
public class MntrData {
    // mntr命令输出的监控项kv
    private Map<String, String> monitorMap = new HashMap<String, String>();

    public MntrData(Map<String, String> monitorMap) {
        this.monitorMap = monitorMap;
    }

    public Map<String, String> getMonitorMap() {
        return monitorMap;
    }

    public void setMonitorMap(Map<String, String> monitorMap) {
        this.monitorMap = monitorMap;
    }

    public boolean allItemsFully() {
        return (monitorMap.containsKey("zk_znode_count")
                && monitorMap.containsKey("zk_ephemerals_count")
                && monitorMap.containsKey("zk_watch_count")
                && monitorMap.containsKey("zk_num_alive_connections")
                && monitorMap.containsKey("zk_max_latency")
                && monitorMap.containsKey("zk_min_latency")
                && monitorMap.containsKey("zk_avg_latency")
                && monitorMap.containsKey("zk_packets_sent")
                && monitorMap.containsKey("zk_packets_received")
                && monitorMap.containsKey("zk_open_file_descriptor_count")
                && monitorMap.containsKey("zk_max_file_descriptor_count")
                && monitorMap.containsKey("zk_outstanding_requests")
                && monitorMap.containsKey("zk_approximate_data_size"));
    }

    @Override
    public String toString() {
        return "MntrData{" +
                "monitorMap=" + monitorMap +
                '}';
    }
}
