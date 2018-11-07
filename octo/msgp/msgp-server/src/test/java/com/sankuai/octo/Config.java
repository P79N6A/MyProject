package com.sankuai.octo;

import java.lang.reflect.Constructor;

/**
 * Created by wangyanzhao on 15/11/29.
 */
public class Config {
    public int maxIdle;
    public int minIdle;
    public int maxActive;

    public int getMaxIdle() {
        return this.maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return this.minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxActive() {
        return this.maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public static void main(String[] args) {
        try {
            Constructor constructor = Config.class.getConstructor();
            Object a = constructor.newInstance();
            Config.class.getMethod("setMaxIdle", int.class).invoke(a, 5);
            System.out.println(Config.class.getMethod("getMaxIdle").invoke(a));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
