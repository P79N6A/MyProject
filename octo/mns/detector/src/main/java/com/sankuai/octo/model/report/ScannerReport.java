package com.sankuai.octo.model.report;

import com.sankuai.octo.Common;


public class ScannerReport {

    private String appkey = "";
    private String category = "";
    private int level = 0;
    private long time;
    private String content = "";
    private String identifier = "";

    public ScannerReport(String category, String content,
            String identifier) {
        this(0, category, content, identifier);
    }

    public ScannerReport(int level, String category, String content,
            String identifier) {
        this.appkey = Common.appkey;
        this.category = category;
        this.level = level;
        this.time = System.currentTimeMillis();
        this.content = content;
        this.identifier = identifier;
    }


    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
