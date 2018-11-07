package com.sankuai.meituan.config.domain;

public class PullRequestDetail {
    private Integer prDetailId;

    private Integer prId;

    private String modifiedKey;

    private String oldValue;

    private String newValue;

    private String oldComment;

    private String newComment;

    private Boolean isDeleted;

    public Integer getPrDetailId() {
        return prDetailId;
    }

    public void setPrDetailId(Integer prDetailId) {
        this.prDetailId = prDetailId;
    }

    public Integer getPrId() {
        return prId;
    }

    public void setPrId(Integer prId) {
        this.prId = prId;
    }

    public String getModifiedKey() {
        return modifiedKey;
    }

    public void setModifiedKey(String modifiedKey) {
        this.modifiedKey = modifiedKey == null ? null : modifiedKey.trim();
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue == null ? null : oldValue.trim();
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue == null ? null : newValue.trim();
    }

    public String getOldComment() {
        return oldComment;
    }

    public void setOldComment(String oldComment) {
        this.oldComment = oldComment == null ? null : oldComment.trim();
    }

    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment == null ? null : newComment.trim();
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}