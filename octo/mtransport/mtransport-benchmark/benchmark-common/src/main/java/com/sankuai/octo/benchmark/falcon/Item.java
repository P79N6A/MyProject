package com.sankuai.octo.benchmark.falcon;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-6
 * Time: 下午2:43
 */
public class Item {

    String key;
    String value;
    long mills;

    public Item(String key, String value, long mills) {
        this.key = key;
        this.value = value;
        this.mills = mills;
    }

}
