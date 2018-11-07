package com.sankuai.octo.msgp.model.flowCopy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sankuai.msgp.common.utils.helper.JsonHelper;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MccNodeDataItem {
    private String key;
    private String value;
    private String comment;
    private String oriValue;
    private String oriComment;

    public MccNodeDataItem() {
    }

    public MccNodeDataItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public MccNodeDataItem(String key, String value, String comment) {
        this.key = key;
        this.value = value;
        this.comment = comment;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MccNodeDataItem that = (MccNodeDataItem) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value) && Objects
                .equals(comment, that.comment) && Objects.equals(oriValue, that.oriValue) && Objects
                .equals(oriComment, that.oriComment);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, value, comment, oriValue, oriComment);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MccNodeDataItem{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", oriValue='").append(oriValue).append('\'');
        sb.append(", oriComment='").append(oriComment).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(JsonHelper.jsonStr(new MccNodeDataItem("a", "b", "")));
    }
}

