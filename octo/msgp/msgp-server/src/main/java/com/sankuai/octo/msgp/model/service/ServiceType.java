package com.sankuai.octo.msgp.model.service;

/**
 * Created by Chen.CD on 2018/7/31
 */

public enum ServiceType {
    RPC(0), HTTP(1), RPC_AND_HTTP(2), NO_PROVIDERS(-1);

    private int value;

    ServiceType(int value) {this.value = value;}

    public int getValue() {return this.value;}


}
