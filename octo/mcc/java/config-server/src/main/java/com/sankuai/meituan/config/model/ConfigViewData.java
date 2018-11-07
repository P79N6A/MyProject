package com.sankuai.meituan.config.model;

public class ConfigViewData extends PropertyValue {
    protected String oriValue;
    protected String oriComment;

    public ConfigViewData() {
    }

    public ConfigViewData(String key, String value, String comment, String oriValue, String oriComment) {
        super(key, value, comment);
        this.oriValue = oriValue;
        this.oriComment = oriComment;
    }

    public static ConfigViewData fromOri(PropertyValue oriValue) {
        return new ConfigViewData(oriValue.key, null, null, oriValue.value, oriValue.comment);
    }

    public static ConfigViewData fromCurrent(PropertyValue currentValue) {
        return new ConfigViewData(currentValue.key, currentValue.value, currentValue.comment, null, null);
    }

    public void copyOri(PropertyValue oriValue) {
        this.key = oriValue.key;
        this.oriValue = oriValue.value;
        this.oriComment = oriValue.comment;
    }

    public void copyCurrent(PropertyValue currentValue) {
        this.key = currentValue.key;
        this.value = currentValue.value;
        this.comment = currentValue.comment;
    }

    public String getOriValue() {
        return oriValue;
    }

    public void setOriValue(String oriValue) {
        this.oriValue = oriValue;
    }

    public String getOriComment() {
        return oriComment;
    }

    public void setOriComment(String oriComment) {
        this.oriComment = oriComment;
    }
}
