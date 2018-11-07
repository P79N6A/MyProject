package com.sankuai.meituan.config.test
import com.sankuai.meituan.config.MtConfigClient
import com.sankuai.meituan.config.TestConfigBean
import com.sankuai.meituan.config.exception.SgAgentServiceException
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.hamcrest.CoreMatchers.*

class MtConfigClientTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    void testV1AndV2() {
        def v2Client = new MtConfigClient()
        v2Client.model = "v2"
        v2Client.id = ClientV2Test.ID
        v2Client.appkey = "test.notify"
        v2Client.env = "prod"
        v2Client.scanBasePackage = "com.sankuai.meituan.config.test"
        v2Client.setGlobalConfigChangeListener({oldData, newData -> logger.info(",数据变更:[$oldData]->[$newData]")})
        v2Client.init()
        def v1Client = new MtConfigClient()
        v1Client.setNodeName("mcctest.prod")
        v1Client.setId("aaaa")
        v1Client.setScanBasePackage("com.sankuai.meituan.config")
        v1Client.init()

        println(ClientV2Test.test)
        println(TestConfigBean.mcckey2)
        println(TestConfigBean.mcckey)
    }

    @Test
    void testV2NotExist() {
        thrown.expect(SgAgentServiceException)
        thrown.expect(hasProperty("errorCode",equalTo(-201502)))
        def v2Client = new MtConfigClient()
        v2Client.model = "v2"
        v2Client.id = "not_exist"
        v2Client.appkey = "not_exist"
        v2Client.env = "prod"
        v2Client.scanBasePackage = "com.sankuai.meituan.config.test"
        v2Client.init()
        println(v2Client.getAllKeys())
    }
}
