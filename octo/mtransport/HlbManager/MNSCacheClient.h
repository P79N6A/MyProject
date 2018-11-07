//
//  MNSCacheClient.h
//  HlbManager
//
//  Created by zhangjinlu on 15/11/18.
//  Copyright (c) 2015年 zhangjinlu. All rights reserved.
//

#ifndef __HLB_MNSCACHE_CLIENT_H__
#define __HLB_MNSCACHE_CLIENT_H__

#include <boost/shared_ptr.hpp>
#include <transport/TBufferTransports.h>
#include <transport/TSocket.h>
#include <protocol/TBinaryProtocol.h>
#include "./hlb_gen_cpp/MNSCacheService.h"
#include "./SgAgentClient.h"

namespace inf {
namespace hlb {
using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;

class MNSCacheConnHandler {
public:
    int init(const std::string& host, int port);
    int checkConnection();
    int createConnection();
    int closeConnection();

    boost::shared_ptr<MNSCacheServiceClient> getClient() {
        return m_client;
    }
    
    bool m_closed;
    std::string m_host;
    int m_port;
    
    boost::shared_ptr<MNSCacheServiceClient> m_client;
    boost::shared_ptr<TSocket> m_socket;
    boost::shared_ptr<TTransport> m_transport;
    boost::shared_ptr<TProtocol> m_protocol;
};


//**  封装mnsc thrift接口
class MNSCacheClientCollector
{
public:
    MNSCacheClientCollector();
    
    //获取Upstream列表
    int getHlbUpstream(std::map<std::string, std::string>& upstreams,
                       const std::string& nginx_type,
                       const std::string& idc_type);
private:
    MNSCacheConnHandler* _getConn();
    
    //获取服务列表，每隔10s检查一次服务列表是
    int _getServiceList();
    
private:
    //sg_agent client
    boost::shared_ptr<SgAgentClientCollector> _sgagent_client_controller;
    
    std::string _mnsc_appkey;
    std::string _env;
    int _last_check_time;
    std::vector<SGService> m_serviceList;
};

}
}
#endif  //__HLB_MNSCACHE_CLIENT_H__
