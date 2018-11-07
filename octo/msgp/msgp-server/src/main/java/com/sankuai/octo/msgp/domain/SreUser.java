package com.sankuai.octo.msgp.domain;

import java.util.List;

/**
 * @author uu
 * @description
 * @date Created in 17:33 2018/5/15
 * @modified
 */
public class SreUser {
    //命名对接sre接口，与接口名称保持一致
    private List<String> current_users;
    private List<String> new_users;
    private String link;
    private String service;
    private String type;
    private String desc;

    public SreUser() {
    }

    public SreUser(List<String> current_users, List<String> new_users, String link, String service, String type, String desc) {
        this.current_users = current_users;
        this.new_users = new_users;
        this.link = link;
        this.service = service;
        this.type = type;
        this.desc = desc;
    }

    public SreUser(List<String> current_users, String link, String service, String type, String desc) {
        this.current_users = current_users;
        this.link = link;
        this.service = service;
        this.type = type;
        this.desc = desc;
    }

    public List<String> getNew_users() {
        return new_users;
    }

    public void setNew_users(List<String> new_users) {
        this.new_users = new_users;
    }

    public List<String> getCurrent_users() {
        return current_users;
    }

    public void setCurrent_users(List<String> current_users) {
        this.current_users = current_users;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
