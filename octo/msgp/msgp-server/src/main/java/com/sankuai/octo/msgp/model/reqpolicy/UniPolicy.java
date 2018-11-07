package com.sankuai.octo.msgp.model.reqpolicy;

/**
 * Created by songjianjian on 2018/6/1.
 */
public class UniPolicy {
    private Long id;

    private String appkey;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    private String env;

    private Integer state;

    private Integer peakShrink;

    private String metricsBound;

    private Integer coolingTime;

    private String tags;

    private Long createTime;

    private Long updateTime;

    private Integer channelType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getPeakShrink() {
        return peakShrink;
    }

    public void setPeakShrink(Integer peakShrink) {
        this.peakShrink = peakShrink;
    }

    public String getMetricsBound() {
        return metricsBound;
    }

    public void setMetricsBound(String metricsBound) {
        this.metricsBound = metricsBound == null ? null : metricsBound.trim();
    }

    public Integer getCoolingTime() {
        return coolingTime;
    }

    public void setCoolingTime(Integer coolingTime) {
        this.coolingTime = coolingTime;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags == null ? null : tags.trim();
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getChannelType() {
        return channelType;
    }

    public void setChannelType(Integer channelType) {
        this.channelType = channelType;
    }
}
