package com.sankuai.octo.errorlog.model;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-6-12
 */
public class ErrorLogHourReport {
    private String hour;
    private Integer total;

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
