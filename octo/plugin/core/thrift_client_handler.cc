#include "util/tinyxml2.h"
#include <string.h>
#include "../plugindef.h"
#include <glog/logging.h>

#include "thrift_client_handler.h"
#include "controlServer/ControllerService.h"
#include "controlServer/controlServer_types.h"

static int RETRY = 1;

using namespace tinyxml2;

namespace cplugin{
    


CpluginClientHandler::CpluginClientHandler()
        : m_client(NULL) {
}

CpluginClientHandler::~CpluginClientHandler() {
    if(m_client) {
        if (CONTROL_SERVER == type) {
            delete static_cast<Controller::ControllerServiceClient *>(m_client);
        } else {
            LOG(ERROR) << "Wrong Type: " << type;
        }

    }
}

int CpluginClientHandler::init(const std::string& host, int port, ProcType proc_type) {
    m_host = host;
    m_port = port;
    type = proc_type;

    m_socket = boost::shared_ptr<apache::thrift::transport::TSocket>(new apache::thrift::transport::TSocket(m_host, m_port));
    m_transport = boost::shared_ptr<apache::thrift::transport::TFramedTransport>(new apache::thrift::transport::TFramedTransport(m_socket));
    m_protocol = boost::shared_ptr<apache::thrift::protocol::TBinaryProtocol>(new apache::thrift::protocol::TBinaryProtocol(m_transport));


    int ret = createConnection();
    if (ret != 0)
    {
        LOG(ERROR) <<  "init create connection fail! ret = " << ret;
        return ret;
    }
    //创建成功，置连接标示为未删除
    m_closed = false;

    if(NULL == m_client) {
        if (CONTROL_SERVER == type) {
            //设置超时100ms
            m_socket->setConnTimeout(CONTROL_SERVER_TIMEOUT);
            m_socket->setSendTimeout(CONTROL_SERVER_TIMEOUT);
            m_socket->setRecvTimeout(CONTROL_SERVER_TIMEOUT);
            m_client =(void*)(new Controller::ControllerServiceClient(m_protocol));
        }  else {
            m_client = NULL;
        }
    }

    LOG(INFO) << "connect to CpluginClientHandler ip: " << m_host.c_str()
                                                    << " port is : " << m_port
                                                    << " type is : " << proc_type
                                                    << " m_client : " << m_client
                                                    << ", timeout = " << CONTROL_SERVER_TIMEOUT;
    srand(time(0));

    return 0;
}

//check连接
int CpluginClientHandler::checkConnection()
{
    if(!m_transport->isOpen()) {
        return ERR_CHECK_CONNECTION;
    }
    return 0;
}

//创建连接
int CpluginClientHandler::createConnection()
{
    int count = 0;
    bool flag = true;
    //先check连接是否可用
    int ret = checkConnection();
    //如果连接不可用，则重新建立连接
    while ((ret != 0) && count < RETRY)
    {
        count ++;
        try
        {
            m_transport->open();
            flag = true;
            LOG(INFO) <<  "reconnect to  ok! ip: " << m_host.c_str()
                                                           << " port is : " << m_port;
        }
        catch(apache::thrift::TException& e)
        {
            flag = false;
            LOG(ERROR) << "reconnect to  failed ip: " << m_host.c_str()
                                                              << " port is : " << m_port
                                                              << ", error : " << e.what()  ;
        }

        //获取连接状态
        ret = checkConnection();
    }

    if(flag && ret == 0)
    {
        return 0;
    } else {
        LOG(ERROR) << "CpluginClientHandler failed! ret = " << ret << "flag = " << flag;
        return ERR_CREATE_CONNECTION;
    }
}

//关闭连接
int CpluginClientHandler::closeConnection()
{
    if (!m_closed)
    {
        //置连接关闭标示为true，防止多次close
        m_closed = true;
        try
        {
            LOG(INFO) <<  "begin close connection !";
            if (NULL != m_transport) {
                m_transport->close();
            }
            else {
                LOG(ERROR) << "m_transport is NULL when to close";
            }
        }
        catch(apache::thrift::TException& e)
        {
            LOG(ERROR) << "ERR, close connection fail! error : " << e.what();
            return ERR_CLOSE_CONNECTION;
        }
    }
    return 0;
}


//check handler对应的连接是否可用
int CpluginClientHandler::checkHandler()
{
    //先check连接
    int ret = checkConnection();
    if (ret != 0)
    {
        LOG(ERROR) << " connection lost, begin close! ret = " << ret;
        //关闭连接
        ret = closeConnection();
        if (ret != 0)
        {
            LOG(ERROR) <<  " connection lost, close fail! ret = " << ret;
            return ret;
        }
        //重新创建连接
        ret = createConnection();
        if (ret != 0)
        {
            LOG(ERROR) << " re-create connection fail! ret = " << ret;
            return ret;
        }
        //创建成功，修改连接标示为连接未被关闭
        m_closed = false;
    }

    return 0;
}

}