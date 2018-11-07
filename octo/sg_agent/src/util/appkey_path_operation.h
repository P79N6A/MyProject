#ifndef __appkey_path_H__
#define __appkey_path_H__

#include <string>

namespace sg_agent {

static const std::string ProtocolProviderThriftTail = "provider";
static const std::string ProtocolRouteThriftTail = "route";
static const std::string ProtocolProviderHttpTail = "provider-http";
static const std::string ProtocolRouteHttpTail = "route-http";

class SGAgentAppkeyPath {
public:
    //根据key把remotekey, protocol解析出来
    static int _deGenKey(const std::string&, std::string&, std::string& protocol);

    //根据传入zkPath，解析出appkey, protocol
    static int deGenZkPath(const char* zkPath, std::string &appkey, std::string &protocol);

    static int deGenNodeType(std::string &protocol);
};

} //namespace
#endif
