package com.sankuai.meituan.config.model;


import com.sankuai.meituan.config.util.Common;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lhmily on 06/12/2016.
 */
public enum Env {
    test(1), stage(2), prod(3);
    public int id;

    Env(int i) {
        this.id = i;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public static Env get(int i) {
        if (i < 1 || i > 3) throw new IllegalArgumentException("Invalid env id = " + i);
        return 3 == i ? prod : (2 == i ? stage : test);
    }

    public static String correctShowEnv(String oldEnv) {
        String ret = oldEnv;
        if (Common.isOnline()) {
            if ("stage".equals(oldEnv)) {
                ret = "staging";
            }
        } else {
            if ("stage".equals(oldEnv)) {
                ret = "beta";
            } else if ("prod".equals(oldEnv)) {
                ret = "dev";
            }
        }
        return ret;
    }
    public static boolean isValid(String env){
        return StringUtils.equals("test", env) || StringUtils.equals("stage", env) || StringUtils.equals("prod", env);
    }
}
