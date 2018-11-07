package com.sankuai.meituan.config.test
import com.alibaba.fastjson.JSON
import com.sankuai.octo.config.model.ConfigNode
import org.apache.commons.lang.StringUtils
import org.junit.Test

import java.nio.file.*
import java.text.MessageFormat

class SimpleTest {
    @Test
    void testList() {
        println(JSON.toJSONString([new ConfigNode(appkey: "app1", env: "env1", path: null), new ConfigNode(appkey: "app2", env: "env2", path: "test")]))
    }

    @Test
    void testMessageFormatter() {
        println(MessageFormat.format("test:{0}", "test"))
    }

    @Test
    void testFileWatch() {
        def watchService= FileSystems.getDefault().newWatchService();
        Paths.get("/Users/Jason/Downloads/test").register(watchService,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        WatchKey key=watchService.take();
        for(WatchEvent<?> event:key.pollEvents())
        {
            System.out.println(event.context()+"发生了"+event.kind()+"事件");
        }
    }

    @Test
    void testSystem() {
        println(System.getProperty("os.name"))
    }

    @Test
    void testSplit() {
        println(StringUtils.split("test+", "+"))
    }

    @Test
    void testForeach() {
        def test = ["a": [111, 222]]
        for (Integer num : test["b"]) {
            println(num.toString())
        }
    }
}
