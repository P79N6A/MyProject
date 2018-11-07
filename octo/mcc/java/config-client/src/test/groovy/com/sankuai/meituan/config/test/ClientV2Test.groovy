package com.sankuai.meituan.config.test
import com.sankuai.meituan.config.MtConfigClient
import com.sankuai.meituan.config.annotation.MtConfig
import org.junit.BeforeClass
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClientV2Test {
    static final Logger logger = LoggerFactory.getLogger(ClientV2Test.class)

    public static final String ID = "test"
    public static final String KEY = "test"
    public static final String NOT_EXIST = "not_exist"

    @MtConfig(clientId = ClientV2Test.ID, key = ClientV2Test.KEY)
    static String test;

    @MtConfig(clientId = ClientV2Test.ID, key = ClientV2Test.NOT_EXIST)
    static String not_exist = NOT_EXIST;
    static MtConfigClient client

    @BeforeClass
    static void init() {
        client = new MtConfigClient()
        client.model = "v2"
        client.id = ID
        client.appkey = "test.notify"
        client.env = "prod"
        client.scanBasePackage = "com.sankuai.meituan.config.test"
        client.setGlobalConfigChangeListener({oldData, newData -> logger.info(",数据变更:[$oldData]->[$newData]")})
        client.init()
    }

    @Test
    void testListener() {
        int i = 0
        for (; i < 3; ++ i) {
            long startTime = System.currentTimeMillis()
            logger.info(client.setValue(KEY, "bbbbb$i").toString())
            client.addListener(KEY, { key, oldValue, newValue -> logger.info("耗时:[${System.currentTimeMillis() - startTime}],数据项变更 - [$key]:[$oldValue]->[$newValue]") })
            Thread.sleep(1000)
        }
    }

    @Test
    void testSet() {
        println(client.setValue(NOT_EXIST, "\"\"\"\\\\   \n\n\n\t\t\t\'\'\'\r\r\r\f\f\f\b\b\b   {{{}}}[[[]]]|||!!!@@@###\$\$\$%%%^^^&&&***((()))___+++:::<<<>>>???///...,,,''';;;"))
//        println(client.setValue(NOT_EXIST, "a\nb\tc\\\\d\"e"))
        Thread.sleep(5 * 1000)
        println(client.getValue(NOT_EXIST))
    }

    @Test
    void testAnno() {
        println(test)
        println(client.getValue(NOT_EXIST))
    }

    @Test
    void testGetValue() {
        println(client.getValue(KEY))
        println(client.getAllKeys())
        println(client.getAllKeyValues())
    }
}
