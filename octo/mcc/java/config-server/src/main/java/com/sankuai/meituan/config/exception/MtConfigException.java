package com.sankuai.meituan.config.exception;

public class MtConfigException extends RuntimeException {
	private final int code;

	public MtConfigException(int code,String message) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
