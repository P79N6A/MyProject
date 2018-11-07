package com.sankuai.meituan.config.test

import com.sankuai.meituan.config.util.Md5Util
import org.junit.Test

class Md5UtilTest {
    @Test
    void testMd5(){
        println(Md5Util.getMd5("test".bytes).length())
    }
}
