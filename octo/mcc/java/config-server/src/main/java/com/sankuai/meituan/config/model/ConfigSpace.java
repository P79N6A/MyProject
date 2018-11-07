package com.sankuai.meituan.config.model;

/*
 * Copyright (c) 2010-2015 meituan.com
 * All rights reserved.
 * 
 */


import com.google.common.base.Objects;

/**
 * @Description 配置中心空间
 * 
 * @Author liuyunqiu
 * @Created 14-4-14
 * @Version 1.0
 */
public class ConfigSpace {
    private String name;

    public ConfigSpace() {
    }

    public ConfigSpace(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
          .add("name", name)
          .toString();
    }
}
