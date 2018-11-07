package com.sankuai.octo.msgp.domain.report;

import java.util.List;

public class WeekData {

    private String name;
    private List<Double> data;

    public WeekData(String name, List<Double> data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }
}
