namespace java com.meituan.service.inf.kms.thrift.name

include 'common.thrift'

/*
    和Kms name Server进行通信，获取密钥信息
*/
service NameService {
    common.TNameStore getNameStore(1: string appKey, 2: string name, 3: common.TVMParam vmParam);
    map<string, common.TNameStore> getNameStores(1: string appKey, 2: set<string> names, 3: common.TVMParam vmParam);
}
