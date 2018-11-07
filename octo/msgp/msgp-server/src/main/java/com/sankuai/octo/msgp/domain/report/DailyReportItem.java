package com.sankuai.octo.msgp.domain.report;

public class DailyReportItem {
    private String appkey = "";
    private String spanname = "";
    private Boolean status = true;
    private Double successRatio = 0.0;
    private String aliveRatio = "NaN, NaN";
    private PerformanceData count = new PerformanceData("count");
    private PerformanceData qps = new PerformanceData("qps");
    private PerformanceData tp50 = new PerformanceData("tp50");
    private PerformanceData tp90 = new PerformanceData("tp90");
    private PerformanceData tp95 = new PerformanceData("tp95");
    private PerformanceData tp99 = new PerformanceData("tp99");
    private PerformanceData tp999 = new PerformanceData("tp999");
    private PerformanceData errorCount = new PerformanceData("errorCount");
    private PerformanceData perfAlert = new PerformanceData("perfAlert");
    private int isLoadBalance = 0;

    public DailyReportItem() {

    }

    public DailyReportItem(String appkey, String spanname, Boolean status, Double successRatio, String aliveRatio, PerformanceData count, PerformanceData qps, PerformanceData tp50, PerformanceData tp90, PerformanceData tp95, PerformanceData tp99, PerformanceData tp999, PerformanceData errorCount, PerformanceData perfAlert, int isLoadBalance) {
        this.appkey = appkey;
        this.spanname = spanname;
        this.status = status;
        this.successRatio = successRatio;
        this.aliveRatio = aliveRatio;
        this.count = count;
        this.qps = qps;
        this.tp50 = tp50;
        this.tp90 = tp90;
        this.tp95 = tp95;
        this.tp99 = tp99;
        this.tp999 = tp999;
        this.errorCount = errorCount;
        this.perfAlert = perfAlert;
        this.isLoadBalance = isLoadBalance;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getSpanname() {
        return spanname;
    }

    public void setSpanname(String spanname) {
        this.spanname = spanname;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Double getSuccessRatio() {
        return successRatio;
    }

    public void setSuccessRatio(Double successRatio) {
        this.successRatio = successRatio;
    }

    public String getAliveRatio() {
        return aliveRatio;
    }

    public void setAliveRatio(String aliveRatio) {
        this.aliveRatio = aliveRatio;
    }

    public PerformanceData getCount() {
        return count;
    }

    public void setCount(PerformanceData count) {
        this.count = count;
    }

    public PerformanceData getQps() {
        return qps;
    }

    public void setQps(PerformanceData qps) {
        this.qps = qps;
    }

    public PerformanceData getTp50() {
        return tp50;
    }

    public void setTp50(PerformanceData tp50) {
        this.tp50 = tp50;
    }

    public PerformanceData getTp90() {
        return tp90;
    }

    public void setTp90(PerformanceData tp90) {
        this.tp90 = tp90;
    }

    public PerformanceData getTp95() {
        return tp95;
    }

    public void setTp95(PerformanceData tp95) {
        this.tp95 = tp95;
    }

    public PerformanceData getTp99() {
        return tp99;
    }

    public void setTp99(PerformanceData tp99) {
        this.tp99 = tp99;
    }

    public PerformanceData getTp999() {
        return tp999;
    }

    public void setTp999(PerformanceData tp999) {
        this.tp999 = tp999;
    }

    public PerformanceData getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(PerformanceData errorCount) {
        this.errorCount = errorCount;
    }

    public PerformanceData getPerfAlert() {
        return perfAlert;
    }

    public void setPerfAlert(PerformanceData perfAlert) {
        this.perfAlert = perfAlert;
    }

    public int getIsLoadBalance() {
        return isLoadBalance;
    }

    public void setIsLoadBalance(int isLoadBalance) {
        this.isLoadBalance = isLoadBalance;
    }


}
