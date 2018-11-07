package com.sankuai.octo.errorlog.model;

/**
 * @author zhangxi
 * @created 13-10-22
 */
public class ErrorLogReport {
    private String appkey;
    private String filterName;
    private Integer filterId;
    private Integer total;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public Integer getFilterId() {
        return filterId;
    }

    public void setFilterId(Integer filterId) {
        this.filterId = filterId;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
