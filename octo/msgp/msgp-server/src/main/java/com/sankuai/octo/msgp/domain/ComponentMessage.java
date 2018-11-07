package com.sankuai.octo.msgp.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yves on 16/9/7.
 */
public class ComponentMessage implements Serializable {

    private int isTesting;

    private String subject;

    private String option_type;

    private List<String> message_type;

    private List<Dependency> dependencies;

    private List<String> wikis;

    private List<Dependency> recommend_dependencies;

    public ComponentMessage() {
    }

    public int getIsTesting() {
        return isTesting;
    }

    public void setIsTesting(int isTesting) {
        this.isTesting = isTesting;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOption_type() {
        return option_type;
    }

    public void setOption_type(String option_type) {
        this.option_type = option_type;
    }

    public List<String> getMessage_type() {
        return message_type;
    }

    public void setMessage_type(List<String> message_type) {
        this.message_type = message_type;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getWikis() {
        return wikis;
    }

    public void setWikis(List<String> wikis) {
        this.wikis = wikis;
    }

    public List<Dependency> getRecommend_dependencies() {
        return recommend_dependencies;
    }

    public void setRecommend_dependencies(List<Dependency> recommend_dependencies) {
        this.recommend_dependencies = recommend_dependencies;
    }
}
