package com.sankuai.octo.msgp.domain.thrift;

/**
 * Mtthrift http invoke参数, 1.8.6版本里有，但避免msgp依赖低版本导致找不到该类单独创建
 * 稳定依赖1.8.6后, 删除此类直接使用Mtthrift的类
 */
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
