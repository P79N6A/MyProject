package com.sankuai.inf.octo.mns.cache;

public class CacheMeta<V> {

    private V value;
    private long lastUpdateTime;
    private int failCount;
    private long updateInterval;

    public CacheMeta(V value) {
        this.value = value;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }
}
