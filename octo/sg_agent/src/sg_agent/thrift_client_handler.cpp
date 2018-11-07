#include "comm/tinyxml2.h"
#include "comm/log4cplus.h"
#include "util/SGAgentErr.h"
#include "util/sg_agent_def.h"

#include "thrift_client_handler.h"

static int RETRY = 1;

using namespace tinyxml2;

ThriftClientHandler::ThriftClientHandler()
    : m_client(NULL) {
}

ThriftClientHandler::~ThriftClientHandler() {
  if (m_client) {
    if (COMMONLOG == type) {
      delete static_cast<AggregatorServiceClient *>(m_client);
    } else if (MTCONFIG == type) {
      delete static_cast<MtConfigServiceClient *>(m_client);
    } else if (MNSCache == type) {
      delete static_cast<MNSCacheServiceClient *>(m_client);
    } else {
      LOG_ERROR("Wrong Type: " << type);
    }

  }
}

int ThriftClientHandler::init(const std::string &host, int port, ProcType proc_type) {
  m_host = host;
  m_port = port;
  type = proc_type;

  m_socket = boost::shared_ptr<TSocket>(new TSocket(m_host, m_port));
  m_transport = boost::shared_ptr<TFramedTransport>(new TFramedTransport(m_socket));
  m_protocol = boost::shared_ptr<TBinaryProtocol>(new TBinaryProtocol(m_transport));

  int ret = createConnection();
  if (ret != 0) {
    LOG_FATAL("init create connection fail! ret = " << ret);
    return ret;
  }
  //创建成功，置连接标示为未删除
  m_closed = false;

  if (NULL == m_client) {
    if (COMMONLOG == type) {
      //设置超时100ms
      m_socket->setConnTimeout(sg_agent::LOGCOLLECTOR_TIMEOUT);
      m_socket->setSendTimeout(sg_agent::LOGCOLLECTOR_SEND_TIMEOUT);
      m_socket->setRecvTimeout(sg_agent::LOGCOLLECTOR_SEND_TIMEOUT);
      m_client = (void *) (new AggregatorServiceClient(m_protocol));
    } else if (MTCONFIG == type) {
      std::cout << "test " << std::endl;
      //设置超时1s
      m_socket->setConnTimeout(sg_agent::MTCONFIG_TIMEOUT);
      m_socket->setSendTimeout(sg_agent::MTCONFIG_TIMEOUT);
      m_socket->setRecvTimeout(sg_agent::MTCONFIG_TIMEOUT);
      m_client = (void *) (new MtConfigServiceClient(m_protocol));
    } else if (MNSCache == type) {
      //设置超时
      m_socket->setConnTimeout(sg_agent::MNSC_TIMEOUT);
      m_socket->setSendTimeout(sg_agent::MNSC_TIMEOUT);
      m_socket->setRecvTimeout(sg_agent::MNSC_TIMEOUT);
      m_client = (void *) (new MNSCacheServiceClient(m_protocol));
    } else {
      m_client = NULL;
    }
  }

  LOG_INFO("connect to ThriftClientHandler ip: " << m_host.c_str()
                                                 << " port is : " << m_port
                                                 << " type is : " << proc_type
                                                 << " m_client : " << m_client
                                                 << ", timeout = " << sg_agent::LOGCOLLECTOR_TIMEOUT);
  srand(time(0));

  return 0;
}

//check连接
int ThriftClientHandler::checkConnection() {
  if (!m_transport->isOpen()) {
    return ERR_CHECK_CONNECTION;
  }
  return 0;
}

//创建连接
int ThriftClientHandler::createConnection() {
  int count = 0;
  bool flag = true;
  //先check连接是否可用
  int ret = checkConnection();
  //如果连接不可用，则重新建立连接
  while ((ret != 0) && count < RETRY) {
    count++;
    try {
      m_transport->open();
      flag = true;
      LOG_INFO("reconnect to logcollector ok! ip: " << m_host.c_str()
                                                    << " port is : " << m_port);
    }
    catch (TException &e) {
      flag = false;
      LOG_WARN("reconnect to logcollector failed ip: " << m_host.c_str()
                                                       << " port is : " << m_port
                                                       << ", error : " << e.what());
    }

    //获取连接状态
    ret = checkConnection();
  }

  if (flag && ret == 0) {
    return 0;
  } else {
    LOG_ERROR("ThriftClientHandler failed! ret = " << ret << "flag = " << flag);
    return ERR_CREATE_CONNECTION;
  }
}

//关闭连接
int ThriftClientHandler::closeConnection() {
  if (!m_closed) {
    //置连接关闭标示为true，防止多次close
    m_closed = true;
    try {
      LOG_WARN("begin close connection !");
      if (likely(NULL != m_transport)) {
        m_transport->close();
      } else {
        LOG_ERROR("m_transport is NULL when to close");
      }
    }
    catch (TException &e) {
      LOG_ERROR("ERR, close connection fail! error : " << e.what());
      return ERR_CLOSE_CONNECTION;
    }
  }
  return 0;
}

//check handler对应的连接是否可用
int ThriftClientHandler::checkHandler() {
  //先check连接
  int ret = checkConnection();
  if (ret != 0) {
    LOG_ERROR("logCollector connection lost, begin close! ret = " << ret);
    //关闭连接
    ret = closeConnection();
    if (ret != 0) {
      LOG_ERROR("logCollector connection lost, close fail! ret = " << ret);
      return ret;
    }
    //重新创建连接
    ret = createConnection();
    if (ret != 0) {
      LOG_WARN("logCollector re-create connection fail! ret = " << ret);
      return ret;
    }
    //创建成功，修改连接标示为连接未被关闭
    m_closed = false;
  }

  return 0;
}

