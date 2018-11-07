package com.sankuai.inf.octo.mns;

@Deprecated
public class AgentException extends Exception {

    public AgentException() {
        this("Agent Exception");
    }

    public AgentException(String msg) {
        super(msg);
    }
}
