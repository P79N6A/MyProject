package com.meituan.service.mobile.mtthrift.client.model;

import com.meituan.service.mobile.mtthrift.util.VersionComparator;

import java.io.Serializable;
import java.util.Date;

/**
 * User: YangXuehua
 * Date: 13-8-8
 * Time: 上午11:15
 */
public class Server implements Serializable {
    public static final int default_weight = 10;
    private static final long serialVersionUID = -2097689067316891538L;
    private static final String PIGEON_UNIPROTO_VERSION = "2.9.9";
    private static final String CTHRIFT_UNIPROTO_VERSION = "2.6.0";
    private static final String MTTHRIFT_NETTYSERVER_VERSION = "1.8.0";

    private String ip;
    private int port;
    private double weight = default_weight;
    private String serverAppKey;
    private Date startTime = new Date();
    // 目前仅被快速降权使用
    private float floating = 1.0f;
    private int status;

    private boolean unifiedProto = false;

    private int socketNullNum = 0;

    private ServerFeedback lastFeedback;

    private String version;
    private boolean nettyIOSupported;
    private byte heartbeatSupport = 0;
    private String swimlane;
    private String cell;

    public Server(String ip, int port) {
        this(ip, port, "");
    }

    public Server(String ip, int port, boolean unifiedProto) {
        this.ip = ip;
        this.port = port;
        this.unifiedProto = unifiedProto;
    }

    public Server(String ip, int port, String serverAppKey) {
        this(ip, port, serverAppKey, default_weight);
    }

    @Deprecated
    public Server(String ip, int port, String serverAppKey, int weight) {
        this.ip = ip;
        this.port = port;
        this.serverAppKey = serverAppKey;
        this.weight = weight;
    }

    public Server(String ip, int port, String serverAppKey, double weight) {
        this.ip = ip;
        this.port = port;
        this.serverAppKey = serverAppKey;
        this.weight = weight;
    }

    public Server(final String ip, final int port, final String serverAppKey,
                  final double weight, final boolean unifiedProto) {
        this.ip = ip;
        this.port = port;
        this.serverAppKey = serverAppKey;
        this.weight = weight;
        this.unifiedProto = unifiedProto;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public double getDoubleServerWeight() {
        return weight;
    }

    // 退化为 int
    public int getServerWeight() {
        return (int)weight;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Deprecated
    public Date getStartTime() {
        return startTime;
    }

    @Deprecated
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getServerAppKey() {
        return serverAppKey;
    }

    public void setServerAppKey(String serverAppKey) {
        this.serverAppKey = serverAppKey;
    }

    @Deprecated
    public float getFloating() {
        return floating;
    }


    @Deprecated
    public void setFloating(float floating) {
//        if (floating > 1.3f)
//            floating = 1.3f;
//        else if (floating < 0.6f)
//            floating = 0.6f;
        this.floating = floating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Server))
            return false;

        Server server = (Server) o;

        if (port != server.port)
            return false;
        if (!ip.equals(server.ip))
            return false;

        return true;
    }

    @Deprecated
    public ServerFeedback getLastFeedback() {
        return lastFeedback;
    }

    @Deprecated
    public void setLastFeedback(ServerFeedback lastFeedback) {
        this.lastFeedback = lastFeedback;
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        return result;
    }

    public void reduceFloating() {
        this.floating = this.floating / 3.f;
    }

    @Override
    public String toString() {
        return "Server{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", weight=" + weight +
                ", serverAppKey='" + serverAppKey + '\'' +
                ", status=" + status +
                ", socketNullNum=" + socketNullNum +
                ", version=" + version +
                ", swimlane=" + swimlane +
                ", cell=" + cell +
                '}';
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void degrade() {
        //降级，直接降低为下一个区间的最大值
        if (Double.compare(this.weight, 1.0d) >= 0)
            this.weight = 0.1d;
        else if (Double.compare(this.weight, 0.001d) >= 0)
            this.weight = 0.0001d;
        else if (Double.compare(this.weight, 0.000001d) >= 0)
            this.weight = 0.000001d;
    }

    public int getSocketNullNum() {
        return socketNullNum;
    }

    public void addSocketNullNum() {
        this.socketNullNum++;
    }

    public void resetSocketNullNum() {
        this.socketNullNum = 0;
    }

    public boolean isUnifiedProto() {
        return unifiedProto;
    }

    public void setUnifiedProto(boolean unifiedProto) {
        this.unifiedProto = unifiedProto;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;

        if (version != null ) {
            if (version.contains("mtthrift-v") && version.length() > 10) {
                String subVersion = version.substring(10);
                if (VersionComparator.compare(subVersion, MTTHRIFT_NETTYSERVER_VERSION) >= 0) {
                    this.nettyIOSupported = true;
                }
            }

            //cthrift 在2.6.0版本以后支持统一协议
            if (version.contains("cthrift-v") && version.length() > 9) {
                String subVersion = version.substring(9);
                if (VersionComparator.compare(subVersion, CTHRIFT_UNIPROTO_VERSION) >= 0) {
                    this.nettyIOSupported = true;
                }
            }

            //pigeon 在2.9.9版本以后支持统一协议
            if (version.matches("\\d*\\.\\d*\\.\\S*")) {
                if (VersionComparator.compare(version, PIGEON_UNIPROTO_VERSION) >= 0) {
                    this.nettyIOSupported = true;
                }
            }
        }
    }

    public boolean isNettyIOSupported() {
        return nettyIOSupported && unifiedProto;
    }

    public void setNettyIOSupported(boolean nettyIOSupported) {
        this.nettyIOSupported = nettyIOSupported;
    }

    public void setHeartbeatSupport(byte heartbeatSupport) {
        this.heartbeatSupport = heartbeatSupport;
    }

    public byte getHeartbeatSupport() {
        return heartbeatSupport;
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }
}
