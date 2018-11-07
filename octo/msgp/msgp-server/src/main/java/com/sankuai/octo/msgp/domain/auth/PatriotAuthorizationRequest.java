package com.sankuai.octo.msgp.domain.auth;

public class PatriotAuthorizationRequest {
    private String namespace;
    private String env;
    private AuthorizationConfig authorization;

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

    public AuthorizationConfig getAuthorization() {
        return authorization;
    }

    public void setAuthorization(AuthorizationConfig authorization) {
        this.authorization = authorization;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PatriotAuthorizationRequest{");
        sb.append("namespace='").append(namespace).append('\'');
        sb.append(", env='").append(env).append('\'');
        sb.append(", authorization=").append(authorization);
        sb.append('}');
        return sb.toString();
    }
}

