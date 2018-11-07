package com.sankuai.meituan.config.test

import org.apache.commons.configuration.PropertiesConfiguration
import org.junit.Test

class CommonsConfigurationTest {
    @Test
    void test() {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setDelimiterParsingDisabled(true)
        propertiesConfiguration.setProperty("a", "a\\\\b,");
        propertiesConfiguration.setProperty("b", "a\\\\b");
        def stream = new ByteArrayOutputStream()
        propertiesConfiguration.save(stream)
        println(stream.toString())
    }
}
