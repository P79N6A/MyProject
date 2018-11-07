package com.sankuai.meituan.config.service

import com.sankuai.meituan.config.test.NewBaseTest
import org.junit.Test

import javax.annotation.Resource

class SpaceConfigServiceTest extends NewBaseTest {
    @Resource
    SpaceConfigService spaceConfigService

    @Test
    void testInit() {
        spaceConfigService.init("test.notify")
    }
}
