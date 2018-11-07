package com.sankuai.meituan.config.model;

import java.util.Arrays;

public class ConfigCaseClass {
    private String version;
    private String path;
    private byte[] fileContents;

    public ConfigCaseClass(String version, String path, byte[] fileContents) {
        this.version = version;
        this.path = path;
        this.fileContents = fileContents;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getFileContents() {
        return fileContents;
    }

    public void setFileContents(byte[] fileContents) {
        this.fileContents = fileContents;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigCaseClass{");
        sb.append("version='").append(version).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", fileContents=").append(Arrays.toString(fileContents));
        sb.append('}');
        return sb.toString();
    }
}
