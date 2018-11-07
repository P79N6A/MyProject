package com.sankuai.meituan.config.pojo;

import java.util.Map;

public class ConfigDataResponse {
    /**
     * 返回码,0为正常,如果错误,后3位一般为服务端返回的错误码
     */
    private Integer ret;
    private String msg;
    private Map<String, String> data;
    private Long version;

    public Integer getRet() {
        return ret;
    }

    public void setRet(Integer ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
