package com.sankuai.octo.oswatch.model;

/**
 * Created by chenxi on 8/12/15.
 */
public class OctoEnv {
    public static String getEnv(int value) {
        switch (value) {
            case 1: return "test";
            case 2: return "stage";
            case 3: return "prod";
            default: return "prod";
        }
    }
}
