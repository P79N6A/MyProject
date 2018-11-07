package com.sankuai.octo.msgp.domain;

import java.util.List;

/**
 * Created by nero on 2018/7/17
 */
public class OncallDesc {
    private String appkey;
    private List<DescUser> data;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public List<DescUser> getData() {
        return data;
    }

    public void setData(List<DescUser> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "OncallDesc{" +
                "appkey='" + appkey + '\'' +
                ", data=" + data +
                '}';
    }

    public String toDescStr() {
        if (data.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (DescUser u : data) {
                sb.append(u.getName()).append("(").append(u.getLogin()).append(")").append(",");
            }
            return sb.substring(0, sb.length() - 1).toString();
        } else {
            return null;
        }
    }
}
