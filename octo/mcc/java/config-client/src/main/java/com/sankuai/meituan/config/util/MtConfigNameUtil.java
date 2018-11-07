package com.sankuai.meituan.config.util;

import com.google.common.collect.ImmutableSet;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.service.SgAgentService;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class MtConfigNameUtil {
	public static final ImmutableSet<String> ALL_ENV = SgAgentService.getAllEvn();

    public static void checkPath(String path) throws MtConfigException {
        if (StringUtils.isEmpty(path) || ! ("/".equals(path) || Pattern.matches("^/[a-zA-Z0-9/_-]*[a-zA-Z0-9_]$", path))) {
            throw new IllegalArgumentException("path不符合命名规则,名字只能包含字符:'a-zA-Z0-9/_-',且开头必须是'/',结尾不能包含'/'或'-'");
        }
    }

    public static void checkEnv(String env) throws MtConfigException {
        if (! (ALL_ENV.contains(env) || "".equals(env))) {
            throw new IllegalArgumentException("env不符合规范,env只能是其中一个:" + ALL_ENV);
        }
    }

    public static void checkAppkey(String appkey) throws MtConfigException {
        if (StringUtils.isEmpty(appkey) || ! Pattern.matches("[a-zA-Z0-9\\._-]*", appkey)) {
            throw new IllegalArgumentException("appkey不符合命名规则,名字只能包含字符:a-zA-Z0-9._-");
        }
    }

    public static void checkPathOfUser(String path) throws MtConfigException {
        if (StringUtils.isEmpty(path) || ! ("/".equals(path) || Pattern.matches("^[a-zA-Z0-9._-]*[a-zA-Z0-9_]$", path))) {
            throw new IllegalArgumentException("path不符合命名规则,名字只能包含字符:'a-zA-Z0-9_-',且分组层级用'.'隔断，不支持'/'");
        }
    }

	public static String getAgentInitFileName(String appkey, String env, String path) {
		StringBuilder agentInitFileName = new StringBuilder().append(wrap(appkey)).append(env);
		if (StringUtils.isNotEmpty(path)) {
			agentInitFileName.append(wrap(path));
		}
		return agentInitFileName.toString();
	}

	private static String wrap(String name) {
		return "[" + name + "]";
	}
}
