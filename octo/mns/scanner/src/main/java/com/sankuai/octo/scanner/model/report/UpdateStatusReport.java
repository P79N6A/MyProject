package com.sankuai.octo.scanner.model.report;

/**
 * Created by jiguang on 15/6/1.
 */
public class UpdateStatusReport extends ScannerReport {
    private int status;
    private int newStatus;

    public UpdateStatusReport(int level, String category, String content,
            int status, int newStatus, String identifier) {
        super(level, category, content, identifier);
        this.status = status;
        this.newStatus = newStatus;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(int newStatus) {
        this.newStatus = newStatus;
    }
}
