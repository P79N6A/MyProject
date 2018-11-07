namespace java com.sankuai.octo.config.model

//PR
struct PullRequest{
    1:i64 prID;
    2:string note;
    3:string prMisID;
    4:i32 status;//0：open，默认值; 1：代表已经merge，merge后不可更改 -1: decline
    5:string appkey;
    6:i32 env;
    7:i64 prTime;
}

// PR的修改详情
struct PRDetail{
    1:i64 prDetailID;
    2:i64 prID;
    3:required string key;
    4:string newValue;
    5:string oldValue;
    6:string newComment;
    7:string oldComment;
    8:bool isDeleted;
    9:optional string path; //该配置的所属分组
    10:optional string version; //当前配置的版本号
}

// Review
struct Review{
    1:i64 reviewID;
    2:i64 prID;
    3:string reviewerMisID;
    4:string note;
    5:i64 reviewTime;
    6:i32 approve;//2:merge 1:approve 0:默认 -1：decline
}

//文件下发的版本检查
struct UpdateGroupRequest{
    1:string appkey;
    2:string env;
    3:string groupId;
    4:optional string version;//当前版本
    5:list<string> ips;//下发主机
    6: optional string userName;
}

//保存文件操作日志
struct FilelogRequest{
    1:string appkey;
    2:string env;
    3:string groupId;
    4:string filename;
    5:string userName;
    6:string misId;
    7:list<string> successList;// 下发成功列表
    8:list<string> dErrList;// 下发失败列表
    9:list<string> eErrList;// 生效失败列表
    10: optional string type;// 操作类型
}

// 删除配置文件的请求
struct DeleteFileRequest{
   1 : required string appkey;
    2 : required string env;
    3 : required string groupID;
    4 : required string fileName;
    5 : optional string username;
    6 : optional string reserved;
}

//sgagent定期同步mcc的文件配置的请求
struct FileConfigSyncRequest{
    1:string appkey;
    2:optional string groupId;
    3:optional string path;
    4:string env;
    5:string ip;
}

//sgagent定期同步mcc的文件配置的返回
struct FileConfigSyncResponse{
    1:i32 code; //返回状态码
    2:string msg; //返回信息
}

//setConfig接口请求参数
struct SetConfigRequest {
    1: required string appkey;
    2: required string env;
    3: required string path; //分组
    4: required string conf; //配置
    5: optional string swimlane; //泳道标识
    6: optional string token; //传输的token
    7: optional string cell; //外卖set标识
    8: optional string ip; //sgagent的ip
}

//setConfig接口返回值
struct SetConfigResponse {
    1: required i32 code = 200; //状态码
    2: optional string errMsg; //状态信息
}

//增加文件配置分组的请求参数
struct AddGroupRequest{
    1:string appkey;
    2:string env;
    3:string groupName;
    4:list<string> ips;//主机列表
    5:optional string username;
}

//删除文件配置分组的请求参数
struct DeleteGroupRequest{
    1:string appkey;
    2:string env;
    3:string groupId;
    4:optional string username;
}

//merge pr的请求参数
struct MergeRequest{
    1:i64 prID;
    2:string username;
}

