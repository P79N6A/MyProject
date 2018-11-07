package com.sankuai.meituan.config.service

import com.sankuai.meituan.config.test.NewBaseTest
import org.junit.Test

import javax.annotation.Resource

class ZookeeperServiceTest extends NewBaseTest {
    @Resource
    ZookeeperService zookeeperService

    @Test
    void testDeleteOldSpaceConfig() {
        zookeeperService.getNodes("/config/mtconfig").each {zookeeperService.delRecurse("/config/mtconfig" + "/" + it)}
    }
}
