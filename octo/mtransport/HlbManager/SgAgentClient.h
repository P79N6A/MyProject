#ifndef __HLB_SG_AGENT_CLIENT_H__
#define __HLB_SG_AGENT_CLIENT_H__

#include <boost/shared_ptr.hpp>
#include <protocol/TBinaryProtocol.h>
#include <transport/TBufferTransports.h>
#include <transport/TSocket.h>
#include "./hlb_gen_cpp/SGAgent.h"

namespace inf {
namespace hlb {
using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;

//**  sg_agent链接管理
class SgAgentConnHandler {
public:
    int init();
    int checkConnection();
    int createConnection();
    int closeConnection();
    boost::shared_ptr<SGAgentClient> getClient() {
        return m_client;
    }

    bool m_closed;
    std::string m_host;
    int m_port;
    
    boost::shared_ptr<SGAgentClient> m_client;
    boost::shared_ptr<TSocket> m_socket;
    boost::shared_ptr<TTransport> m_transport;
    boost::shared_ptr<TProtocol> m_protocol;
};



//**  封装sg_agent thrift接口
class SgAgentClientCollector {
public:
    SgAgentClientCollector() {}
    
    //获取本地环境
    std::string getEnvironment();
    //注册HlbManager
    //return:  true 注册成功
    //        false 注册失败
    bool registService();

    //sg_agent原始接口，根据业务线编号获取appkeyList
    void getAppKeyListByBusinessLine(std::vector<std::string>& appKeyList,
                                     const int businessCode);
    
    //sg_agent原始接口，根据remoteAppkey获取该appkey下的http服务节点列表
    void getHttpServiceList(std::vector< SGService>& httpServiceList,
                            const std::string& localAppkey,
                            const std::string& remoteAppkey);
    
    //sg_agent原始接口，根据remoteAppkey获取该appkey下的thrift服务节点列表
    void getServiceList(std::vector< SGService>& serviceList,
                        const std::string& localAppkey,
                        const std::string& remoteAppkey);
    
    //sg_agent原始接口，根据remoteAppkey获取该appkey的降级action列表
    void getDegradeActions(std::vector< DegradeAction>& degradeActions,
                           const std::string& localAppkey,
                           const std::string& remoteAppkey);

    //sg_agent原始接口，根据业务线编号获取该业务线所有appkey的HttpProperties
    //返回值为 map< appkey, map< propName, propValue> >
    void getHttpPropertiesByBusinessLine(
            std::map<std::string, HttpProperties>& httpPropertiesMap,
            const int bizCode);
    
    //根据业务线编号获取该业务线所有appkey下的全部http服务列表
    //返回值为map<appkey, vector< SGService> >
    void getHttpServiceListByBusinessLine(
            std::map< std::string, std::vector< SGService> >& httpServiceListMap,
            const int businessCode);
private:
    SgAgentConnHandler* _getConn();

};

}
}
#endif //__HLB_SG_AGENT_CLIENT_H__
