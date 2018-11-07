package com.sankuai.octo.msgp.model.service;

/**
 * Created by Chen.CD on 2018/7/31
 */
public enum ThriftType {
    THRIFT(0), PIGEON(1), OTHER(2), NO_PROVIDERS(-1);

    private int value;

    ThriftType(int value) {this.value = value;}

    public int getValue() {return this.value;}
}
