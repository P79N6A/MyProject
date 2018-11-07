package com.sankuai.octo.doclet.octo;

import com.sankuai.octo.doclet.util.HttpUtil;
import org.junit.Test;

public class HttpUtilTest {

    @Test
    public void test() {
        HttpUtil.get("http://octo.sankuai.com/api/zk/providerForOverload/aliveNode?env=0&appkey=com.sankuai.inf.logCollector&providerCountSwitch=1");
        HttpUtil.post("http://sg.sankuai.com/api/monitor/alive", "test");
        HttpUtil.post("http://octo.sankuai.com/api/log/report", "test");
    }
}
