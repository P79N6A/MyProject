package com.sankuai.meituan.config;

import com.sankuai.meituan.config.annotation.MtConfig;

/**
 * test configBean
 * <p/>
 * Created by oulong on 14-5-20.
 */
public class TestConfigBean {
    private static final String NODENAME = MtConfigV1ClientTest.NODE_NAME;

    @MtConfig(clientId = NODENAME, key = "switch")
    public static boolean abc = false;

    @MtConfig(clientId = NODENAME, key = "switch-22")
    public static String str = "中文";

    @MtConfig(clientId = NODENAME, key = "kkk")
    public static String kkk;

    @MtConfig(clientId = MtConfigV1ClientTest.OTHER_NODE_NAME, key = "ggg")
    public static String ggg = "ggg";

	@MtConfig(clientId = "aaa", key = "key")
	public static String key;

	@MtConfig(clientId = "aaaa", key = "test")
	public static String test;

	@MtConfig(clientId = "mobile.prometheus.server", key = "threshold")
	public static int abc2 = 8000;

    @MtConfig(clientId = "aaaa", key = "mcckey")
    public static String mcckey;

    @MtConfig(nodeName = "mcctest.prod", key = "mcckey2")
    public static String mcckey2;
}
