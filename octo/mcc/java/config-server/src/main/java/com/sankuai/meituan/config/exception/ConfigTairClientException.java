package com.sankuai.meituan.config.exception;

public class ConfigTairClientException extends RuntimeException{
    public ConfigTairClientException() {
    }

    public ConfigTairClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigTairClientException(String message) {
        super(message);
    }

    public ConfigTairClientException(Throwable cause) {
        super(cause);
    }
}
