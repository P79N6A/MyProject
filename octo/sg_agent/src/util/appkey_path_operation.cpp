#include "appkey_path_operation.h"
#include "comm/inc_comm.h"

namespace sg_agent
{
int SGAgentAppkeyPath::_deGenKey(const std::string& key,
            std::string& remoteappkey, std::string &protocol)
{
    std::size_t found = key.find_last_of("+");
    if(std::string::npos == found || found >= key.size()) 
    {
        LOG_ERROR("key is wrong, key = " << key << ", found = " << found);
        return -1;
    } else {
        protocol = key.substr(0, found);
        remoteappkey = key.substr(found + 1);
    }
    return 0;
}

int SGAgentAppkeyPath::deGenNodeType(std::string &protocol) {
  if(protocol.empty()) {
    LOG_ERROR("protocol in deGenNodeType is empty! ");
    return -1;
  } 
  if(0 == protocol.compare(ProtocolProviderHttpTail)
      || 0 == protocol.compare(ProtocolRouteHttpTail)) {
     protocol = "http";  
  } 
  else if (0 == protocol.compare(ProtocolProviderThriftTail)
    || 0 == protocol.compare(ProtocolRouteThriftTail)) {
      protocol = "thrift";
  }
  else {
    LOG_INFO("when watcher trigger, in deGenNodeType, protocol:" << protocol);
  }
  
  return 0;
}

int SGAgentAppkeyPath::deGenZkPath(const char *zkPath, std::string &appkey, std::string &protocol) {
    std::vector<std::string> pathList;
    int ret = SplitStringIntoVector(zkPath, "/", pathList);
    if (0 < ret) {
        int length = pathList.size();
        //这里必须保证zkPath符合 /mns/sankuai/环境/appkey/(nodeType + protocol)
        if(4 < length) {
            //appkey在数组的下标是4，是因为pathList[0]是""
            appkey = pathList[4];
            if(appkey.empty()) {
                LOG_ERROR("deGenZkPath appkey is empty! zkPath" << zkPath);
                return -3;
            }
            //protocol取最后一个，有可能是cellar, provider, provider-http
            protocol = pathList[length - 1];
            if(protocol.empty()) {
                LOG_ERROR("deGenZkPath protocol is empty! zkPath" << zkPath);
                return -3;
            }
        }
        else {
            LOG_ERROR("zkPath is not complete! zkPath: " << zkPath 
                          << ", appkey: " << appkey 
                          << ", length: " << length);
            return -2;
        }
    } 
    else {
        LOG_ERROR("zkPath is wroing! zkPath: " << zkPath << ", appkey:" << appkey);
        return -1;
    }

    ///Extract protocol from zkpath
    ret = deGenNodeType(protocol);
    if(0 != ret) {
        LOG_ERROR("deGenNodeType is wrong!  protocol:" << protocol); 
    }
       
    return ret;
}

}//namespace
