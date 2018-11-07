package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: jinmengzhe
 * Date: 2015-06-16
 * Desc:
 *      DumpData用于描述dump命令输出的结构化:
 *      dump命令输出两种信息:1) 每个时间有哪些sessionId超时  2) 每个sessionId对应着哪些临时节点
 * @expiredSessionMap:
 *      time->该time超时的sessionId集合
 * @sessionEphemeralsMap:
 *      sessionId->该sessionId创建的临时节点
 */
public class DumpData {
    // time->expired-session-id-set
    private Map<String, Set<String>> expiredSessionMap = new HashMap<String, Set<String>>();
    // sessionid->Ephemerals
    private Map<String, Set<String>> sessionEphemeralsMap = new HashMap<String, Set<String>>();

    public DumpData(Map<String, Set<String>> expiredSessionMap, Map<String, Set<String>> sessionEphemeralsMap) {
        this.expiredSessionMap = expiredSessionMap;
        this.sessionEphemeralsMap = sessionEphemeralsMap;
    }


    public Map<String, Set<String>> getExpiredSessionMap() {
        return expiredSessionMap;
    }

    public void setExpiredSessionMap(Map<String, Set<String>> expiredSessionMap) {
        this.expiredSessionMap = expiredSessionMap;
    }

    public Map<String, Set<String>> getSessionEphemeralsMap() {
        return sessionEphemeralsMap;
    }

    public void setSessionEphemeralsMap(Map<String, Set<String>> sessionEphemeralsMap) {
        this.sessionEphemeralsMap = sessionEphemeralsMap;
    }

    @Override
    public String toString() {
        return "ExpiredSessionAndEphemerals{" +
                "expiredSessionMap=" + expiredSessionMap +
                ", sessionEphemeralsMap=" + sessionEphemeralsMap +
                '}';
    }
}
