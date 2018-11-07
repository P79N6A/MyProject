package com.sankuai.octo.msgp.domain.coverage;

import java.util.List;

/**
 * Created by huoyanyu on 2017/7/18.
 */
public class ServiceCoverage {
    private List<AppkeyComponentInfo> details;
    private ServiceCoverageOutline outline;

    @Override
    public String toString() {
        return "ServiceCoverage{" +
                "details=" + details +
                ", outline=" + outline +
                '}';
    }

    public List<AppkeyComponentInfo> getDetails() {
        return details;
    }

    public void setDetails(List<AppkeyComponentInfo> details) {
        this.details = details;
    }

    public ServiceCoverageOutline getOutline() {
        return outline;
    }

    public void setOutline(ServiceCoverageOutline outline) {
        this.outline = outline;
    }
}
