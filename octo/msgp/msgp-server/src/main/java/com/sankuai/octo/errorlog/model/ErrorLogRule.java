package com.sankuai.octo.errorlog.model;

import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-11-26
 */
public class ErrorLogRule {

    private List<String> messageRules;
    private List<String> exceptionRules;


    public List<String> getMessageRules() {
        return messageRules;
    }

    public void setMessageRules(List<String> messageRules) {
        this.messageRules = messageRules;
    }

    public List<String> getExceptionRules() {
        return exceptionRules;
    }

    public void setExceptionRules(List<String> exceptionRules) {
        this.exceptionRules = exceptionRules;
    }
}
