namespace java com.sankuai.octo.oswatch.thrift.service

include "oswatch_data.thrift"

typedef oswatch_data.MonitorPolicy MonitorPolicy
typedef oswatch_data.OswatchResponse OswatchResponse

service OSWatchService {

     OswatchResponse addMonitorPolicy(1: MonitorPolicy monitorPolicy, 2: string responseUrl);

     OswatchResponse updateMonitorPolicy(1: i64 oswatchId, 2: MonitorPolicy monitorPolicy, 3: string responseUrl);

     OswatchResponse delMonitorPolicy(1: i64 monitorPolicyId);
}
