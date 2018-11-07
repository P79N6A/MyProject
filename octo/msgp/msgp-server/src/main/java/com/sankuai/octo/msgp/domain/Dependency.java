package com.sankuai.octo.msgp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by yves on 16/9/5.
 */
public class Dependency {

    @JsonProperty( "groupId" )
    private String groupId;

    @JsonProperty( "artifactId" )
    private String artifactId;

    @JsonProperty( "version" )
    private String version;

    public Dependency(){}

    public Dependency(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return  new HashCodeBuilder(17, 37).
                append(groupId).
                append(artifactId).
                append(version).
                toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;
        Dependency e = (Dependency) obj;
        return new EqualsBuilder().
                append(getGroupId(), e.getGroupId()).
                append(getArtifactId(), e.getArtifactId()).
                append(getVersion(), e.getVersion()).
                isEquals();
    }

    @Override
    public String toString() {
        String version = this.version.isEmpty()? "" : ", " + this.version;
        return this.groupId + ", " + this.artifactId + version;
    }
}
