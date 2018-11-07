package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.Map;

/**
 * User: jinmengzhe
 * Date: 2015-06-16
 * Desc:
 *      ConnectedClient用于描述与当前zk server连接的客户端信息
 * @sessionid:
 *      该连接的sessionId
 * @ipport:
 *      该连接的ip port
 * @detailMap:
 *      该连接的一些统计信息、为一组key-value
 * Note:
 *      cons命令和stat命令的返回中都会包含客户端连接信息，cons的返回包含sid、ipport和detailMap。
 *      而stat命令只包含iport和detailMap、并且stat命令返回的detailMap没有cons返回的丰富(统计项更少一些)。
 */
public class ConnectedClient {
    // sid唯一代表一个Connection
    private String sid = "";
    private String ipPort = "";
    private Map<String, String> detailMap = new HashMap<String, String>();

    public ConnectedClient(String sid, String ipPort, Map<String, String> detailMap) {
        this.sid = sid;
        this.ipPort = ipPort;
        this.detailMap = detailMap;
    }


    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    public Map<String, String> getDetailMap() {
        return detailMap;
    }

    public void setDetailMap(Map<String, String> detailMap) {
        this.detailMap = detailMap;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "sid='" + sid + '\'' +
                ", ipPort='" + ipPort + '\'' +
                ", detailMap=" + detailMap +
                '}';
    }
}
