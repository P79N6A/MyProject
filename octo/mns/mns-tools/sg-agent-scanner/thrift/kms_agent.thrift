namespace java com.meituan.service.inf.kms.thrift.kms_agent

include "common.thrift"

/*
    1.OnNotifyUpdate接口 Kms agent充当Kms Notify的server
    2.GetKeyByName接口  Kms agent充当php_ext/jni的server
*/
service KmsAgent
{
    /**
     * 通知服务调用的接口，用来更新本机的缓存
     */
    i32 OnNotifyUpdate(1:list<common.TNameStore> key_list);

    /**
     * 根据密码的名称获取密码接口，取本机缓存
     */
    string GetKeyByName(1:string appKey, 2:string name);

    /**
    * 获取共享内存当中的所有密钥
    */
    list<common.TNameStore> getNameList();
}
