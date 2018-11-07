namespace java com.sankuai.octo.config.model

//ConfigFile.type和ConfigData.dataType的类型
const string JSON = "json";
const string PROPERTIES = "properties";
const string XML = "xml";

//节点的配置项,用于静态配置,预留
struct ConfigFile {
    1: required string filename;
    2: optional string filepath;
    3: optional string type;
    4: optional string privilege;
    5: optional i64 version;           //文件版本
    6: optional string md5;            //md5值，用来校验文件内容
    7: optional binary filecontent;    //文件内容
    8: optional i32 err_code;          //单个文件的错误码
    9: optional string reserved;       //保留字段
}

struct file_param_t{
    1: required string appkey;
    2: optional string env;
    3: optional string path;
    4: optional i32 cmd; // MtConfigCmdType消息类型
    5: optional i32 err;
    6: optional list<ConfigFile> configFiles;
    7: string groupId; // 配置分组
    8: string ip; // 请求来源节点IP（不一定是agent IP）
    9: string key; // 某次请求的唯一标示,用于匹配请求应答对
}

// 配置分组的结构体
struct ConfigGroup {
    1: required string appkey;
    2: required string env;
    3: required string id = "0";// 0:默认分组
    4: string name;
    5: i64 createTime;
    6: i64 updateTime;
    7: list<string> ips;
    8: i32 state; // 0:被删除 1:正常
    9: optional string version;//当前版本
}

// 配置分组list结构体
struct ConfigGroups {
    1: required list<ConfigGroup> groups;
}

//环境的名字
const string PROD = "prod";
const string STAGE = "stage";
const string TEST = "test";

//节点信息
struct ConfigNode {
    1: required string appkey;
    2: required string env;
    3: required string path;
    4: optional ConfigFile file;
    5: optional string swimlane;
    6: optional string cell;
}

struct ConfigData  {
    1: required string appkey;
    2: required string env;
    /**
     * 如果是""(空的字符串),表示没有path
     **/
    3: required string path;
    /**
     * 用version区分版本,避免updateTime的精确度问题
     **/
    4: required i64 version;
    5: required i64 updateTime;
    /**
     * 配置的序列化内容,内容的格式根据dataType定义
     * 如果没有更新则没有data
     **/
    6: required string data;
    /**
     * 配置数据的格式
     **/
    7: required string dataType;
}

struct ConfigDataResponse{
    /**
     * 异常错误码,具体含义参考 MtConfigExceptionType
     **/
    1 : required i32 code = 200;
    2 : optional ConfigData configData;
}

struct DefaultConfigResponse{
    /**
     * 异常错误码,具体含义参考 MtConfigExceptionType
     **/
    1 : required i32 code = 200;
    2 : optional map<string, string> defaultConfigs;
}

struct GetMergeDataRequest{
	1 : required string appkey;
    2 : required string env;
    3 : required string path;
    4 : required i64 version;
	5 : required string requestIp;
	6 : optional string swimlane; //增加泳道标识
	7 : optional string cell; //外卖set标识
}

struct ConfigFileRequest {
    1: required list<string> hosts;
    2: optional file_param_t files;
}

struct ConfigFileResponse {
    1: required i32 code = 200;
    2: optional list<string> hosts;
    3: optional map<string, i32> codes;//每个host一个错误码
}

struct ConfigGroupResponse {
    1: required i32 code = 200;
    2: ConfigGroup group
    3: string errMsg;
}

struct ConfigGroupsResponse {
    1: required i32 code = 200;
    2: ConfigGroups groups;
    3: string errMsg;
}


/**
* 异常错误定义
* 仅供参考用,新版本可能出现没有列出的错误code
**/
/**
 * 正常服务
 **/
const i32 SUCCESS = 200;
/**
 * 内容没更改
 **/
const i32 NO_CHANGE = 302;
/**
 * 未知服务异常
 **/
const i32 UNKNOW_ERROR = 500;
/**
 * 参数错误
 **/
const i32 PARAM_ERROR = 501;
/**
 * 对应的数据节点不存在
 **/
const i32 NODE_NOT_EXIST = 502;
/**
 * 要获取的配置版本不存在
 **/
const i32 NOT_EXIST_VERSION = 503;
/**
 * 版本过期,当更新时的version比服务器的旧时出现
 **/
const i32 DEPRECATED_VERSION = 504;
/**
 * 当前NODE已经被删除
 **/
const i32 NODE_DELETED = 505;

