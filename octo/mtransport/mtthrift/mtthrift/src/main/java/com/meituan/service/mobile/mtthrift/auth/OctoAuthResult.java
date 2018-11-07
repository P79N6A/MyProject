package com.meituan.service.mobile.mtthrift.auth;

public enum OctoAuthResult {
    WHITELIST_PASS,
    UNI_AUTH_PASS,
    OLD_AUTH_PASS,
    NOT_AUTH_METHOD_PASS,
    SIGN_NOT_EQUAL_DENY,
    TOKEN_EMPTY_DENY,
    APPKEY_EMPTY_DENY,
    TOKEN_MAP_EMPTY_DENY,
    OTHER
}
