package com.sankuai.meituan.config.util;

import java.io.Serializable;
import java.util.Map;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-22
 */
public class OObject implements Serializable {
    private static final long serialVersionUID = -8662652840325410970L;
    private String str;
    private Integer i;
    private Map<String, String> map;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        this.i = i;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OObject oObject = (OObject) o;

        if (i != null ? !i.equals(oObject.i) : oObject.i != null) return false;
        if (map != null ? !map.equals(oObject.map) : oObject.map != null) return false;
        if (str != null ? !str.equals(oObject.str) : oObject.str != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = str != null ? str.hashCode() : 0;
        result = 31 * result + (i != null ? i.hashCode() : 0);
        result = 31 * result + (map != null ? map.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OObject{" +
                "str='" + str + '\'' +
                ", i=" + i +
                ", map=" + map +
                '}';
    }
}