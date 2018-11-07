package com.sankuai.octo.plugins.enforcer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class SimpleArtifact implements Comparable<Artifact> {
    private String groupId;
    private String artifactId;
    private String version;
    private String action = "display"; // display, broken

    public SimpleArtifact() {
    }

    public SimpleArtifact(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public SimpleArtifact(String groupId, String artifactId, String version, String action) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.action = action;
    }

    public int compareTo(Artifact artifact) {
        if ((artifact != null) && (getGroupId().equals(artifact.getGroupId())) &&
                (getArtifactId().equals(artifact.getArtifactId()))) {
            ArtifactVersion ourVersion = new DefaultArtifactVersion(getVersion());
            ArtifactVersion theirVerion = new DefaultArtifactVersion(artifact.getVersion());
            return ourVersion.compareTo(theirVerion);
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleArtifact)) return false;

        SimpleArtifact that = (SimpleArtifact) o;

        if (!action.equals(that.action)) return false;
        if (!artifactId.equals(that.artifactId)) return false;
        if (!groupId.equals(that.groupId)) return false;
        if (!version.equals(that.version)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + action.hashCode();
        return result;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String toString() {
        return this.groupId + ":" + this.artifactId + ":" + this.version;
    }
}