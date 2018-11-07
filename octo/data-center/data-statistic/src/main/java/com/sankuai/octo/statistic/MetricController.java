package com.sankuai.octo.statistic;

import com.sankuai.octo.statistic.helper.api;
import com.sankuai.octo.statistic.metric.AppKeyListActor;
import com.sankuai.octo.statistic.metric.AppKeyReceiveCountActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wujinwu on 16/5/15.
 */
@Controller
public class MetricController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/appkey/recv_count", method = RequestMethod.GET)
    @ResponseBody
    public String appkeyRecvCount() {
        return api.dataJson(AppKeyReceiveCountActor.Value());
    }

    @RequestMapping(value = "/appkey/list", method = RequestMethod.GET)
    @ResponseBody
    public String appkeyList() {
        return api.dataJson(AppKeyListActor.Value());
    }
}
