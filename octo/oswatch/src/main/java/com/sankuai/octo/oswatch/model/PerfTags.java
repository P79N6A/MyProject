package com.sankuai.octo.oswatch.model;

/**
 * Created by chenxi on 6/17/15.
 */
public class PerfTags {
    String source;
    String remoteApp;
    String spanname;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {

        this.source = source;
    }

    public String getRemoteApp() {
        return remoteApp;
    }

    public void setRemoteApp(String remoteApp) {
        this.remoteApp = remoteApp;
    }

    public String getSpanname() {
        return spanname;
    }

    public void setSpanname(String spanname) {
        this.spanname = spanname;
    }
}
