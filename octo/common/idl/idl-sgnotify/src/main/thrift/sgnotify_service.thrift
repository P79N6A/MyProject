namespace java com.sankuai.octo.sgnotify.service
include  "../../../../idl-common/src/main/thrift/config_common.thrift"
include  "sgnotify_data.thrift"

typedef sgnotify_data.ConfigUpdateEvent ConfigUpdateEvent
typedef sgnotify_data.ConfigUpdateResult ConfigUpdateResult
typedef config_common.ConfigFileRequest ConfigFileRequest
typedef config_common.ConfigFileResponse ConfigFileResponse

service SgNotify
{
    string Notify(1:i32 cmdType, 2:string sData);

    ConfigUpdateResult notifyConfig(1:ConfigUpdateEvent event);

    ConfigFileResponse distributeConfigFile(1:ConfigFileRequest request);

    ConfigFileResponse enableConfigFile(1:ConfigFileRequest request);
}

