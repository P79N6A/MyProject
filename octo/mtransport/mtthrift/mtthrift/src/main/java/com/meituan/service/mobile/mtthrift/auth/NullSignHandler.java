package com.meituan.service.mobile.mtthrift.auth;

/**
 * an empty signHandler which does nothing
 */

public class NullSignHandler implements ISignHandler {
    @Override
    public SignMetaData sign(SignMetaData signMetaData) {
        return signMetaData;
    }
}
