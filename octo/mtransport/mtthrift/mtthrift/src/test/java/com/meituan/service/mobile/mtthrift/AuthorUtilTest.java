package com.meituan.service.mobile.mtthrift;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/1/12
 * Time: 23:10
 */
public class AuthorUtilTest {
    private static Logger logger = LoggerFactory.getLogger(AuthorUtilTest.class);
    private static final String json = "{ \"user\" : \"杨杰(yangjie17)\", \"updateTime\" : 1445308751, \"status\" : 0, \"ips\" : [ \"10.4.243.208\", \"192.168.12.164\", \"10.4.242.103\" ]}";

    @Test public void test() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> authMap = objectMapper.readValue(json, Map.class);
        assert (Integer.valueOf(1445308751).equals(authMap.get("updateTime")));
        assert (Integer.valueOf(0).equals(authMap.get("status")));
        assert (authMap.get("ips") instanceof List);
        List ipList = ((List) authMap.get("ips"));
        assert (ipList.contains("10.4.243.208"));
        assert (ipList.contains("192.168.12.164"));
        assert (ipList.contains("10.4.242.103"));
    }
}
