package com.sankuai.octo.msgp.remote;

import com.sankuai.octo.msgp.domain.CapacityResult;
import com.sankuai.octo.msgp.service.ptest.PtestApiService;
import org.junit.Test;

public class PtestApiTest {
    @Test
    public void testAppkey() {
        CapacityResult result = PtestApiService.getCapacity("com.meituan.inf.rpc.benchmark");
        System.out.println(result.toString());
    }
}
