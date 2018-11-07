package com.sankuai.octo.doclet.doc;

import java.util.Map;

public class OctoMethodDoc extends OctoClassDoc {
    // @api 接口标识，可选
    private String api;
    // @name 接口名字，可选
    private String name;
    // @desc 接口描述，可选
    private String desc;
    // @param 参数信息，多个分开写，可选，格式：参数名 说明
    private Map<String, OctoTypeDoc> params;
    // @return 返回结果，可选
    private OctoTypeDoc result;
    // @throws 异常信息，多个分开写，可选，格式：异常类型 说明
    private Map<String, OctoTypeDoc> exceptions;

    public OctoMethodDoc() {
    }

    public OctoMethodDoc(OctoClassDoc octoClassDoc, String api) {
        super(octoClassDoc.getAppkey());
        this.setGroup(octoClassDoc.getGroup());
        this.setPermission(octoClassDoc.getPermission());
        this.setStatus(octoClassDoc.getStatus());
        this.setVersion(octoClassDoc.getVersion());
        this.setLink(octoClassDoc.getLink());
        this.setApi(api);
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Map<String, OctoTypeDoc> getParams() {
        return params;
    }

    public void setParams(Map<String, OctoTypeDoc> params) {
        this.params = params;
    }

    public OctoTypeDoc getResult() {
        return result;
    }

    public void setResult(OctoTypeDoc result) {
        this.result = result;
    }

    public Map<String, OctoTypeDoc> getExceptions() {
        return exceptions;
    }

    public void setExceptions(Map<String, OctoTypeDoc> exceptions) {
        this.exceptions = exceptions;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("OctoMethodDoc{");
        sb.append("api='").append(api).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", desc='").append(desc).append('\'');
        sb.append(", params='").append(params).append('\'');
        sb.append(", result='").append(result).append('\'');
        sb.append(", exceptions='").append(exceptions).append('\'');
        sb.append("} ");
        sb.append(super.toString());
        return sb.toString();
    }
}
