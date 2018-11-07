package com.meituan.control.zookeeper.service;

/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */


class ResourceNotExistException extends Exception {
	private static final long serialVersionUID = 1L;
	public ResourceNotExistException(String message) {
		super(message);
	}
	public ResourceNotExistException(String message, Throwable cause) {
		super(message, cause);
	}
}