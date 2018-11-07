namespace java com.sankuai.octo.aggregator.thrift.service
include  "../../../../idl-common/src/main/thrift/aggregator_common.thrift"

typedef aggregator_common.SGModuleInvokeInfo SGModuleInvokeInfo
typedef aggregator_common.CommonLog CommonLog
typedef aggregator_common.SGLog SGLog

service AggregatorService {

    i32 uploadLog(1:SGLog log);
 
    i32 uploadModuleInvoke(1:SGModuleInvokeInfo log);

    i32 uploadCommonLog(1:CommonLog log);
}
