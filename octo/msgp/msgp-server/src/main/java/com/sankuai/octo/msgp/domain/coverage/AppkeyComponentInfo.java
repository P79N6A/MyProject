package com.sankuai.octo.msgp.domain.coverage;

import java.sql.Date;

/**
 * Created by huoyanyu on 2017/7/18.
 */
public class AppkeyComponentInfo {
    private Date statdate;
    private String owt;
    private String appkey;
    private String componentName;
    private boolean isUsed;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Date getStatdate() {
        return statdate;
    }

    public void setStatdate(Date statdate) {
        this.statdate = statdate;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public String getOwt() {
        return owt;
    }

    public void setOwt(String owt) {
        this.owt = owt;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

}
