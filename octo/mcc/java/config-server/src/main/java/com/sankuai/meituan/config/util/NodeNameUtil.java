package com.sankuai.meituan.config.util;

import com.sankuai.meituan.config.constant.ParamName;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.octo.config.model.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.regex.Pattern;

public class NodeNameUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeNameUtil.class);

    public static boolean checkSpacePath(String spacePath) {
        for (String node : spacePath.split("/")) {
            if (! validateAppkey(node)) {
                return false;
            }
        }
        return true;
    }

    public static void checkPath(String path) throws MtConfigException {
        if (StringUtils.isEmpty(path) || ! ("/".equals(path) || Pattern.matches("^/[a-zA-Z0-9/_-]*[a-zA-Z0-9_]$", path))) {
            throw new MtConfigException(Constants.PARAM_ERROR, "path不符合命名规则,名字只能包含字符:'a-zA-Z0-9/_-',且开头必须是'/',结尾不能包含'/'或'-'");
        }
    }

    public static void checkEnv(String env) throws MtConfigException {
        if (! ParamName.ALL_ENV.contains(env)) {
            throw new MtConfigException(Constants.PARAM_ERROR, "env不符合规范,env只能是其中一个:" + ParamName.ALL_ENV);
        }
    }

    public static void checkAppkey(String appkey) throws MtConfigException {
        if (StringUtils.isEmpty(appkey) || ! validateAppkey(appkey)) {
            throw new MtConfigException(Constants.PARAM_ERROR, "appkey不符合命名规则,名字只能包含字符:a-zA-Z0-9._-");
        }
    }

    public static void checkKey(String key) {
        if (! Pattern.matches("[a-zA-Z0-9_.-]*", key)) {
            throw new MtConfigException(Constants.PARAM_ERROR, MessageFormatter.format("key[{}]不符合命名规则,名字只能包含字符:a-zA-Z0-9_.-", key).getMessage());
        }
    }

    public static boolean isPathExist(String path) {
        return StringUtils.isNotEmpty(path) && (! "/".equals(path));
    }

    public static String getSpacePath(String appkey, String env, String path) {
        return ZKPathBuilder.newBuilder().appendSpace(appkey).appendSpace(env).appendSpace(path).toPath();
    }

    public static String getSpacePath(String spaceName, String nodeName) {
        ZKPathBuilder zkPathBuilder = ZKPathBuilder.newBuilder();
        if (nodeName.startsWith(spaceName)) {
            if (spaceName.equals(nodeName)) {
                return zkPathBuilder.appendSpace(spaceName).toPath();
            } else {
                String childNodePath = StringUtils.substring(nodeName, spaceName.length() + 1);
                return zkPathBuilder.appendSpace(spaceName).appendNode(childNodePath).toPath();
            }
        } else {
            LOGGER.warn("nodeName:{} does not start with spaceName: {}", nodeName, spaceName);
            return zkPathBuilder.appendNode(nodeName).toPath();
        }
    }

    public static String getAppkey(String spacePath) {
        String[] nodes = StringUtils.split(spacePath, "/");
        return nodes[0];
    }

    public static boolean validateAppkey(String appkey) {
        return Pattern.matches("[a-zA-Z0-9\\._-]*", appkey);
    }

    public static String getMCCSettingSpacePath(){
        return getSpacePath("/com.sankuai.cos.mtconfig", "/com.sankuai.cos.mtconfig/prod");
    }
}
