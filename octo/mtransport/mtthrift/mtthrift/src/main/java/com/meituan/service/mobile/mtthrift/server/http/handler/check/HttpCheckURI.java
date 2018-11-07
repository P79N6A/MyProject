package com.meituan.service.mobile.mtthrift.server.http.handler.check;


import com.meituan.dorado.common.RpcRole;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/3/13
 */
public enum HttpCheckURI {
    // 接口调用
    SERVICE_REQUEST_PREFIX("/invoke", RpcRole.PROVIDER),

    // 服务端  基本信息
    SERVICE_BASE_INFO("/service.info", RpcRole.PROVIDER),
    // 服务端  服务方法信息
    SERVICE_METHOD_INFO("/method.info", RpcRole.PROVIDER),
    // 服务端 流量录制信息
    FLOWCOPY_INFO("/flowcopy.info", RpcRole.PROVIDER),
    // 调用端  服务提供者信息
    PROVIDER_INFO("/provider.info", RpcRole.INVOKER),
    // 服务端/调用端  鉴权信息
    AUTH_INFO("/auth.info", RpcRole.MULTIROLE),

    UNKNOW("", null);

    private String uri;
    private RpcRole role;

    HttpCheckURI(String uri, RpcRole role) {
        this.uri = uri;
        this.role = role;
    }

    public String uri() {
        return uri;
    }

    @Override
    public String toString() {
        return uri;
    }

    public static Set<String> getSupportUriOfRole(RpcRole role) {
        Set<String> uris = new HashSet<String>();
        for (HttpCheckURI uri : HttpCheckURI.values()) {
            if (RpcRole.MULTIROLE == role || RpcRole.MULTIROLE == uri.role || uri.role == role) {
                uris.add(uri.uri);
            }
        }
        return uris;
    }

    public static HttpCheckURI toHttpCheckURI(String path) {
        for (HttpCheckURI uri : HttpCheckURI.values()) {
            if (uri.uri.equals(path)) {
                return uri;
            }
        }
        return UNKNOW;
    }
}
