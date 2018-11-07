package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: jinmengzhe
 * Date: 2015-06-17
 * Desc:
 *      WchpData用于描述wchp命令返回的结构化：
 *      wchp命令返回: 每个节点被哪些sessionId监听着
 * @watchedPath2SessionsMap:
 *      path->监听该path的sessionId集合
 */
public class WchpData {
    private Map<String, Set<String>> watchedPath2SessionsMap = new HashMap<String, Set<String>>();

    public WchpData(Map<String, Set<String>> watchedPath2SessionsMap) {
        this.watchedPath2SessionsMap = watchedPath2SessionsMap;
    }

    public Map<String, Set<String>> getWatchedPath2SessionsMap() {
        return watchedPath2SessionsMap;
    }

    public void setWatchedPath2SessionsMap(Map<String, Set<String>> watchedPath2SessionsMap) {
        this.watchedPath2SessionsMap = watchedPath2SessionsMap;
    }

    @Override
    public String toString() {
        return "WchpData{" +
                "watchedPath2SessionsMap=" + watchedPath2SessionsMap +
                '}';
    }
}
