package com.sankuai.octo.doclet.doc;

public class OctoClassDoc {
    // @octo.appkey 服务标识，必填
    private String appkey;
    // @group 所属分组，可选，可在method中覆盖
    private String group;
    // @permission 权限，可选，可在method中覆盖
    private String permission;
    // @status 状态，可选，可在method中覆盖
    private String status;
    // @version 版本，可选，可在method中覆盖
    private String version;
    // @link 关联信息，如wiki等，可选，可在method中覆盖
    private String link;
    // @author 负责人，如邮箱、大象etc
    private String author;

    public OctoClassDoc() {
    }

    public OctoClassDoc(String appkey) {
        this.appkey = appkey;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("OctoClassDoc{");
        sb.append("appkey='").append(appkey).append('\'');
        sb.append(", group='").append(group).append('\'');
        sb.append(", permission='").append(permission).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", link='").append(link).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
