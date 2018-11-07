namespace java com.sankuai.octo.sgnotify.service
include  "MtConfig.thrift"
include  "sgnotify_data.thrift"

service SgNotify
{
    string Notify(1:i32 cmdType, 2:string sData);

    ConfigUpdateResult notifyConfig(1:ConfigUpdateEvent event);

    configData.ConfigFileResponse distributeConfigFile(1:configData.ConfigFileRequest request);

    configData.ConfigFileResponse enableConfigFile(1:configData.ConfigFileRequest request);
}

