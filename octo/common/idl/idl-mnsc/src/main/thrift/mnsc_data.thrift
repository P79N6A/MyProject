namespace java com.sankuai.octo.mnsc.idl.thrift.model

include  "../../../../idl-common/src/main/thrift/sgagent_common.thrift"
include  "../../../../idl-common/src/main/thrift/appkey.thrift"

typedef sgagent_common.SGService SGService
typedef appkey.AppkeyDesc AppkeyDesc
typedef appkey.AppkeyDescResponse AppkeyDescResponse

struct MNSResponse {
    /*
     * 状态码，200: 正常返回200；如果version不同，MNSCache正在更新时，先直接返回500; 400: 访问MNS请求超时
     */
    1 : required i32 code = 200; 

    /*
     * 节点列表，code = 200时，必须返回的字段
     */
    2 : optional list<SGService> defaultMNSCache;  

    /*
     * 版本信息
     */
    3 : optional string version; //正常返回时，附带上version信息
}

// for scanner
struct MNSBatchResponse {
    /*
     * 状态码，200: 正常返回200；如果version不同，MNSCache正在更新时，先直接返回500; 400: 访问MNS请求超时
     */
    1 : required i32 code = 200;

    /*
     * appkey->(env->detail)
     */
    2 : optional map<string, map<string, list<SGService>>> cache;
}

struct AppKeyListResponse {
    /*
     * 状态码，200: 正常返回；400: 访问MNS请求超时；
     */
    1 : required i32 code = 200; 

    /*
     * 服务appkey列表
     */
    2 : optional list<string> appKeyList;
}

typedef map<string,string> HttpProperties
struct HttpPropertiesResponse {
    /*
     * 状态码，200: 正常返回；400: 访问MNS请求超时；
     */
    1 : required i32 code = 200;

    /*
     * Http属性，key为appKey、value为HttpProperties
     */
    2 : optional map<string, HttpProperties> propertiesMap; 
}


//分组相关结构定义
struct groupNode {
    1 : string ip;
    2 : i32   port;
}
struct HttpGroup {
    1 : string          groupName;
    2 : string          appkey;
    3 : list<groupNode> server;//分组包含的后端节点
}
struct HttpGroupResponse {
    1 : required i32           code = 200;
    2 : map<string, HttpGroup> groups; //key是group_name，value是结构化数据
}
struct AllHttpGroupsResponse {
    1 : required i32           code = 200;
    2 : map<string, map<string, HttpGroup> > allGroups;//key是appkey，value是分组map
}


enum Protocols{
    THRIFT,
    HTTP
}

struct MnsRequest{
    1: Protocols protoctol;
    2: string appkey;
    3: string env;
}

//双框架注册状态查询响应结构
/*
 * 状态码，200: 正常返回；400: 参数错误; 404:请求的资源不存在; 500:服务端错误
 */
struct RegisterResponse{
    1: i32 code;            //返回码子
    2: optional bool allowRegister; //是否允许注册
    3: optional string msg;          //描述原因
}

//环境的名字
const string PROD = "prod";
const string STAGE = "stage";
const string TEST = "test";

/**
* 异常错误定义
**/
/**
* 正常服务
**/
const i32 SUCCESS = 200;
/**
* 如果version不同，MNSCache正在更新时，先直接返回500
**/
const i32 MNSCache_UPDATE = 500;
/**
* 超时异常
**/
const i32 TIMEOUT_ERROR = 400;

//参数错误
const i32 ILLEGAL_ARGUMENT = 400;

//请求的缓存不存在
const i32 NOT_FOUND = 404

//缓存没有变化
const i32 NOT_MODIFIED = 304

