package com.sankuai.meituan.config.domain;

import java.util.Date;

public class Review {
    private Integer reviewId;

    private Integer prId;

    private String reviewerMisid;

    private String note;

    private Date reviewTime;

    private Integer approve;

    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public Integer getPrId() {
        return prId;
    }

    public void setPrId(Integer prId) {
        this.prId = prId;
    }

    public String getReviewerMisid() {
        return reviewerMisid;
    }

    public void setReviewerMisid(String reviewerMisid) {
        this.reviewerMisid = reviewerMisid == null ? null : reviewerMisid.trim();
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    public Integer getApprove() {
        return approve;
    }

    public void setApprove(Integer approve) {
        this.approve = approve;
    }
}