package com.sankuai.meituan.config.test
import com.sankuai.meituan.config.FileConfigClient
import com.sankuai.meituan.config.util.StreamUtil
import org.junit.BeforeClass
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FileConfigTest {
    static final Logger logger = LoggerFactory.getLogger(ClientV2Test.class)
    static FileConfigClient client

    @BeforeClass
    static void init() {
        def client = new FileConfigClient(appkey: "com.sankuai.cos.mtconfig",)
        client.addListener("test.txt", {String fileName, BufferedInputStream oriFile, BufferedInputStream newFile -> println("$oriFile:${StreamUtil.getLine(oriFile)}->${StreamUtil.getLine(newFile)}")})
        client.init()
        FileConfigTest.client = client
    }

    @Test
    void testGetFile() {
        println(StreamUtil.getLine(client.getFile("test.txt")))
    }

    @Test
    void testListener() {
        while (true) {
            Thread.sleep(500)
            println(StreamUtil.getLine(client.getFile("test.txt")))
        }
    }
}
