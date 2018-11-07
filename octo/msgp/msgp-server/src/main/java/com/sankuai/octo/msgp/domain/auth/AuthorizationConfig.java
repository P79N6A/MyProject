package com.sankuai.octo.msgp.domain.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class AuthorizationConfig {
    private AccessConfig globalAccessControl;
    private Map<String, AccessConfig> remoteAppAccessControl = new HashMap<>();
    private Map<String, AccessConfig> remoteIpAccessControl = new HashMap<>();

    public AuthorizationConfig() {
        this.remoteAppAccessControl = new HashMap<>();
        this.remoteIpAccessControl = new HashMap<>();
    }

    @JsonProperty("local-namespace")
    public AccessConfig getGlobalAccessControl() {
        return globalAccessControl;
    }

    @JsonProperty("local-namespace")
    public void setGlobalAccessControl(AccessConfig globalAccessControl) {
        this.globalAccessControl = globalAccessControl;
    }

    @JsonProperty("remote-namespace")
    public Map<String, AccessConfig> getRemoteAppAccessControl() {
        return remoteAppAccessControl;
    }

    @JsonProperty("remote-namespace")
    public void setRemoteAppAccessControl(Map<String, AccessConfig> remoteAppAccessControl) {
        this.remoteAppAccessControl = remoteAppAccessControl;
    }

    @JsonProperty("remote-ip")
    public Map<String, AccessConfig> getRemoteIpAccessControl() {
        return remoteIpAccessControl;
    }

    @JsonProperty("remote-ip")
    public void setRemoteIpAccessControl(Map<String, AccessConfig> remoteIpAccessControl) {
        this.remoteIpAccessControl = remoteIpAccessControl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AuthorizationConfig{");
        sb.append("globalAccessControl=").append(globalAccessControl);
        sb.append(", remoteAppAccessControl=").append(remoteAppAccessControl);
        sb.append(", remoteIpAccessControl=").append(remoteIpAccessControl);
        sb.append('}');
        return sb.toString();
    }
}

