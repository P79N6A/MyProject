package com.sankuai.meituan.config.test

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.sankuai.octo.config.model.ConfigNode
import com.sankuai.octo.sgnotify.model.ConfigUpdateEvent
import com.sankuai.octo.sgnotify.service.SgNotify
import org.junit.Test

import javax.annotation.Resource

class SgNotifyTest extends NewBaseTest {
    @Resource
    SgNotify.Iface sgNotify

    @Test
    void testNotify() {
        def map = Maps.newHashMap()
        map.put("192.168.3.163", Sets.newHashSet(new ConfigNode("test.notify", "prod", ""), new ConfigNode("test.notify", "test", "")))
        println(sgNotify.notifyConfig(new ConfigUpdateEvent(map)))
    }
}
