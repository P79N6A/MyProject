package com.sankuai.octo.msgp.domain.auth;

public class PatriotAuthorizationResponseData {
    private String namespace;
    private AuthorizationConfig authorization;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public AuthorizationConfig getAuthorization() {
        return authorization;
    }

    public void setAuthorization(AuthorizationConfig authorization) {
        this.authorization = authorization;
    }


}
