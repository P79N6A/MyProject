package com.sankuai.octo.falcon;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-11-25
 * Time: 下午10:59
 */
public class FalconItem {

    //扫描sg_agent失败数
    public static AtomicInteger failNum;

    //扫描sg_agent ping失败数(可能是机器下线了)
    public static AtomicInteger pingFailNum;

    //扫描sg_agent总数
    public static AtomicInteger totalNum;

    //扫描sg_agent成功数
    public static int successNum;

    //扫描sg_agent成功率
    public static float successRate;

    public static AtomicInteger providerTotalNum = new AtomicInteger(0);

    public static AtomicInteger providerFailNum = new AtomicInteger(0);

}
