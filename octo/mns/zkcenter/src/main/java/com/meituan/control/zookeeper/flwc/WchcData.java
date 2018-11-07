package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      WchcData用于描述wchc命令返回数据的结构化：
 *      wchc命令返回:每个session监听了那些节点
 * @session2WatchedPathMap:
 *      sessionId->该sessionId监听的节点集合
 */
public class WchcData {
    private Map<String, Set<String>> session2WatchedPathMap = new HashMap<String, Set<String>>();

    public WchcData(Map<String, Set<String>> session2WatchedPathMap) {
        this.session2WatchedPathMap = session2WatchedPathMap;
    }


    public Map<String, Set<String>> getSession2WatchedPathMap() {
        return session2WatchedPathMap;
    }

    public void setSession2WatchedPathMap(Map<String, Set<String>> session2WatchedPathMap) {
        this.session2WatchedPathMap = session2WatchedPathMap;
    }

    @Override
    public String toString() {
        return "WchcData{" +
                "session2WatchedPathMap=" + session2WatchedPathMap +
                '}';
    }
}
