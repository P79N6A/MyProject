package com.sankuai.meituan.config.service;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-4-29
 */
public class MtHttpRequest {
    private String host;
    private String path;
    private String method;
    private Map<String, Object> headers = new HashMap<String, Object>();
    private Map<String, Object> params = new HashMap<String, Object>();
    private Map<String, Object> entitys = new HashMap<String, Object>();

    public MtHttpRequest() {

    }

    public MtHttpRequest(Builder builder) {
        this.host = builder.host;
        this.path = builder.path;
        this.method = builder.method;
        if (builder.headers == null) {
            builder.headers = new HashMap<String, Object>();
        }
        this.headers = builder.headers;
        if (builder.params == null) {
            builder.params = new HashMap<String, Object>();
        }
        this.params = builder.params;
        if (builder.entitys == null) {
            builder.entitys = new HashMap<String, Object>();
        }
        this.entitys = builder.entitys;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String host;
        private String path;
        private String method;
        private Map<String, Object> headers = new HashMap<String, Object>();
        private Map<String, Object> params = new HashMap<String, Object>();
        private Map<String, Object> entitys = new HashMap<String, Object>();
        public Builder() {

        }

        public MtHttpRequest build() {
            return new MtHttpRequest(this);
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder headers(Map<String, Object> values) {
            if (headers == null) {
                if (values == null) {
                    headers = new HashMap<String, Object>();
                } else {
                    headers = new HashMap<String, Object>(values);
                }
            } else if (values != null) {
                headers.putAll(values);
            }
            return this;
        }

        public Builder header(String key, Object value) {
            if (value != null) {
                if (headers == null) {
                    headers = new HashMap<String, Object>();
                }
                this.headers.put(key, value);
            }
            return this;
        }

        public Builder params(Map<String, Object> parameters) {
            if (params == null) {
                if (parameters == null) {
                    params = new HashMap<String, Object>();
                } else {
                    params = new HashMap<String, Object>(parameters);
                }
            } else if (parameters != null) {
                params.putAll(parameters);
            }
            return this;
        }

        public Builder param(String key, Object value) {
            if (params == null) {
                params = new HashMap<String, Object>();
            }
            this.params.put(key, value);
            return this;
        }

        public Builder jsonEntity(String json) {
            if (entitys == null) {
                entitys = new HashMap<String, Object>();
            }
            entitys.put("json", json);
            return this;
        }
        public Builder entity(String key, Object value) {
            if (entitys == null) {
                entitys = new HashMap<String, Object>();
            }
            this.entitys.put(key, value);
            return this;
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, Object> getEntitys() {
        return entitys;
    }

    public void setEntitys(Map<String, Object> entitys) {
        this.entitys = entitys;
    }

    public boolean needAuth() {
        return StringUtils.isNotBlank(getClientKey()) && StringUtils.isNotBlank(getSecret());
    }

    public String getSecret() {
        return (String) headers.get("secret");
    }

    public void setSecret(String secret) {
        headers.put("secret", secret);
    }

    public String getClientKey() {
        return (String) headers.get("key");
    }

    public void setClientKey(String key) {
        headers.put("key", key);
    }

    public Integer getTimeout() {
        return (Integer) headers.get("timeout");
    }

    public Integer getRetry() {
        return (Integer) headers.get("retry");
    }
}
