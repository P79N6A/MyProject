package com.meituan.service.mobile.mtthrift.server.http.handler.check;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.meituan.service.mobile.mtthrift.auth.ISignHandler;
import com.meituan.service.mobile.mtthrift.client.cluster.DirectlyCluster;
import com.meituan.service.mobile.mtthrift.client.cluster.ICluster;
import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.sgagent.thrift.model.SGService;

import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/3
 */
public class ClientInfo {

    private String appkey;
    private String remoteAppkey;
    private Boolean remoteAppIsCell = false;
    private Integer remoteServerPort;
    private String ifaceName;
    private Boolean useDirect = false;
    private String env;
    private String swimlane;
    private String cell;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Server> providers;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SignInfo signInfo;
    @JsonIgnore
    private ISignHandler signHandler;
    @JsonIgnore
    private ICluster cluster;

    public ClientInfo(String appKey, String remoteAppkey, int remoteServerPort, String ifaceName, ICluster cluster, ISignHandler signHandler) {
        this.appkey = appKey;
        this.remoteAppkey = remoteAppkey;
        this.remoteServerPort = remoteServerPort;
        this.ifaceName = ifaceName;

        this.cluster = cluster;
        this.signHandler = signHandler;
        if (cluster instanceof DirectlyCluster) {
            this.useDirect = true;
        }
    }

    public ClientInfo(ClientInfo clientInfo) {
        this.appkey = clientInfo.appkey;
        this.remoteAppkey = clientInfo.remoteAppkey;
        this.remoteServerPort = clientInfo.remoteServerPort;
        this.ifaceName = clientInfo.ifaceName;

        this.env = ProcessInfoUtil.getHostEnv().name();
        this.swimlane = ProcessInfoUtil.getSwimlane();
        this.cell = ProcessInfoUtil.getCell();
    }

    public String getAppkey() {
        return appkey;
    }

    public String getRemoteAppkey() {
        return remoteAppkey;
    }

    public Integer getRemoteServerPort() {
        return remoteServerPort;
    }

    public String getIfaceName() {
        return ifaceName;
    }

    public Boolean isUseDirect() {
        return useDirect;
    }

    public void setUseDirect(Boolean useDirect) {
        this.useDirect = useDirect;
    }

    public List<Server> getProviders() {
        return providers;
    }

    public void setProviders(List<Server> providers) {
        this.providers = providers;
    }

    public void setSignInfo(SignInfo signInfo) {
        this.signInfo = signInfo;
    }

    public SignInfo getSignInfo() {
        return signInfo;
    }

    public String getEnv() {
        return env;
    }

    public String getSwimlane() {
        return swimlane;
    }

    public String getCell() {
        return cell;
    }

    public ISignHandler getSignHandler() {
        return signHandler;
    }

    public ICluster getCluster() {
        return cluster;
    }

    public Boolean isRemoteAppIsCell() {
        return remoteAppIsCell;
    }

    public void setRemoteAppIsCell(Boolean remoteAppIsCell) {
        this.remoteAppIsCell = remoteAppIsCell;
    }
}
