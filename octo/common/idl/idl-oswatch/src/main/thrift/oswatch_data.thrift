namespace java com.sankuai.octo.oswatch.thrift.data

enum ErrorCode {
    OK  = 0,
    ERROR = 1,
    ACTIVE = 2,
}

enum MonitorType {
    CPU = 0,
    MEM = 1,
    QPS = 2,
    TP50 = 3,
    TP90 = 4,
    TP95 = 5,
    TP99 = 6,
}

enum GteType {
    GTE = 0,
    LTE = 1,
}
enum EnvType {
    TEST = 1,
    STAGE = 2,
    PROD = 3,
}

struct MonitorPolicy {
    1: i64 oswatchId; //oswatch注册id,＝0表示新注册, !=0 表示更新
    2: string appkey;
    3: optional string idc;
    4: EnvType env;
    5: GteType gteType;
    6: i32 watchPeriod;
    7: MonitorType monitorType;
    8: double value;
    9: optional string spanName;
   10: optional i32 providerCountSwitch; //providerCountSwitch=0 表示计算服务的有效节点时by appkey(IP+port)   providerCountSwitch=1 表示by IP(主机数)
}

struct OswatchResponse{
    1: ErrorCode errorCode;
    2: optional i64 oswatchId;
    3: optional double monitorTypeValue;
}
