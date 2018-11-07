package com.meituan.service.mobile.mtthrift.auth;



public interface IAuthHandler {

    /**
     * 对请求进行认证
     * @param authMetaData 认证的元数据
     * @return true：认证通过 false：认证失败
     */
    boolean auth(AuthMetaData authMetaData);

    /**
     * 获取鉴权类型，比如是连接粒度鉴权还是请求粒度鉴权
     * @return
     */
    AuthType getAuthType();

}
