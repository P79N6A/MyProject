package com.meituan.control.zookeeper.monitor;

import java.util.Date;

/**
 * User: jinmengzhe
 * Date: 2015-07-23
 */
public class ZkMonitor {
    // primary key
    private String server = "";
    private Date timestamp = null;

    // 默认-1时表示该值不存在
    private int znode_count = -1;
    private int ephemerals_count = -1;
    private int watch_count = -1;
    private int num_alive_connections = -1;
    private int max_latency = -1;
    private int min_latency = -1;
    private int avg_latency = -1;
    private int packets_sent = -1;
    private int packets_received = -1;
    private int open_file_descriptor_count = -1;
    private int max_file_descriptor_count = -1;
    private int outstanding_requests = -1;
    private int approximate_data_size = -1;


    public boolean isLegal() {
        return (znode_count > -1)
                && (ephemerals_count > -1)
                && (watch_count > -1)
                && (num_alive_connections > -1)
                && (max_latency > -1)
                && (avg_latency > -1)
                && (min_latency > -1)
                && (packets_sent > -1)
                && (packets_received > -1)
                && (open_file_descriptor_count > -1)
                && (max_file_descriptor_count > -1)
                && (outstanding_requests > -1)
                && (approximate_data_size > -1);
    }


    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getZnode_count() {
        return znode_count;
    }

    public void setZnode_count(int znode_count) {
        this.znode_count = znode_count;
    }

    public int getEphemerals_count() {
        return ephemerals_count;
    }

    public void setEphemerals_count(int ephemerals_count) {
        this.ephemerals_count = ephemerals_count;
    }

    public int getWatch_count() {
        return watch_count;
    }

    public void setWatch_count(int watch_count) {
        this.watch_count = watch_count;
    }

    public int getNum_alive_connections() {
        return num_alive_connections;
    }

    public void setNum_alive_connections(int num_alive_connections) {
        this.num_alive_connections = num_alive_connections;
    }

    public int getMax_latency() {
        return max_latency;
    }

    public void setMax_latency(int max_latency) {
        this.max_latency = max_latency;
    }

    public int getMin_latency() {
        return min_latency;
    }

    public void setMin_latency(int min_latency) {
        this.min_latency = min_latency;
    }

    public int getAvg_latency() {
        return avg_latency;
    }

    public void setAvg_latency(int avg_latency) {
        this.avg_latency = avg_latency;
    }

    public int getPackets_sent() {
        return packets_sent;
    }

    public void setPackets_sent(int packets_sent) {
        this.packets_sent = packets_sent;
    }

    public int getPackets_received() {
        return packets_received;
    }

    public void setPackets_received(int packets_received) {
        this.packets_received = packets_received;
    }

    public int getOpen_file_descriptor_count() {
        return open_file_descriptor_count;
    }

    public void setOpen_file_descriptor_count(int open_file_descriptor_count) {
        this.open_file_descriptor_count = open_file_descriptor_count;
    }

    public int getMax_file_descriptor_count() {
        return max_file_descriptor_count;
    }

    public void setMax_file_descriptor_count(int max_file_descriptor_count) {
        this.max_file_descriptor_count = max_file_descriptor_count;
    }

    public int getOutstanding_requests() {
        return outstanding_requests;
    }

    public void setOutstanding_requests(int outstanding_requests) {
        this.outstanding_requests = outstanding_requests;
    }

    public int getApproximate_data_size() {
        return approximate_data_size;
    }

    public void setApproximate_data_size(int approximate_data_size) {
        this.approximate_data_size = approximate_data_size;
    }

    @Override
    public String toString() {
        return "ZkMonitor{" +
                "server='" + server + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", znode_count=" + znode_count +
                ", ephemerals_count=" + ephemerals_count +
                ", watch_count=" + watch_count +
                ", num_alive_connections=" + num_alive_connections +
                ", max_latency=" + max_latency +
                ", min_latency=" + min_latency +
                ", avg_latency=" + avg_latency +
                ", packets_sent=" + packets_sent +
                ", packets_received=" + packets_received +
                ", open_file_descriptor_count=" + open_file_descriptor_count +
                ", max_file_descriptor_count=" + max_file_descriptor_count +
                ", outstanding_requests=" + outstanding_requests +
                ", approximate_data_size=" + approximate_data_size +
                '}';
    }
}
