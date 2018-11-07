package com.sankuai.meituan.config.service

import org.apache.commons.io.IOUtils
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ConfigTairClientTest {
    static final Logger LOGGER = LoggerFactory.getLogger(ConfigTairClient.class)
    def configTairClient = new ConfigTairClient()

    @Test
    void testAddFile() {
        configTairClient.addFile("prod", "com.sankuai.cos.mtconfig", "log4j2.xml", "/opt/meituan/apps/mcc/com.sankuai.cos.mtconfig/", IOUtils.toByteArray(ConfigTairClientTest.class.getResource("/log4j2.xml")))
    }

    @Test
    void testDelFile() {
        configTairClient.delFile("prod", "com.sankuai.cos.mtconfig", null, "log4j2.xml")
    }

    @Test
    void testGetFilenameList() {
        println(configTairClient.getFilenameList ("prod", "com.sankuai.cos.mtconfig") .toString())
        println(configTairClient.getFilenameList ("prod", "com.sankuai.cos.mtconfig.http") .toString())
    }

    @Test
    void testGetFileVersionList() {
        println(configTairClient.getFileVersionList("prod", "com.sankuai.cos.mtconfig", null, "test.txt") .toString())
        println(configTairClient.getFileVersionList("prod", "com.sankuai.cos.mtconfig.http", null, "test.txt") .toString())
    }

    @Test
    void testGetCurrentFile() {
        println(configTairClient.getCurrentFile("prod", "com.sankuai.cos.mtconfig", "test.txt").toString())
        println(configTairClient.getCurrentFile("prod", "com.sankuai.cos.mtconfig.http", "test.txt").toString())
    }
}
