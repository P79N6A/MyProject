package com.sankuai.octo.doclet.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OctoTypeDoc {
    private String type;
    private String simpleType;
    private List<OctoTypeDoc> paramTypes = new ArrayList<OctoTypeDoc>();
    private String commentText;
    private Map<String, OctoTypeDoc> fields = new HashMap<String, OctoTypeDoc>();

    public OctoTypeDoc() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSimpleType() {
        return simpleType;
    }

    public void setSimpleType(String simpleType) {
        this.simpleType = simpleType;
    }


    public List<OctoTypeDoc> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(List<OctoTypeDoc> paramTypes) {
        this.paramTypes = paramTypes;
    }

    public void putParamType(OctoTypeDoc typeDoc) {
        paramTypes.add(typeDoc);
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Map<String, OctoTypeDoc> getFields() {
        return fields;
    }

    public void setFields(Map<String, OctoTypeDoc> fields) {
        this.fields = fields;
    }

    public void putField(String name, OctoTypeDoc typeDoc) {
        fields.put(name, typeDoc);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("OctoTypeDoc{");
        sb.append("type='").append(type).append('\'');
        sb.append(", simpleType='").append(simpleType).append('\'');
        sb.append(", paramTypes=").append(paramTypes).append('\'');
        sb.append(", commentText='").append(commentText).append('\'');
        sb.append(", fields=").append(fields);
        sb.append('}');
        return sb.toString();
    }
}
