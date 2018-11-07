namespace java com.sankuai.sgagent.thrift.model
namespace cpp com.sankuai.cmtrace

struct SGLog 
{
    1:string appkey;
    2:i64 time;
    3:i32 level;
    4:string content;
}

struct LogList 
{
    1: required list<SGLog> logs;
}

struct SGModuleInvokeInfo 
{
    1:string traceId;
    2:string spanId;
    3:string spanName;
    4:string localAppKey;
    5:string localHost;
    6:i32 localPort;
    7:string remoteAppKey;
    8:string remoteHost;
    9:i32 remotePort;
    10:i64 start;  //开始时间戳
    11:i32 cost;    //耗时ms
    12:i32 type;    //调用类型（0为client，1为server）
    13:i32 status;
    14:i32 count;
    15:i32 debug;
    16:string extend;
}

const i32 TRACE_LOG_LIST = 1;
const i32 ERROR_LOG_LIST = 2;
const i32 TRACE_THRESHOLD_LOG_LIST = 3;
const i32 DROP_REQUEST_LIST = 4;
const i32 MTRACE_LOG_LIST = 5; // TODO define in mtrace.jar?

struct CommonLog {
    1: required i32 cmd;
    2: required binary content;
    3: optional string extend;
}

struct ErrorLog {
    1: string appkey;
    2: i64 time;
    3: i32 level;
    4: string category;
    5: string content;
}

struct ErrorLogList {
    1: list<ErrorLog> logs;
}

struct TraceLog {
    1: string traceId;
    2: string spanId;
    3: string spanName;
    4: string localAppKey;
    5: string localHost;
    6: i32 localPort;
    7: string remoteAppKey;
    8: string remoteHost;
    9: i32 remotePort;
    10: i64 start;
    11: i32 cost;
    12: i32 type;
    13: i32 status;
    14: i32 count;
    15: i32 debug;
    16: string extend;
}

struct TraceLogList {
    1: list<TraceLog> logs;
}

struct TraceThresholdLog {
    1: string traceId;
    2: string spanId;
    3: string spanName;
    4: string localAppKey;
    5: string localHost;
    6: i32 localPort;
    7: string remoteAppKey;
    8: string remoteHost;
    9: i32 remotePort;
    10: i64 start;
    11: i32 cost;
    12: i32 type;
    13: i32 status;
    14: i32 count;
    15: i32 debug;
    16: string extend;
}

struct TraceThresholdLogList {
    1: list<TraceThresholdLog> logs;
}

struct DropRequest {
    1: string appkey;
    2: string host;
    3: string remoteAppkey;
    4: string spanname;
    5: i64 start;
    6: i64 count;
    7: i32 type;
}

struct DropRequestList {
    1: list<DropRequest> requests;
}

struct PerfCostData {
    1: i32 cost;
    2: i64 count;
}

struct PerfCostDataList {
    1: list<PerfCostData> costDataList;
}
