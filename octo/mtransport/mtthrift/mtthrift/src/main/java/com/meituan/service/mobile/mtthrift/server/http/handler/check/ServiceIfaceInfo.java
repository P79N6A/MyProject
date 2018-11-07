package com.meituan.service.mobile.mtthrift.server.http.handler.check;

public class ServiceIfaceInfo {

    private String ifaceName;

    private String implName;

    public ServiceIfaceInfo( String ifaceName, String implName) {
        this.ifaceName = ifaceName;
        this.implName = implName;
    }

    public String getIfaceName() {
        return ifaceName;
    }

    public String getImplName() {
        return implName;
    }
}
