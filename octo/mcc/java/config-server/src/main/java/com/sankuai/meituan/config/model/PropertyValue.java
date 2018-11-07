/*
 * Copyright (c) 2010-2015 meituan.com
 * All rights reserved.
 * 
 */
package com.sankuai.meituan.config.model;

import org.apache.commons.lang.StringUtils;

/**
 * @author liuxu<liuxu04@meituan.com>
 */
public class PropertyValue {
    protected String key;
    protected String value;
    protected String comment;

	public PropertyValue() {
	}

	public PropertyValue(String key, String value, String comment) {
		this.key = key;
		this.value = value;
		this.comment = comment;
	}

	public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyValue that = (PropertyValue) o;

        if (! key.equals(that.key)) return false;
        if (! value.equals(that.value)) return false;
        return isCommentEqual(that);

    }

    public boolean isCommentEqual(PropertyValue that) {
        return StringUtils.isEmpty(comment) ? StringUtils.isEmpty(that.comment) : comment.equals(that.comment);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + (StringUtils.isNotEmpty(comment) ? comment.hashCode() : 0);
        return result;
    }
}
