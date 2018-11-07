package com.sankuai.inf.octo.mns.exception;

/**
 * Created by lhmily on 05/19/2016.
 */
public class MnsException extends Exception{
    public MnsException() {
        this("Mns Exception");
    }

    public MnsException(String msg) {
        super(msg);
    }
}
