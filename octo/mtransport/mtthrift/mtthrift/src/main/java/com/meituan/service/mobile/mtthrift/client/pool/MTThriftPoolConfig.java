package com.meituan.service.mobile.mtthrift.client.pool;

import org.apache.commons.pool.impl.GenericObjectPool.Config;

/**
 * @author YaoZhidong
 * @version 1.0
 * @created 13-1-17
 */
public class MTThriftPoolConfig extends Config {
    private static final int cores = Runtime.getRuntime().availableProcessors();
    public static final int DEFAULT_MAX_ACTIVE = cores * 50;
    public static final int DEFAULT_INITIAL_SIZE = 1;
    public static final int DEFAULT_NORMAL_SIZE = DEFAULT_INITIAL_SIZE;
    public static final int DEFAULT_MAX_WAIT = 1000;
    public static final int DEFAULT_WRITE_HIGH_WATER_MARK = 64 * 1024;
    public static final int DEFAULT_WRITE_LOW_WATER_MARK = 32 * 1024;
    private int initialSize = DEFAULT_INITIAL_SIZE;
    private int normalSize = DEFAULT_NORMAL_SIZE;
    private int eventLoopSize = 0;
    private int writeHighWaterMark;
    private int writeLowWaterMark;

    public MTThriftPoolConfig() {
        setTestWhileIdle(true);
        setTimeBetweenEvictionRunsMillis(30000);
        setMaxWait(DEFAULT_MAX_WAIT);
        setMaxActive(DEFAULT_MAX_ACTIVE);
        setWriteHighWaterMark(DEFAULT_WRITE_HIGH_WATER_MARK);
        setWriteLowWaterMark(DEFAULT_WRITE_LOW_WATER_MARK);
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getNormalSize() {
        return normalSize;
    }

    public void setNormalSize(int normalSize) {
        this.normalSize = normalSize;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public byte getWhenExhaustedAction() {
        return whenExhaustedAction;
    }

    public void setWhenExhaustedAction(byte whenExhaustedAction) {
        this.whenExhaustedAction = whenExhaustedAction;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public long getSoftMinEvictableIdleTimeMillis() {
        return softMinEvictableIdleTimeMillis;
    }

    public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
        this.softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
    }

    public int getEventLoopSize() {
        return eventLoopSize;
    }

    public void setEventLoopSize(int eventLoopSize) {
        this.eventLoopSize = eventLoopSize;
    }

    public int getWriteHighWaterMark() {
        return writeHighWaterMark;
    }

    public void setWriteHighWaterMark(Integer writeHighWaterMark) {
        this.writeHighWaterMark = writeHighWaterMark;
    }

    public int getWriteLowWaterMark() {
        return writeLowWaterMark;
    }

    public void setWriteLowWaterMark(Integer writeLowWaterMark) {
        this.writeLowWaterMark = writeLowWaterMark;
    }
}
