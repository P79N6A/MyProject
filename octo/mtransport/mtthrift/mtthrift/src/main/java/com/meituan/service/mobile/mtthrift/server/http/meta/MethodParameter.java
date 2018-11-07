package com.meituan.service.mobile.mtthrift.server.http.meta;

public class MethodParameter {
    private String typeStr;
    private String argStr;

    public MethodParameter() {}

    public MethodParameter(String typeStr, String argStr) {
        this.typeStr = typeStr;
        this.argStr = argStr;
    }

    public String getTypeStr() {
        return typeStr;
    }

    public void setTypeStr(String typeStr) {
        this.typeStr = typeStr;
    }

    public String getArgStr() {
        return argStr;
    }

    public void setArgStr(String argStr) {
        this.argStr = argStr;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("{typeStr=").append(typeStr).append(", argStr=").append(argStr).append("}");
        return str.toString();
    }
}
