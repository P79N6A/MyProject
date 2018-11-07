package com.sankuai.octo.msgp.domain.coverage;

import java.sql.Date;
import java.util.List;

/**
 * Created by huoyanyu on 2017/7/18.
 */
public class ServiceCoverageOutline {
    private List<ServiceCoverageOutlinePeriod> outlinePeriods;
    private List<Date> dates;


    public List<ServiceCoverageOutlinePeriod> getOutlinePeriods() {
        return outlinePeriods;
    }

    public void setOutlinePeriods(List<ServiceCoverageOutlinePeriod> outlinePeriods) {
        this.outlinePeriods = outlinePeriods;
    }

    public List<Date> getDates() {
        return dates;
    }

    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

    @Override
    public String toString() {
        return "ServiceCoverageOutline{" +
                "outlinePeriods=" + outlinePeriods +
                ", dates=" + dates +
                '}';
    }
}
