package com.sankuai.octo.scanner.model.report;

/**
 * Created by jiguang on 15/6/1.
 */
public class RoundTimeReport extends ScannerReport {
    private int roundTime;

    public RoundTimeReport(int level, String category, String content,
            int roundTime, String identifier) {
        super(level, category, content, identifier);
        this.roundTime = roundTime;
    }

    public int getRoundTime() {
        return roundTime;
    }

    public void setRoundTime(int roundTime) {
        this.roundTime = roundTime;
    }

}
