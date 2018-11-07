package com.sankuai.octo.scanner.model;

import com.alibaba.fastjson.JSONObject;
import com.sankuai.octo.scanner.Common;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by jiguang on 14-9-19.
 */
public class Provider {
    private String appkey;
    private String version;
    private String ip;
    private int port;
    private int weight;
    private int status;
    private int role;
    private int env;
    private long lastUpdateTime;
    private String extend;
    private int weightInExtend;
    private String checkUrl;
    private String id;
    private String name;
    private int enabled;
    private boolean slowConnecting = false;
    private long slowConnectingBeginTime;
    private Date startTime = new Date();
    private int slowStartSeconds;
    private String serverPath = "";
    private int unHealthyCount = 0;
    private String providersDir = "";
    private String exceptionMsg = "";

    public static Provider json2Provider(JSONObject json) {
        Provider provider = new Provider();
        provider.setAppkey(json.getString("appkey"));
        provider.setVersion(json.getString("version"));
        provider.setIp(json.getString("ip"));
        provider.setPort(Integer.parseInt(json.getString("port")));
        provider.setWeight(json.getIntValue("weight"));
        provider.setStatus(json.getIntValue("status"));
        provider.setRole(json.getIntValue("role"));
        provider.setEnv(json.getIntValue("env"));
        provider.setLastUpdateTime(json.getLong("lastUpdateTime"));
        provider.setCheckUrl(json.getString("checkUrl"));
        provider.setId(json.getString("id"));
        provider.setName(json.getString("name"));
        provider.setEnabled(json.getIntValue("enabled"));
        provider.setExtend(json.getString("extend"));
        if(!StringUtils.isBlank(provider.getExtend())) {
            String extend = provider.getExtend();
            // 修复之前可能出错的分隔符:
            extend = extend.replaceAll(Common.vbarAsRead, Common.vbar);
            provider.setExtend(extend);
            json.put("extend", extend);
            String[] items = provider.getExtend().split(Common.vbarAsRead);
            int length = items.length;
            if(length >= 2) {
                for (String item : items) {
                    if (item.startsWith("slowStartSeconds")) {
                        provider.parseSlowStartSeconds(item, provider);
                    } else if (item.startsWith("weight")) {
                        provider.parseWeightInExtend(item, provider);
                    }
                }

            }
        }
        return provider;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getEnv() {
        return env;
    }

    public void setEnv(int env) {
        this.env = env;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    @Override
    public String toString() {
        Field[] fields = this.getClass().getDeclaredFields();

        StringBuilder strBuf = new StringBuilder();
        strBuf.append(this.getClass().getName());
        strBuf.append("(");
        for (int i = 0; i < fields.length; i++) {
            Field fd = fields[i];
            strBuf.append(fd.getName()).append(Common.colon);
            try {
                strBuf.append(fd.get(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (i != fields.length - 1)
                strBuf.append(Common.vbar);
        }

        strBuf.append(")");
        return strBuf.toString();
    }


    public boolean equals2(Provider that) {
        if (this == that)
            return true;

        if (!appkey.equals(that.appkey))
            return false;
        if (!ip.equals(that.ip))
            return false;
        if (port != that.port)
            return false;

        return true;
    }

    public String getCheckUrl() {
        return checkUrl;
    }

    public void setCheckUrl(String checkUrl) {
        this.checkUrl = checkUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public boolean isSlowConnecting() {
        return slowConnecting;
    }

    public void setSlowConnecting(boolean isSlow) {
        this.slowConnecting = isSlow;
    }

    public int getSlowStartSeconds() {
        return slowStartSeconds;
    }

    public void setSlowStartSeconds(int slowStartSeconds) {
        this.slowStartSeconds = slowStartSeconds;
    }

    private void parseSlowStartSeconds(String strSlowStart, Provider provider){
        if(2 == strSlowStart.split(Common.colon).length ) {
            String strSlowStartValue = strSlowStart.split(Common.colon)[1].trim();
            provider.setSlowStartSeconds(Integer.parseInt(strSlowStartValue) );
        }

    }

    private void parseWeightInExtend(String strWeight, Provider provider){
        if(2 == strWeight.split(Common.colon).length ) {
            String strWeightValue = strWeight.split(Common.colon)[1].trim();
            provider.setWeightInExtend(Integer.parseInt(strWeightValue));
        }

    }

    public String getIdentifierString() {
        return  "env" + Common.colon + env + Common.vbar +
                "appkey" + Common.colon + appkey + Common.vbar +
                "ip" + Common.colon + ip +  Common.vbar +
                "port" + Common.colon + port;
    }

    public String getIpPort() {
        return  "ip" + Common.colon + ip +  Common.vbar +
                "port" + Common.colon + port;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public long getSlowConnectingBeginTime() {
        return slowConnectingBeginTime;
    }

    public void setSlowConnectingBeginTime(long slowConnectingBeginTime) {
        this.slowConnectingBeginTime = slowConnectingBeginTime;
    }

    public int getWeightInExtend() {
        return weightInExtend;
    }

    public void setWeightInExtend(int weightInExtend) {
        this.weightInExtend = weightInExtend;
        if(!this.extend.contains("weight" + Common.colon) ) {
            this.extend = this.extend + Common.vbar +
                    "weight" + Common.colon + weightInExtend;
        }
    }

    public int getUnHealthyCount() {
        return unHealthyCount;
    }

    public void setUnHealthyCount(int unHealthyCount) {
        this.unHealthyCount = unHealthyCount;
    }

    public void increaseUnHealthyCountByOne(){
        this.unHealthyCount ++;
    }

    public void resetUnHealthyCount(){
        this.unHealthyCount = 0;
    }

    public String getProvidersDir() {
        return providersDir;
    }

    public void setProvidersDir(String providersDir) {
        this.providersDir = providersDir;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public void setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
    }

}
