package com.meituan.mtrace.hbase;

/**
 * @author zhangzhitong
 * @created 11/3/15
 */
public enum Env {
    Prod("prod"),
    Stage("stage"),
    Test("test");
    private String value;
    private Env(String value) {
        this.value = value;
    }
    public String getValue() {
        return this.value;
    }
    public static Env strToEnv(String envStr) {
        if (envStr == null || envStr.equalsIgnoreCase("prod")) {
            return Env.Prod;
        } else if (envStr.equalsIgnoreCase("stage")) {
            return Env.Stage;
        } else if (envStr.equalsIgnoreCase("test")) {
            return Env.Test;
        } else {
            return Env.Prod;
        }
    }
}
