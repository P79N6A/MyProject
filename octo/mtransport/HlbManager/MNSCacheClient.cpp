//
//  MNSCacheClient.cpp
//  HlbManager
//
//  Created by zhangjinlu on 15/11/18.
//  Copyright (c) 2015年 zhangjinlu. All rights reserved.
//

#include <boost/lexical_cast.hpp>
#include "tinyxml2.h"
#include "log4cplus.h"
#include "Config.h"
#include "./utils/CommonTool.h"
#include "MNSCacheClient.h"

using namespace std;
using namespace tinyxml2;
using namespace inf::hlb;

static const int CONN_RETRY = 3;      //链接mnsc失败重试次数
static const int MAX_TIMEOUT = 300;   //thrift连接、发送、接收最大超时时间
static const int SERVICE_LIST_POLLING_INTERVAL = 30; //每隔30秒访问sg_agent拉取最新MNSC服务列表

int MNSCacheConnHandler::init(const std::string& host, int port) {
    m_host = host;
    m_port = port;
    
    LOG_INFO( "[init] connect to mnscache ip: " << m_host.c_str() << " port is : " << m_port);
    m_socket = boost::shared_ptr<TSocket>(new TSocket(m_host, m_port));
    m_transport = boost::shared_ptr<TFramedTransport>(new TFramedTransport(m_socket));
    m_protocol = boost::shared_ptr<TBinaryProtocol>(new TBinaryProtocol(m_transport));
    
    m_socket->setConnTimeout(MAX_TIMEOUT);
    m_socket->setSendTimeout(MAX_TIMEOUT);
    m_socket->setRecvTimeout(MAX_TIMEOUT);
    
    int ret = createConnection();
    if (ret != 0)
    {
        LOG_WARN( "[init] mnscache create connection fail!");
        return -1;
    }
    //创建成功，置连接标示为未删除
    m_closed = false;
    
    m_client = boost::shared_ptr<MNSCacheServiceClient>(new MNSCacheServiceClient(m_protocol));
    
    srand(time(0));
    
    return 0;
}

//check连接
int MNSCacheConnHandler::checkConnection()
{
    if(!m_transport->isOpen()) {
        return -1;
    }
    
    return 0;
}

//创建连接
int MNSCacheConnHandler::createConnection()
{
    int count = 0;
    bool flag = true;
    //先check连接是否可用
    int ret = checkConnection();
    
    //如果连接不可用，则重新建立连接
    while ((ret != 0) && count < CONN_RETRY)
    {
        count ++;
        try
        {
            m_transport->open();
            flag = true;
            LOG_INFO( "connect to mnscache ok! ip: " << m_host.c_str() << " port is : " << m_port);
        }
        catch(TException& e)
        {
            flag = false;
            LOG_WARN( "connect to mnscache failed ip: " << m_host.c_str() << " port is : " << m_port << ", error : " << e.what());
        }
        
        //获取连接状态
        ret = checkConnection();
    }
    
    if(flag && ret == 0)
    {
        return 0;
    } else {
        return -1;
    }
}

//关闭连接
int MNSCacheConnHandler::closeConnection()
{
    if (!m_closed)
    {
        //置连接关闭标示为true，防止多次close
        m_closed = true;
        try
        {
            m_transport->close();
        }
        catch(TException& e)
        {
            LOG_ERROR("ERR, close connection fail! error: " << e.what());
            return -1;
        }
    }
    
    return 0;
}


MNSCacheClientCollector::MNSCacheClientCollector() {
    _mnsc_appkey = HlbConfig::instance().m_mnscAppkey;
    _last_check_time = 0;
    m_serviceList.clear();
    
    _sgagent_client_controller = boost::shared_ptr<SgAgentClientCollector> (
                        new SgAgentClientCollector());
    //拉取本地环境，初始化_env
    _env = _sgagent_client_controller->getEnvironment();
}

/*
 * 从mnscache获取upstream
 */
int MNSCacheClientCollector::getHlbUpstream(
        std::map<std::string, std::string>& upstreams,
        const std::string& nginx_type,
        const std::string& idc_type) {
    MNSCacheConnHandler* connHandler = _getConn();
    if (!connHandler) {
        LOG_ERROR("[getHlbUpstream] ERR _getConn failed!");
        return -1;
    }
    
    int retCode = 0;
    UpstreamResponse upstreamRes;
    try {
        connHandler->getClient()->getHlbUpstream(upstreamRes, nginx_type, idc_type, _env);
        if (upstreamRes.code == 200 && upstreamRes.__isset.upstreams) {
            upstreams = upstreamRes.upstreams;
        }
        LOG_INFO("[getHlbUpstream] call MNSC ok! nginx_type|idc_type|env = "
                 << nginx_type<<"|"<< idc_type <<"|"<< _env
                 <<" , upstream.size=" << upstreams.size() <<" code="<< upstreamRes.code);
    } catch(TException& e) {
        LOG_ERROR( "getHlbUpstream ERROR. " << e.what());
        retCode = -1;
    }
    
    //使用完成，关闭连接
    connHandler->closeConnection();
    //释放内存
    SAFE_DELETE(connHandler);
    
    //返回错误码code
    return retCode;
}


//获取服务列表,放入m_serviceList中
int MNSCacheClientCollector::_getServiceList()
{
    //如果缓存数据非空，则每隔10秒更新一次缓存数据; 若缓存为空，则立即获取列表
    int cur_time = time(0);
    if((cur_time < (_last_check_time + SERVICE_LIST_POLLING_INTERVAL)) && (m_serviceList.size() != 0)) {
        return 0;
    }
    
    std::vector<SGService> serviceList;
    string localAppkey = HlbConfig::instance().m_hlbManagerAppkey;
    _sgagent_client_controller->getServiceList( serviceList, localAppkey, _mnsc_appkey);
    
    //更新时间
    _last_check_time = cur_time;
    
    if (serviceList.size() == 0) {
        LOG_ERROR( "ERR mnscache service list return null");
        return -2;
    }
    
    //更新服务列表
    m_serviceList = serviceList;
    
    return 0;
}

MNSCacheConnHandler* MNSCacheClientCollector::_getConn() {
    //获取服务列表
    if(_getServiceList() != 0) {
        LOG_ERROR( "[_getConn] getServiceList FAILED!");
        return NULL;
    }
    //若获取的为空，直接返回
    int handlerListSize = m_serviceList.size();
    if (handlerListSize <= 0) {
        LOG_ERROR( "[_getConn] getServiceList size <= 0 !");
        return NULL;
    }
    //随机选择一个server,创建连接
    int beginIndex = rand() % handlerListSize;
    int index = beginIndex;
    do {
        if (m_serviceList[index].status == 2)
        {
            MNSCacheConnHandler* pCollector = new MNSCacheConnHandler();
            int ret = pCollector->init(m_serviceList[index].ip, m_serviceList[index].port);
            
            if((ret == 0) && pCollector->m_transport->isOpen())
            {
                return pCollector;
            } else {
                SAFE_DELETE(pCollector);
                index = (index + 1) % handlerListSize;
            }
        } else {
            //如果本次server不可用，则尝试连接下一个server
            index = (index + 1) % handlerListSize;
        }
    } while(index != beginIndex);
    
    LOG_ERROR("[MNSCacheClientCollector::_getConn] fail!");
    return NULL;
}

