package com.sankuai.octo.msgp.domain;


import java.util.List;

public class HostInfo {
    private static final String defaultMsg = "--";
    private String ip = defaultMsg;
    private String hostName = defaultMsg;
    private String env = defaultMsg;
    private String idc = defaultMsg;
    private String osVersion = defaultMsg;
    private String osStartTime = defaultMsg;
    private String fileRes = defaultMsg;
    private String sgagentInstalled = defaultMsg;
    private String puppetRes = defaultMsg;
    private String sgagentLog = defaultMsg;
    private String rpcRes = defaultMsg;
    private String cpluginRunningRes = defaultMsg;
    private String sgagentRunningRes = defaultMsg;
    private String monitorRes = defaultMsg;
    private String sysResourceRes = defaultMsg;
    private String ipVlanRes = defaultMsg;
    private String errlog = defaultMsg;
    private String result = defaultMsg;
    private List<String> errList;

    public HostInfo(String errlog, String result, List<String> errList) {
        this.errlog = errlog;
        this.result = result;
        this.errList = errList;
    }

    public HostInfo(String ip, String hostName, String env, String idc, String errlog, String result, List<String> errList) {
        this(errlog, result, errList);
        this.ip = ip;
        this.hostName = hostName;
        this.env = env;
        this.idc = idc;
    }

    public HostInfo(String ip, String hostName, String env, String idc, String osVersion, String osStartTime, String fileRes,
                    String sgagentInstalled, String puppetRes, String sgagentLog, String rpcRes, String cpluginRunningRes,
                    String sgagentRunningRes, String monitorRes, String sysResourceRes, String ipVlanRes,
                    String errlog, String result, List<String> errList) {
        this(ip, hostName, env, idc, errlog, result, errList);
        this.osVersion = osVersion;
        this.osStartTime = osStartTime;
        this.fileRes = fileRes;
        this.sgagentInstalled = sgagentInstalled;
        this.puppetRes = puppetRes;
        this.sgagentLog = sgagentLog;
        this.rpcRes = rpcRes;
        this.cpluginRunningRes = cpluginRunningRes;
        this.sgagentRunningRes = sgagentRunningRes;
        this.monitorRes = monitorRes;
        this.sysResourceRes = sysResourceRes;
        this.ipVlanRes = ipVlanRes;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsStartTime() {
        return osStartTime;
    }

    public void setOsStartTime(String osStartTime) {
        this.osStartTime = osStartTime;
    }

    public String getFileRes() {
        return fileRes;
    }

    public void setFileRes(String fileRes) {
        this.fileRes = fileRes;
    }

    public String getSgagentInstalled() {
        return sgagentInstalled;
    }

    public void setSgagentInstalled(String sgagentInstalled) {
        this.sgagentInstalled = sgagentInstalled;
    }

    public String getPuppetRes() {
        return puppetRes;
    }

    public void setPuppetRes(String puppetRes) {
        this.puppetRes = puppetRes;
    }

    public String getSgagentLog() {
        return sgagentLog;
    }

    public void setSgagentLog(String sgagentLog) {
        this.sgagentLog = sgagentLog;
    }

    public String getRpcRes() {
        return rpcRes;
    }

    public void setRpcRes(String rpcRes) {
        this.rpcRes = rpcRes;
    }

    public String getCpluginRunningRes() {
        return cpluginRunningRes;
    }

    public void setCpluginRunningRes(String cpluginRunningRes) {
        this.cpluginRunningRes = cpluginRunningRes;
    }

    public String getSgagentRunningRes() {
        return sgagentRunningRes;
    }

    public void setSgagentRunningRes(String sgagentRunningRes) {
        this.sgagentRunningRes = sgagentRunningRes;
    }

    public String getMonitorRes() {
        return monitorRes;
    }

    public void setMonitorRes(String monitorRes) {
        this.monitorRes = monitorRes;
    }

    public String getSysResourceRes() {
        return sysResourceRes;
    }

    public void setSysResourceRes(String sysResourceRes) {
        this.sysResourceRes = sysResourceRes;
    }

    public String getIpVlanRes() {
        return ipVlanRes;
    }

    public void setIpVlanRes(String ipVlanRes) {
        this.ipVlanRes = ipVlanRes;
    }

    public String getErrlog() {
        return errlog;
    }

    public void setErrlog(String errlog) {
        this.errlog = errlog;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<String> getErrList() {
        return errList;
    }

    public void setErrList(List<String> errList) {
        this.errList = errList;
    }
}
