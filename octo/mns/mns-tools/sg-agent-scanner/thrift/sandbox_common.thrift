namespace java com.sankuai.octo.sandbox.thrift.model

/*
 * sandbox定义
 */
struct SandboxConfig
{
    1:string appkey;
    2:list<string> ips;
}

struct Sandbox
{
    1:string id;
    2:string name;
    3:i32 env;
    4:i32 status;  //0：禁用，1：启用
    5:i32 access;  //0:封闭，除白名单外， 别的服务不可访问； 1：开放
    6:list<SandboxConfig> services;
    7:list<SandboxConfig> whiteList;
    8:i32 createTime;  //创建时间
    9:i32 updateTime;  //最近修改时间
    10:string reserved;   //扩展 k1:v1|k2:v2...
}

struct SandboxData
{
    1:Sandbox sandbox;
    2:string version;
}

struct SandboxResponse
{
    1:required i32 code = 200;
    2:optional list<SandboxData> sandboxList;
}