package com.sankuai.meituan.config.test

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import com.sankuai.meituan.config.client.SGAgent
import com.sankuai.meituan.config.pojo.ConfigDataResponse
import org.junit.Test

class JNITest {
    @Test
    void testJNI() {
        def testList = ["/", "/test1", "/test1/aaa", "/test1/bbb", "/test2", "/test2/ccc", "/test2/ddd"]
        for (String testPath : testList) {
            def resultString = SGAgent.get("test.notify", "prod", testPath)
            println(resultString)

            try {
                ObjectMapper mapper = new ObjectMapper();
                def result = mapper.readValue(resultString, ConfigDataResponse.class)

                while (result.ret != 0 && result.ret != -201302) {
                    resultString = SGAgent.get("test.notify", "prod", testPath)
                    println(resultString)

                    result = mapper.readValue(resultString, ConfigDataResponse.class)
                    System.sleep(2000)
                }
                if (result.ret == -201302) {
                    println("$testPath is not ok")
                    System.exit(1)
                }
                println("$testPath is ok")

            } catch (JsonParseException e) {
                e.printStackTrace()
            } catch (JsonMappingException e) {
                e.printStackTrace()
            } catch (IOException e) {
                e.printStackTrace()
            }

        }
        println("all ok")
    }

    @Test
    void testCall() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            println(mapper.readTree(SGAgent.get("test.notify", "prod", "/test1/aaa")).toString())

        } catch (JsonParseException e) {
            e.printStackTrace()
        } catch (JsonMappingException e) {
            e.printStackTrace()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    @Test
    void testSet() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String configJson = mapper.writeValueAsString(Maps.newHashMap(["test":"aaa"]))
            println(SGAgent.setConfig("test.notify", "prod", "/test1/aaa", configJson))
        } catch (JsonProcessingException e) {
            e.printStackTrace()
        }
    }
}