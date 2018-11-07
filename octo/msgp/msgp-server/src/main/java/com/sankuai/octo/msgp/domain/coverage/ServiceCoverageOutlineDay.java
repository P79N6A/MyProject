package com.sankuai.octo.msgp.domain.coverage;

import java.sql.Date;
import java.util.List;

/**
 * Created by huoyanyu on 2017/7/24.
 */
public class ServiceCoverageOutlineDay {
    private Date statdate;
    private Integer httpCount;
    private Integer javaCount;
    private Integer componentCount;

    public Date getStatdate() {
        return statdate;
    }

    public void setStatdate(Date statdate) {
        this.statdate = statdate;
    }

    public Integer getHttpCount() {
        return httpCount;
    }

    public void setHttpCount(Integer httpCount) {
        this.httpCount = httpCount;
    }

    public Integer getJavaCount() {
        return javaCount;
    }

    public void setJavaCount(Integer javaCount) {
        this.javaCount = javaCount;
    }

    public Integer getComponentCount() {
        return componentCount;
    }

    public void setComponentCount(Integer componentCount) {
        this.componentCount = componentCount;
    }
}
