package com.sankuai.octo.msgp.domain.auth;

import java.util.Set;

public class PatriotAuthenticationRequest {
    private String namespace;
    private String env;
    private Set<String> authentication;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Set<String> getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Set<String> authentication) {
        this.authentication = authentication;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PatriotAuthenticationRequest{");
        sb.append("namespace='").append(namespace).append('\'');
        sb.append(", env='").append(env).append('\'');
        sb.append(", authentication=").append(authentication);
        sb.append('}');
        return sb.toString();
    }
}

