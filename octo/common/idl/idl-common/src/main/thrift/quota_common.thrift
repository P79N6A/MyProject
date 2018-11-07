namespace java com.sankuai.octo.oswatch.thrift.data

const string DEFAULT_CONSUMER_APPKEY="CONSUMERS_DEGRADE_WITHOUT_QUOTA"

enum DegradeStatus {
    ENABLE = 0,
    DISABLE = 1,
}

enum AlarmStatus {
    ENABLE = 0,
    DISABLE = 1,
}

enum DegradeStrategy {
    DROP = 0,
    CUSTOMIZE = 1,
}

enum DegradeEnd {
    SERVER = 0,
    CLIENT = 1,
}

enum ProviderNumCountSwitch {
    APPKEY = 0,
    HOST = 1,
}

struct ConsumerQuota {
    1:required string consumerAppkey,
    2:required double QPSRatio, //from 0 to 1
    3:required DegradeStrategy degradeStrategy,
    4:optional string degradeRedirect,
}

struct ProviderQuota {
    1:required string id,
    2:required string name;
    3:required string providerAppkey;
    4:required i32 env;
    5:required string method;
    6:required i32 QPSCapacity;
    9:required list<ConsumerQuota> consumerList;
    10:required DegradeStatus status;
    11:required i32 watchPeriodInSeconds = 10;
    12:required i64 createTime;
    13:required i64 updateTime;
    14:optional AlarmStatus alarm = AlarmStatus.ENABLE;
    15:optional DegradeEnd degradeEnd = DegradeEnd.CLIENT;
    16:optional ProviderNumCountSwitch proNumCntSwitch = ProviderNumCountSwitch.APPKEY;
}

struct DegradeAction {
    1:required string id; //consumerAppkey + method + providerQuota.id
    2:required i32 env;
    3:required string providerAppkey;
    4:required string consumerAppkey;
    5:required string method;
    6:required double degradeRatio; // from 0 to 1
    7:required DegradeStrategy degradeStrategy;
    8:required i64 timestamp;
    9:optional string degradeRedirect;
   10:optional i32 consumerQPS;
   11:optional DegradeEnd degradeEnd = DegradeEnd.CLIENT;
   12:optional string extend;
}
