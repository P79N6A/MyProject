package com.sankuai.msgp.common.model;

/**
 * Created by yves on 17/1/3.
 */
public class User {
    private int id;
    private String login;
    private String name;

    public User(int id, String login, String name) {
        this.id = id;
        this.login = login;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
