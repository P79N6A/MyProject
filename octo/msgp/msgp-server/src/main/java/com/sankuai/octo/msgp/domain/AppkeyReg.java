package com.sankuai.octo.msgp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * Created by zava on 16/9/21.
 * 用于注册服务
 */
public class AppkeyReg implements java.io.Serializable{

    private static final long serialVersionUID = 2538483864714583127L;

    private String username;
    private SimpleDesc data;

    public AppkeyReg(){

    }
    public AppkeyReg(String username,String appkey,List<String> owners,List<String> observers,
                     int base,String owt,String pdl,String tags,String intro){
        this.username = username;
        this.data = new SimpleDesc(appkey,owners,observers,base,owt,pdl,tags,intro);
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SimpleDesc getData() {
        return data;
    }

    public void setData(SimpleDesc data) {
        this.data = data;
    }

    public String toString(){
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
