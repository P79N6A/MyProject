#include <boost/lexical_cast.hpp>
#include "tinyxml2.h"
#include "log4cplus.h"
#include "Config.h"
#include "./utils/CommonTool.h"
#include "SgAgentClient.h"

using namespace std;
using namespace tinyxml2;
using namespace inf::hlb;

static const int CONN_RETRY = 3;      //链接sg_agent失败重试次数
static const int MAX_TIMEOUT = 250;   //thrift连接、发送、接收最大超时时间

int SgAgentConnHandler::init() {
    m_host = HlbConfig::instance().m_sgagentIp;
    m_port = HlbConfig::instance().m_sgagentPort;
    LOG_DEBUG( "[SgAgentConnHandler::init] connect to sg_agent ip=" << m_host.c_str() << " port=" << m_port);
    
    m_socket = boost::shared_ptr<TSocket>(new TSocket(m_host, m_port));
    m_transport = boost::shared_ptr<TFramedTransport>(new TFramedTransport(m_socket));
    m_protocol = boost::shared_ptr<TBinaryProtocol>(new TBinaryProtocol(m_transport));

    m_socket->setConnTimeout(MAX_TIMEOUT);
    m_socket->setSendTimeout(MAX_TIMEOUT);
    m_socket->setRecvTimeout(MAX_TIMEOUT);

    int ret = createConnection();
    if (ret != 0) {
        LOG_ERROR( "[SgAgentConnHandler::init] sg_agent connection create fail!");
        return -1;
    }
    
    //创建成功，置连接标示为未删除
    m_closed = false;
    m_client = boost::shared_ptr<SGAgentClient>(new SGAgentClient(m_protocol));

    return 0;
}

//check连接
int SgAgentConnHandler::checkConnection()
{
    if(!m_transport->isOpen()) {
        return -1;
    }
    return 0;
}

//创建连接
int SgAgentConnHandler::createConnection()
{
    int count = 0;
    bool flag = true;
    //先check连接是否可用
    int ret = checkConnection();
    
    //如果连接不可用，则重新建立连接
    while ((ret != 0) && count < CONN_RETRY) {
        count ++;
        try {
            m_transport->open();
            flag = true;
            LOG_INFO( "[SgAgentConnHandler::createConnection] connect to sg_agent ok!"
                      <<" ip=" << m_host.c_str() << " port=" << m_port);
        } catch(TException& e) {
            flag = false;
            LOG_WARN( "[SgAgentConnHandler::createConnection] connect to sg_agent failed!"
                       <<" ip=" << m_host.c_str() << " port=" << m_port<< ", error : " << e.what());
        }

        //获取连接状态 
        ret = checkConnection();
    }
    
    if(flag && ret == 0) {
        return 0;
    } else {
        return -1;
    }
}

//关闭连接
int SgAgentConnHandler::closeConnection()
{
    if (!m_closed) {
        //置连接关闭标示为true，防止多次close
        m_closed = true;
        try {
            m_transport->close();
        } catch(TException& e) {
            LOG_ERROR("ERR, close connection fail! error: " << e.what());
            return -1; 
        }
    }

    return 0;
}


/* =============================================================================
 * ======================== SgAgentClientCollector =============================
 */

SgAgentConnHandler* SgAgentClientCollector::_getConn() {
    SgAgentConnHandler* connHandler = new SgAgentConnHandler();
    int ret = connHandler->init();
    
    if((ret == 0) && connHandler->m_transport->isOpen()) {
        return connHandler;
    } else {
        SAFE_DELETE(connHandler);
    }
    
    LOG_WARN("[SgAgentClientCollector::_getConn] fail!");
    return NULL;
}

/*
 * 获取本地环境
 * 返回值为："prod" "stage" "test"
 */
std::string SgAgentClientCollector::getEnvironment() {
    string env = "prod";

    SgAgentConnHandler* connHandler = _getConn();
    if (!connHandler) {
        LOG_ERROR("[getEnvironment] ERROR _getConn failed!");
        return env;
    }

    try {
        START_TIME
        int i_env = 3; //default prod
        i_env = connHandler->getClient()->getEnv();
        switch (i_env) {
            case 1: {
                env = "test";
                break;
            }
            case 2: {
                env = "stage";
                break;
            }
            case 3: {
                env = "prod";
                break;
            }
            default:
                env = "prod";
        }
        std::string cost_info = "sg_agent::getEnvironment = " + env;
        END_TIME(cost_info)
        LOG_INFO( "[getEnvironment] ok! env = " << env );
    } catch(TException& e) {
        LOG_ERROR( "[getEnvironment] ERROR : " << e.what());
    }

    //使用完成，关闭连接
    connHandler->closeConnection();
    //释放内存
    SAFE_DELETE(connHandler); 
    return env;
}

//int32_t registService(const SGService& oService)
//HlbManager进行服务注册
bool SgAgentClientCollector::registService() {
    bool ret=true;
    SgAgentConnHandler* connHandler = _getConn();
    if (!connHandler) {
        LOG_ERROR("[registService] ERROR _getConn failed!");
        return false;
    }

    try {
        START_TIME
        SGService hlb_serv;
        string local_ip;
        getIntranet(local_ip);
        hlb_serv.__set_ip(local_ip);
        hlb_serv.__set_port( HlbConfig::instance().m_hlbManagerHttpPort);
        hlb_serv.__set_appkey( HlbConfig::instance().m_hlbManagerAppkey);
        hlb_serv.__set_serverType(1); //0:thrift  1:http  2:thrift over http
        hlb_serv.__set_version("original_http");
		hlb_serv.__set_weight(10);
		hlb_serv.__set_fweight(10.0);
        int regist_ret = connHandler->getClient()->registService( hlb_serv);
        std::string cost_info = "sg_agent::registService";
        END_TIME(cost_info)
        
        if (0!=regist_ret) {
            LOG_WARN( "[registService] FAILED. ret="<<regist_ret
                     <<"  ip|port = "<< local_ip <<"|"<< HlbConfig::instance().m_hlbManagerHttpPort);
            ret = false;
        } else {
            LOG_INFO( "[registService] SUCCEED appkey=" << HlbConfig::instance().m_hlbManagerAppkey
                     <<" ip|port = "<< local_ip <<"|"<< HlbConfig::instance().m_hlbManagerHttpPort);
        }
    } catch(TException& e) {
        LOG_ERROR( "[registService] ERROR : " << e.what());
        ret = false;
    }

    //使用完成，关闭连接
    connHandler->closeConnection();
    //释放内存
    SAFE_DELETE(connHandler);
    return ret;
}

/*
 * 根据业务线编号获取appKeyList
 */
void SgAgentClientCollector::getAppKeyListByBusinessLine (
        vector< string>& appKeyList, const int businessCode) {
    
    SgAgentConnHandler* connHandler = _getConn();
    if (!connHandler) {
        LOG_ERROR("[getAppKeyListByBusinessLine] ERROR _getConn failed!");
        return;
    }
    
    try {
        START_TIME
        connHandler->getClient()->getAppKeyListByBusinessLine( appKeyList, businessCode);
        std::string strBizCode = boost::lexical_cast<string>(businessCode);
        std::string cost_info = "sg_agent::getAppKeyListByBusinessLine biz="+strBizCode;
        END_TIME(cost_info)
        LOG_INFO( "[getAppKeyListByBusinessLine] ok! bizCode=" << businessCode
                  <<" appKeyList.size="<< appKeyList.size());
    } catch(TException& e) {
        LOG_ERROR( "[getAppKeyListByBusinessLine] ERROR : " << e.what());
    }
    
    //使用完成，关闭连接
    connHandler->closeConnection();
    //释放内存
    SAFE_DELETE(connHandler);
}

/*
 * 根据remoteAppkey获取该appkey下的http服务节点列表
 */
void SgAgentClientCollector::getHttpServiceList(
        std::vector< SGService>& httpServiceList,
        const std::string& localAppkey,
        const std::string& remoteAppkey) {
    SgAgentConnHandler* connHandler = _getConn();
    if (!connHandler) {
        LOG_ERROR("[getHttpServiceList] ERROR _getConn failed!");
        return;
    }
    
    try {
        START_TIME
        connHandler->getClient()->getHttpServiceList(httpServiceList,
                                                     localAppkey,
                                                     remoteAppkey);
        std::string cost_info = "sg_agent::getHttpServiceList remote="+remoteAppkey;
        END_TIME(cost_info)
        LOG_INFO( "[getHttpServiceList] ok! remote="<<remoteAppkey
                 <<" httpServiceList.size="<< httpServiceList.size());
    } catch(TException& e) {
        LOG_ERROR( "[getHttpServiceList] ERROR : " << e.what());
    }
    
    //使用完成，关闭连接
    connHandler->closeConnection();
    //释放内存
    SAFE_DELETE(connHandler);
}

/*
 * 根据remoteAppkey获取该appkey下的thrift服务节点列表
 */
void SgAgentClientCollector::getServiceList(
            std::vector< SGService>& serviceList,
            const std::string& localAppkey,
            const std::string& remoteAppkey) {
    SgAgentConnHandler* connHandler = _getConn();
    if (!connHandler) {
        LOG_ERROR("[getHttpServiceList] ERROR _getConn failed!");
        return;
    }
    
    try {
        START_TIME
        connHandler->getClient()->getServiceList(serviceList, localAppkey, remoteAppkey);
        std::string cost_info = "sg_agent::getServiceList remote="+remoteAppkey;
        END_TIME(cost_info)
        LOG_INFO( "[getServiceList] ok! remote="<<remoteAppkey
                 <<" getServiceList.size="<< serviceList.size());
    } catch(TException& e) {
        LOG_ERROR( "[getServiceList] ERROR : " << e.what());
    }
    
    //使用完成，关闭连接
    connHandler->closeConnection();
    //释放内存
    SAFE_DELETE(connHandler);
}

/*
 * 根据remoteAppkey获取该appkey的降级action列表
 */
void SgAgentClientCollector::getDegradeActions( 
        std::vector< DegradeAction>& degradeActions,
        const std::string& localAppkey,
        const std::string& remoteAppkey) {
    SgAgentConnHandler* connHandler = _getConn();
    if (!connHandler) {
        LOG_ERROR("[getDegradeActions] ERROR _getConn failed!");
        return;
    }
    
    try {
        START_TIME
        connHandler->getClient()->getDegradeActions(degradeActions,
                                                    localAppkey,
                                                    remoteAppkey);
        std::string cost_info = "sg_agent::getDegradeActions remote="+remoteAppkey;
        END_TIME(cost_info)
        LOG_INFO( "[getDegradeActions] ok! remote="<<remoteAppkey
                 <<" degradeActions.size="<< degradeActions.size());
    } catch(TException& e) {
        LOG_ERROR( "[getDegradeActions] ERROR : " << e.what());
    }
    
    //使用完成，关闭连接
    connHandler->closeConnection();
    //释放内存
    SAFE_DELETE(connHandler);
}

/*
 * 根据业务线编号获取该业务线所有appkey的HttpProperties
 * 返回值为 map< appkey, map< propName, propValue> >
 */
void SgAgentClientCollector::getHttpPropertiesByBusinessLine(
        map<std::string, HttpProperties>& httpPropertiesMap,
        const int bizCode) {
    SgAgentConnHandler* connHandler = _getConn();
    if (!connHandler) {
        LOG_ERROR("[getHttpPropertiesByBusinessLine] ERROR _getConn failed!");
        return;
    }
    
    try {
        START_TIME
        connHandler->getClient()->getHttpPropertiesByBusinessLine(
                                        httpPropertiesMap, bizCode);
        string strBizCode = boost::lexical_cast<std::string>(bizCode);
        std::string cost_info = "sg_agent::getHttpPropertiesByBusinessLine bizCode="+strBizCode;
        END_TIME(cost_info)
        LOG_INFO( "[getHttpPropertiesByBusinessLine] ok! bizCode="<<bizCode
                 <<" httpPropertiesMap.size="<< httpPropertiesMap.size());
    } catch(TException& e) {
        LOG_ERROR( "[getHttpPropertiesByBusinessLine] ERROR : " << e.what());
    }
    
    //使用完成，关闭连接
    connHandler->closeConnection();
    //释放内存
    SAFE_DELETE(connHandler);
}


/*
 * 根据业务线编号获取该业务线下的全部http服务列表
 * 返回值为map<appkey, vector< SGService> >
 */
void SgAgentClientCollector::getHttpServiceListByBusinessLine(
        std::map< std::string, std::vector< SGService> >& httpServiceListMap,
        const int businessCode) {
    SgAgentConnHandler* connHandler = _getConn();
    if (!connHandler) {
        LOG_ERROR("[getHttpServiceListByBusinessLine] ERROR _getConn failed!");
        return;
    }
    
    map< string, vector< SGService> > tmpHttpServiceListMap;
    string localAppkey = HlbConfig::instance().m_hlbManagerAppkey;
    try {
        //START_TIME
        //获取该业务线下的所有appKey列表
        vector< string> appKeyList;
        connHandler->getClient()->getAppKeyListByBusinessLine( appKeyList, businessCode);
        LOG_INFO( "[getHttpServiceListByBusinessLine] bizCode=" <<businessCode
                 <<" appKeyList.size="<< appKeyList.size());
        
        //获取各个appKey的serviceList
        for (vector< string>::const_iterator iter= appKeyList.begin();
             iter != appKeyList.end(); ++iter) {
            vector< SGService> tmpServiceList;
            connHandler->getClient()->getHttpServiceList( tmpServiceList, localAppkey, *iter);
            LOG_DEBUG( "[getHttpServiceListByBusinessLine] appKey= "<< *iter
                     <<" ServiceList.size= "<< tmpServiceList.size() );
            tmpHttpServiceListMap[*iter] = tmpServiceList;
        }
        
        string bizCode = boost::lexical_cast<std::string>(businessCode);
        string cost_info = "getHttpServiceListByBusinessLine bizCode="+bizCode;
        //END_TIME(cost_info)
        LOG_INFO( "[getHttpServiceListByBusinessLine] ok! bizCode="<<businessCode
                 <<" httpServiceListMap.size= "<< tmpHttpServiceListMap.size());
        //set返回值
        httpServiceListMap.swap( tmpHttpServiceListMap);
    } catch(TException& e) {
        LOG_ERROR( "[getHttpServiceListByBusinessLine] ERROR : " << e.what());
    }
    
    //使用完成，关闭连接
    connHandler->closeConnection();
    //释放内存
    SAFE_DELETE( connHandler);
}


