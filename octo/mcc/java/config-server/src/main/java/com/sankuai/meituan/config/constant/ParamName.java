package com.sankuai.meituan.config.constant;

import com.google.common.collect.ImmutableSet;

public class ParamName {
	public static final String CLIENT_ID = "_client_id_";
	public static final String ENV_PROD = "prod", ENV_STAGE = "stage", ENV_TEST = "test";
	public static final ImmutableSet<String> ALL_ENV = ImmutableSet.of(ENV_PROD, ENV_STAGE, ENV_TEST);
	public static final String CONFIG_BASE_PATH = "/config";
    public static final String NOTIFY_BASE_PATH= "/config_notify";
    public static final String SETTING_SPACE = "/config_settings";
    public static final String DEFAULT_SPACE = "mtconfig";
}
