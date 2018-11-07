package com.sankuai.octo.demo;

import org.apache.thrift.TApplicationException;

/**
 * 消息发送异常
 */
public class MessageException extends TApplicationException {
    /**
     * 失败信息
     */
    private String message;
    /**
     * 详细堆栈
     */
    private String cause;
}
