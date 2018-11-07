package com.sankuai.octo.falcon;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-11-26
 * Time: 上午11:07
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
