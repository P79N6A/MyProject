package com.sankuai.octo.msgp.domain.report;

import java.util.List;

public class DailyReportWrapper {
    private String appkey;
    private String owt;
    private DailyReportItem mainData;
    private List<DailyReportItem> spannameData;

    public DailyReportWrapper() {

    }

    public DailyReportWrapper(String appkey,String owt, DailyReportItem mainData, List<DailyReportItem> spannameData) {
        this.appkey = appkey;
        this.owt = owt;
        this.mainData = mainData;
        this.spannameData = spannameData;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public DailyReportItem getMainData() {
        return mainData;
    }

    public void setMainData(DailyReportItem mainData) {
        this.mainData = mainData;
    }

    public List<DailyReportItem> getSpannameData() {
        return spannameData;
    }

    public void setSpannameData(List<DailyReportItem> spannameData) {
        this.spannameData = spannameData;
    }


}
