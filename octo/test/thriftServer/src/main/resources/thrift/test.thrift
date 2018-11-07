namespace java com.sankuai.octo.test
include 'fb303.thrift'

struct SGLog {
    1:string appkey;
    2:i64 time;
    3:i32 level;
    4:string content;
}
struct SGModuleInvokeInfo {
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
 
service LogCollectorService extends fb303.FacebookService {
    /*
     *上报日志
     */
    i32 uploadLog(1:SGLog oLog);
 
    /*
     *上报调用信息
     */
    i32 uploadModuleInvoke(1:SGModuleInvokeInfo oInfo);
}