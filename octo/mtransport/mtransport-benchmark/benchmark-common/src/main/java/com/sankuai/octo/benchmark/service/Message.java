package com.sankuai.octo.benchmark.service;

import java.io.Serializable;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-12-30
 * Time: 上午10:19
 */
public class Message implements Serializable{

    public int id;
    public String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
