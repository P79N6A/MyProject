package com.sankuai.meituan.config.test

import com.sankuai.meituan.config.util.RuntimeUtil
import org.junit.Test

class RuntimeUtilTest {
    @Test
    void testIsOnline() {
        println(RuntimeUtil.onlineIp)
    }
}
