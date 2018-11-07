package com.sankuai.meituan.config.model;

/**
 * Created by liangchen on 2017/10/24.
 */
public class UpdatedPropertyValue extends PropertyValue {
    protected String newValue;
    protected String newComment;

    public UpdatedPropertyValue() {
    }

    public UpdatedPropertyValue(PropertyValue propertyValue, String newValue, String newComment) {
        super(propertyValue.key, propertyValue.value, propertyValue.comment);
        this.newValue = newValue;
        this.newComment = newComment;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }
}
