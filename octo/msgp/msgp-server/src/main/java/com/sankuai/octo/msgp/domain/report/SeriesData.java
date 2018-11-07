package com.sankuai.octo.msgp.domain.report;

import java.util.List;

public class SeriesData {
    private List<WeekData> count;
    private List<WeekData> qps;
    private List<WeekData> successRatio;
    private List<WeekData> tp50;
    private List<WeekData> tp90;
    private List<WeekData> tp999;

    public SeriesData(List<WeekData> count, List<WeekData> qps, List<WeekData> successRatio,
                      List<WeekData> tp50, List<WeekData> tp90, List<WeekData> tp999) {
        this.count = count;
        this.qps = qps;
        this.successRatio = successRatio;
        this.tp50 = tp50;
        this.tp90 = tp90;
        this.tp999 = tp999;

    }

    public List<WeekData> getCount() {
        return count;
    }

    public void setCount(List<WeekData> count) {
        this.count = count;
    }

    public List<WeekData> getQps() {
        return qps;
    }

    public void setQps(List<WeekData> qps) {
        this.qps = qps;
    }

    public List<WeekData> getSuccessRatio() {
        return successRatio;
    }

    public void setSuccessRatio(List<WeekData> successRatio) {
        this.successRatio = successRatio;
    }

    public List<WeekData> getTp50() {
        return tp50;
    }

    public void setTp50(List<WeekData> tp50) {
        this.tp50 = tp50;
    }

    public List<WeekData> getTp90() {
        return tp90;
    }

    public void setTp90(List<WeekData> tp90) {
        this.tp90 = tp90;
    }

    public List<WeekData> getTp999() {
        return tp999;
    }

    public void setTp999(List<WeekData> tp999) {
        this.tp999 = tp999;
    }
}
