namespace java com.sankuai.octo.mnsc.idl.thrift.model

include  "./sgagent_common.thrift"
 
typedef sgagent_common.SGService SGService

struct MNSResponse {
    1 : required i32 code = 200; 
    2 : optional list<SGService> defaultMNSCache;  
    3 : optional string version;
}

struct AppKeyListResponse {
    1 : required i32 code = 200;
    2 : optional list<string> appKeyList;
}

typedef map<string,string> HttpProperties
struct HttpPropertiesResponse {
    1 : required i32 code = 200;
    2 : optional map<string, HttpProperties> propertiesMap; 
}

struct UpstreamResponse {
    1 : required i32 code = 200; 
    2 : map<string, string> upstreams; 
    3 : string ext;
}

const string PROD = "prod";
const string STAGE = "stage";
const string TEST = "test";

const i32 SUCCESS = 200;
const i32 MNSCache_UPDATE = 500;
const i32 TIMEOUT_ERROR = 400;
