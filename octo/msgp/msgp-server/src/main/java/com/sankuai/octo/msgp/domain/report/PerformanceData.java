package com.sankuai.octo.msgp.domain.report;

/**
 * Wow : week-over-week 同比,  day-over-day: 环比
 */
public class PerformanceData {
    private String name = "";
    private double value = 0;
    private double doD = 0;
    private double woW = 0;

    public PerformanceData() {

    }

    public PerformanceData(String name, double value, double doD, double woW) {
        this.name = name;
        this.value = value;
        this.doD = doD;
        this.woW = woW;
    }

    public PerformanceData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getDoD() {
        return doD;
    }

    public void setDoD(double doD) {
        this.doD = doD;
    }

    public double getWoW() {
        return woW;
    }

    public void setWoW(double woW) {
        this.woW = woW;
    }
}
