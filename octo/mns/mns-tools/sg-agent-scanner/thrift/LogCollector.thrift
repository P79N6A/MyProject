include 'sgagent_data.thrift'

service LogCollectorService {
    /*
     *上报通用日志
     */
    i32 uploadCommonLog(1:sgagent_data.CommonLog oLog);
    /*
     *上报日志
     */
    i32 uploadLog(1:sgagent_data.SGLog oLog);
 
    /*
     *上报调用信息
     */
    i32 uploadModuleInvoke(1:sgagent_data.SGModuleInvokeInfo oInfo);
}
