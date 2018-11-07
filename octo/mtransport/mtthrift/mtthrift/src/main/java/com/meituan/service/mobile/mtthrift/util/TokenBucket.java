package com.meituan.service.mobile.mtthrift.util;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-11-22
 * Time: 下午6:05
 */
public class TokenBucket {

    private long timeStamp;
    private int capacity;      //桶的最大容量
    private int rate;          //增加令牌的速率，最低qps为1
    private int tokens;        //桶中令牌的数量

    private int limitTime;

    public TokenBucket(int limitCount, int limitSecondsTime) {
        this.capacity = limitCount;
        this.limitTime = limitSecondsTime;

        this.tokens = capacity;
        this.timeStamp = System.currentTimeMillis();
        if (this.limitTime > 0)
            this.rate = this.capacity / this.limitTime;
        if (this.rate <= 0)
            this.rate = 1;
    }

    public synchronized boolean limit() {
        if (limitTime > 0) {
            long now = System.currentTimeMillis();
            long duration = (now - timeStamp) / 1000;
            if (duration > 1) {
                tokens = (int) Math.min(capacity, tokens + duration * rate);
                timeStamp = now;
            }
            if (tokens > 0) {
                tokens--;
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public int getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }

    @Override
    public String toString() {
        return "TokenBucket{" +
                "timeStamp=" + timeStamp +
                ", capacity=" + capacity +
                ", rate=" + rate +
                ", tokens=" + tokens +
                ", limitTime=" + limitTime +
                '}';
    }

    public static void main(String[] args) throws InterruptedException {

        //1分钟内不能访问超过600次，超过则需要等待令牌
        TokenBucket rateLimiter = new TokenBucket(600, 60);
        for (int i = 0; i < 10000; i++) {
            if (rateLimiter.limit()) {
                Thread.sleep(100);
                System.out.println("limited!");
            } else {
                Thread.sleep(10);
                System.out.println(rateLimiter.toString());
            }
        }
    }
}
