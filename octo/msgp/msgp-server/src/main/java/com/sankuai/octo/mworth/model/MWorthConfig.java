package com.sankuai.octo.mworth.model;

import com.sankuai.octo.mworth.util.DateTimeUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Created by zava on 15/11/30.
 */
public class MWorthConfig {
    public final static boolean EFFECTIVED_DELETE = false;

    private Long id;
    private Long functionId;
    private String targetAppkey;
    private int worth = 0;
    private int primitiveCostTime = 0;
    @DateTimeFormat(pattern = DateTimeUtil.DATE_TIME_FORMAT)
    private Date fromTime;
    @DateTimeFormat(pattern = DateTimeUtil.DATE_TIME_FORMAT)
    private Date toTime;
    private boolean coverd = false;
    private boolean effectived = false;
    private Date createTime = new Date();

    public MWorthConfig() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }

    public String getTargetAppkey() {
        return targetAppkey;
    }

    public void setTargetAppkey(String targetAppkey) {
        this.targetAppkey = targetAppkey;
    }

    public int getWorth() {
        return worth;
    }

    public void setWorth(int worth) {
        this.worth = worth;
    }

    public int getPrimitiveCostTime() {
        return primitiveCostTime;
    }

    public void setPrimitiveCostTime(int primitiveCostTime) {
        this.primitiveCostTime = primitiveCostTime;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    public boolean isCoverd() {
        return coverd;
    }

    public void setCoverd(boolean coverd) {
        this.coverd = coverd;
    }

    public boolean isEffectived() {
        return effectived;
    }

    public void setEffectived(boolean effectived) {
        this.effectived = effectived;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    boolean deleted = false;


}
