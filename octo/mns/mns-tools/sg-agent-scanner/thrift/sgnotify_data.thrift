namespace java com.sankuai.octo.sgnotify.model
include  "MtConfig.thrift"

struct ConfigUpdateEvent {
    1: required map<string, list<configData.ConfigNode>> changed;
}

const i32 CODE_SUCCESS = 0;
const i32 CODE_AGENT_CONNECT_ERROR = -1;
const i32 CODE_AGENT_INVOKE_ERROR = -2;
const i32 CODE_AGENT_INTERNAL_ERROR = -3;
const i32 CODE_SGNOTIFY_INTERNAL_ERROR = -4;

struct ConfigUpdateResult {
    1: required map<string, i32> codes;

