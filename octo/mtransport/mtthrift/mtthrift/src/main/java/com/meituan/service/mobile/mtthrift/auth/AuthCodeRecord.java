package com.meituan.service.mobile.mtthrift.auth;

/**
 */
public class AuthCodeRecord {
    /**
     VALID = 0;
     SIGNATURE_NOT_EQUAL = 1; // client server 两端签名不匹配
     EXPIRE_TIME_ERR = 2; // 签名时间过期
     KMS_ERR_CODE = 3; // kms 获取密钥问题
     JSON_ERR_CODE = 4; // 签名格式json解析问题
     SIGNATURE_PARSE_ERR = 5; // 签名解析错误，一般是签名不符合header.data.signature的格式
     BASE64_DECODE_ERR = 6; // base64 decode 问题
     NUM_FORMAT_ERR = 7; // 过期时间戳不能转为long

     // 访问控制使用
     AC_IP_INVALID = 11;
     AC_REMOTE_NAMESPACE_INVALID = 12;
     AC_LOCAL_NAMESPACE_INVALID = 13;
     */
    private int authCode = -1;

    public int getAuthCode() {
        return authCode;
    }

    public void setAuthCode(int authCode) {
        this.authCode = authCode;
    }
}
