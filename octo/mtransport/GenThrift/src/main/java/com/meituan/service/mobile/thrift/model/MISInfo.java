package com.meituan.service.mobile.thrift.model;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-2-6
 * Time: 下午2:40
 */
public class MISInfo {

    private String login;
    private String name;
    private Long id;

    @Override
    public String toString() {
        return "MISInfo{" +
                "login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
