package com.sankuai.mtthrift.testSuite.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyAuthDataSource {
    public Map<String, String> getLocalTokenMap() {
        return Collections.singletonMap("com.sankuai.inf.mtthrift.testClient", "com.sankuai.inf.mtthrift.testClient");
    }
    public Map<String, String> getAppkeyTokenMap() {
        return Collections.singletonMap("com.sankuai.inf.mtthrift.testClient", "com.sankuai.inf.mtthrift.testClient");
    }

    public Set<String> getAppkeyWhitelist() {
        return Collections.emptySet();
    }

    public Map<String, Map<String, String>> getMethodAppkeyTokenMap() {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put("testString", Collections.singletonMap("com.sankuai.inf.mtthrift.testClient", "com.sankuai.inf.mtthrift.testClient"));
        return map;
    }
}
