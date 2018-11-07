package com.meituan.service.mobile.mtthrift.auth;


public interface ISignHandler {

    /**
     * 对请求进行签名
     * @param signMetaData 签名的元数据
     * @return 返回签名数据
     */
    SignMetaData sign(SignMetaData signMetaData);
}
