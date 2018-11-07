package com.sankuai.octo.msgp.model.reqpolicy;

/**
 * Created by songjianjian on 2018/5/31.
 */
public class TagInfo {

    private Long id;

    private Long policyId;

    private String appkey;

    private String env;

    private String region;

    private String idc;

    private String swimlane;

    private String cell;

    private String preTasks;

    private Integer scaleResult;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env == null ? null : env.trim();
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region == null ? null : region.trim();
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc == null ? null : idc.trim();
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane == null ? null : swimlane.trim();
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell == null ? null : cell.trim();
    }

    public String getPreTasks() {
        return preTasks;
    }

    public void setPreTasks(String preTasks) {
        this.preTasks = preTasks == null ? null : preTasks.trim();
    }

    public Integer getScaleResult() {
        return scaleResult;
    }

    public void setScaleResult(Integer scaleResult) {
        this.scaleResult = scaleResult;
    }
}
