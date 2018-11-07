package com.sankuai.meituan.config.test

import org.junit.Test

class PropertyTest {
    @Test
    void testPropertyNewline() {
        Properties properties = new Properties()
        def reader = new BufferedReader(new FileReader(PropertyTest.classLoader.getResource("test.properties").file))
        def line = reader.readLine()
        println(line)
//        properties.load(reader)
//        println(properties.entrySet())
    }
}
