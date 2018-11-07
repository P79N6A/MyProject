package com.sankuai.meituan.config.test

import org.junit.Test

class TmpDirTest {
    @Test
    void testTmp() {
        def tmp = new File("/tmp/config_snapshot/mtconfig")
        println(tmp.canWrite())
        println(tmp.canRead())
    }
}
