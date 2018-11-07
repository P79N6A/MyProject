// =====================================================================================
// 
//       Filename:  remote_log_collector.cpp
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-09-22 18时15分11秒
//       Revision:  none
// 
// =====================================================================================

#include "comm/tinyxml2.h"
#include "comm/sgagent_mq.h"

#include "util/sgagent_filter.h"
#include "remote_log_collector.h"

#include "mns/mns_iface.h"
#include "util/global_def.h"
#include "mns.h"

using namespace tinyxml2;
using namespace sg_agent;

extern GlobalVar *g_global_var;
extern MNS *g_mns;

RemoteLogCollector::RemoteLogCollector()
    : m_appkey(g_global_var->gLogCollectorAppkey),
      m_lastCheckTime(0) {
  m_collectorHandlerList.clear();
}

RemoteLogCollector::~RemoteLogCollector() {
}

int RemoteLogCollector::sendCommonLogs(const CommonLog &oCommonLog) {
  //获取整个collectorHandler结构体，便于异常从map中删除连接
  CollectorHandlerInfo *pCollectorHandler = _getOneCollector();
  if (!pCollectorHandler) {
    LOG_WARN("ERR send_uploadCommonLog failed, get CollectorHandlerInfo fail");
    return ERR_GET_HANDLER_INFO_FAIL;
  }

  ThriftClientHandler *pCollector = pCollectorHandler->m_pHandler;
  if (!pCollector) {
    LOG_ERROR("ERR send_uploadCommonLog failed, getOneCollector fail");
    return ERR_GET_HANDLER_FAIL;
  }

  //check连接，失败重连
  int ret = pCollector->checkHandler();
  if (ret != 0) {
    std::string ip = pCollectorHandler->m_host;
    int port = pCollectorHandler->m_port;
    //如果连接不可用，重连仍不可用，则从cache中删除连接
    deleteCollector(ip, port);
    LOG_ERROR("check logcollector handler fail, host : " << ip
                                                         << ", port : " << port);
    return ret;
  }

  try {
    //使用时强制类型转换成对应的对象
    AggregatorServiceClient *ptr = static_cast<AggregatorServiceClient *>(pCollector->getClient());
    if (!ptr) {
      LOG_ERROR("static_cast Aggrefailed!");
      return ERR_GET_HANDLER_FAIL;
    }
    ptr->uploadCommonLog(oCommonLog);
  }
  catch (TException &e) {
    pCollector->closeConnection();

    std::string ip = pCollectorHandler->m_host;
    int port = pCollectorHandler->m_port;
    //从cache中删除连接
    deleteCollector(ip, port);   //DO NOT use pCollectorHandler ->xxx
    // directly since will destroy the handler itself inside deleteCollector
    LOG_ERROR("ERR send_commonlogs failed. " << e.what()
                                             << ", host : " << ip
                                             << ", port : " << port);
    return ERR_SEND_COMMONLOGS_FAIL;
  }

  return 0;
}

/*
 * 第一次发送，使用cache; 失败后，将不再用cache，以防logCollector重启
 */
int RemoteLogCollector::sendModuleInvokeInfo(const SGModuleInvokeInfo &oInfo) {
  //获取整个collectorHandler结构体，便于异常从map中删除连接
  CollectorHandlerInfo *pCollectorHandler = _getOneCollector();
  if (!pCollectorHandler) {
    LOG_ERROR("ERR send_uploadModuleInvoke failed, get CollectorHandlerInfo fail");
    return ERR_GET_HANDLER_INFO_FAIL;
  }

  ThriftClientHandler *pCollector = pCollectorHandler->m_pHandler;
  if (!pCollector) {
    LOG_ERROR("ERR send_uploadModuleInvoke failed, getOneCollector fail!");
    return ERR_GET_HANDLER_FAIL;
  }

  //check连接，失败重连
  int ret = pCollector->checkHandler();
  if (ret != 0) {
    LOG_ERROR("check logcollector handler fail, host : " << pCollectorHandler->m_host
                                                         << ", port : " << pCollectorHandler->m_port);
    //如果连接不可用，重连仍不可用，则从cache中删除连接
    deleteCollector(pCollectorHandler->m_host, pCollectorHandler->m_port);
    return ret;
  }

  try {
    //使用时强制类型转换成对应的对象
    AggregatorServiceClient *ptr = static_cast<AggregatorServiceClient *>(pCollector->getClient());
    if (!ptr) {
      LOG_ERROR("static_cast Aggrefailed!");
      return ERR_GET_HANDLER_FAIL;
    }
    ptr->uploadModuleInvoke(oInfo);
  }
  catch (TException &e) {
    LOG_WARN("ERR send_uploadModuleInvoke failed: " << e.what());
    pCollector->closeConnection();
    //从cache中删除连接
    deleteCollector(pCollectorHandler->m_host, pCollectorHandler->m_port);
    return ERR_SEND_MODULE_FAIL;
  }

  return 0;
}

//获取服务列表,放入m_serviceList中
int RemoteLogCollector::_getServiceList() {
  //If 30s, do not need to check
  int cur_time = time(0);
  if (cur_time < (m_lastCheckTime + 30)
      && (m_collectorHandlerList.size() != 0)) {
    LOG_DEBUG("do not need to check new logCollector serviceList");
    return 0;
  }
  //更新时间
  m_lastCheckTime = cur_time;

  std::vector<SGService> serviceList;
  int ret = getServiceListFromMNSC(serviceList);
  if (0 != ret) {
    LOG_ERROR("ERR get remote_log_collector servicelist from MNS fail.\
                ret = " << ret);
  }

  // 更新serviceList
  std::vector<SGService> vec_sgservice_add;
  std::vector<SGService> vec_sgservice_del;
  std::vector<SGService> vec_sgservice_chg;
  sgserviceMgr.UpdateSvrList(serviceList, vec_sgservice_add,
                             vec_sgservice_del, vec_sgservice_chg);

  ////step2: 过滤不可用的机器节点,并对新加入的机器创建连接(只是对原来逻辑单独提取一个函数，还是需要优化)
  _addHandler(vec_sgservice_add);
  _deleteHandler(vec_sgservice_del);
  // 检查每个结果
  _changeHandler(serviceList);
  LOG_DEBUG("m_collectorHandlerList.size() = " << m_collectorHandlerList.size());

  if (0 == m_collectorHandlerList.size()) {
    LOG_ERROR("logCollector _getServiceList after filter fail");
    ret = ERR_SERVICELIST_FAIL;
  }

  return ret;
}

int RemoteLogCollector::getServiceListFromMNSC(std::vector<SGService> &serviceList) {
  LOG_DEBUG("need to check new logCollector serviceList");

  //从MNS获取服务列表
  ProtocolRequest request;
  request.__set_remoteAppkey(m_appkey);
  request.__set_localAppkey("sg_agent_inside_request");
  request.__set_protocol("thrift");

  //TODO:
  int ret = g_mns->GetMnsPlugin()->GetSrvList(serviceList, request, false, true, true);
  if (0 != ret || serviceList.empty()) {
    LOG_ERROR("ERR remote_log_collector service list return fail. appkey = " << m_appkey);
    return ERR_SERVICELIST_NULL;
  }

  sg_agent::SGAgent_filter::filterUnAlive(serviceList);
  ret = sg_agent::SGAgent_filter::FilterWeight(serviceList, sg_agent::IdcThresHold);
  if (0 != ret) {
    LOG_DEBUG("result from IDC filte is empty");
    ret = sg_agent::SGAgent_filter::FilterWeight(serviceList, sg_agent::RegionThresHold);
    if (0 != ret) {
      LOG_WARN("result from Region filte is empty");
    }
  }

  return 0;
}

void RemoteLogCollector::_addHandler(const std::vector<SGService> &vec_sgservice_add) {
  LOG_DEBUG("add handl size = " << vec_sgservice_add.size());
  for (std::vector<SGService>::const_iterator serviceIter = vec_sgservice_add.begin();
       serviceIter != vec_sgservice_add.end(); ++serviceIter) {
    CollectorHandlerInfo *pCollectorHandlerInfo = new CollectorHandlerInfo;
    if (NULL == pCollectorHandlerInfo) {
      LOG_ERROR("new CollectorHandlerInfo fail! ip is : "
                    << serviceIter->ip.c_str()
                    << " envir is : " << serviceIter->envir
                    << " weight is : " << serviceIter->weight
                    << " port is : " << serviceIter->port);
      continue;
    }

    ThriftClientHandler *pCollector = new ThriftClientHandler();
    if (NULL == pCollector) {
      LOG_ERROR("new ThriftClientHandler fail! ip is : "
                    << serviceIter->ip.c_str()
                    << " envir is : " << serviceIter->envir
                    << " weight is : " << serviceIter->weight
                    << " port is : " << serviceIter->port);
      SAFE_DELETE(pCollectorHandlerInfo);
      continue;
    } else {
      LOG_INFO("RUN create ThriftClientHandler ip is : "
                   << serviceIter->ip.c_str()
                   << " port is : " << serviceIter->port);
    }

    int ret = pCollector->init(serviceIter->ip, serviceIter->port, COMMONLOG);
    if (0 == ret) //创建成功
    {
      pCollectorHandlerInfo->m_pHandler = pCollector;
      pCollectorHandlerInfo->m_host = serviceIter->ip;
      pCollectorHandlerInfo->m_port = serviceIter->port;
      pCollectorHandlerInfo->m_isavailable = true;
      m_collectorHandlerList.push_back(pCollectorHandlerInfo);
    } else {
      LOG_ERROR("init ip fail: " << serviceIter->ip.c_str()
                                 << " envir is : " << serviceIter->envir
                                 << " weight is : " << serviceIter->weight
                                 << " port is : " << serviceIter->port);
      SAFE_DELETE(pCollector);
      SAFE_DELETE(pCollectorHandlerInfo);
    }
  }
}

void RemoteLogCollector::_deleteHandler(const std::vector<SGService> &vec_sgservice_delete) {
  LOG_DEBUG("delete handl size = " << vec_sgservice_delete.size());
  for (std::vector<SGService>::const_iterator serviceIter = vec_sgservice_delete.begin();
       serviceIter != vec_sgservice_delete.end(); ++serviceIter) {
    std::vector<CollectorHandlerInfo *>::iterator handlerIter
        = m_collectorHandlerList.begin();
    bool isFound = false;
    while (handlerIter != m_collectorHandlerList.end()) {
      if ((*handlerIter)->m_host == serviceIter->ip
          && (*handlerIter)->m_port == serviceIter->port) {
        LOG_WARN("delete this node from m_collectorHandlerList, ip : "
                     << serviceIter->ip.c_str()
                     << " port is : " << serviceIter->port
                     << " envir is : " << serviceIter->envir
                     << " weight is : " << serviceIter->weight);
        (*handlerIter)->m_isavailable = false;

        ThriftClientHandler *pHandler = (*handlerIter)->m_pHandler;
        if (likely(NULL != pHandler)) {
          pHandler->closeConnection();
        } else {
          LOG_ERROR("pHandler is NULL when close Connection,\
                            ip = " << serviceIter->ip
                                   << ", port = " << serviceIter->port);
        }

        //从cache中删除连接
        deleteCollector((*handlerIter)->m_host, (*handlerIter)->m_port);

        isFound = true;
        break;
      }
      ++handlerIter;
    }
    if (unlikely(!isFound)) {
      LOG_ERROR("delete node not find int m_collectorHandlerList, ip: "
                    << serviceIter->ip.c_str()
                    << " port is : " << serviceIter->port
                    << " envir is : " << serviceIter->envir
                    << " weight is : " << serviceIter->weight);
    }
  }
  LOG_DEBUG("m_collectorHandlerList's size = " <<
                                               m_collectorHandlerList.size());
}

void RemoteLogCollector::_changeHandler(const std::vector<SGService> &vec_sgservice_change) {
  LOG_DEBUG("res handler size = " << vec_sgservice_change.size()
                                  << "; m_collectorHandlerList.size() = " << m_collectorHandlerList.size());
  // 记录中途因为服务不通已经删除的节点， 此处新增
  std::vector<SGService> vec_sgservice_add;

  for (std::vector<SGService>::const_iterator serviceIter = vec_sgservice_change.begin();
       serviceIter != vec_sgservice_change.end(); ++serviceIter) {
    std::vector<CollectorHandlerInfo *>::iterator handlerIter
        = m_collectorHandlerList.begin();
    bool isFound = false;
    while (handlerIter != m_collectorHandlerList.end()) {
      if (0 == (*handlerIter)->m_host.compare(serviceIter->ip)
          && (*handlerIter)->m_port == serviceIter->port) {
        (*handlerIter)->m_isavailable = true;
        if (!(*handlerIter)->m_pHandler->m_transport->isOpen()) {
          LOG_ERROR("ThriftClientHandler open failed! reopen ip : "
                        << serviceIter->ip.c_str()
                        << " port is : " << serviceIter->port
                        << " envir is : " << serviceIter->envir
                        << " weight is : " << serviceIter->weight);
          (*handlerIter)->m_pHandler->m_transport->open();
        }
        isFound = true;
        break;
      }
      ++handlerIter;
    }
    if (isFound) {
      continue;
    }
    LOG_ERROR("change node not find int m_collectorHandlerList, ip: "
                  << serviceIter->ip.c_str()
                  << " port is : " << serviceIter->port
                  << " envir is : " << serviceIter->envir
                  << " weight is : " << serviceIter->weight);
    vec_sgservice_add.push_back(*serviceIter);
  }
  _addHandler(vec_sgservice_add);
}

CollectorHandlerInfo *RemoteLogCollector::_getOneCollector() {
  //获取logCollector服务列表，通过agent接口进行获取，支持服务分组，同机房调用等逻辑
  int ret = _getServiceList();
  if (ret != 0) {
    LOG_ERROR("ERR getlogCollectorList fail! ret = " << ret);
    return NULL;
  }

  //added by tuyang
  int handlerListSize = m_collectorHandlerList.size();
  if (handlerListSize <= 0) {
    LOG_ERROR("ERR getlogCollectorList fail! size = " << handlerListSize);
    return NULL;
  }
  LOG_DEBUG("collector handlerListSize = " << handlerListSize);

  int beginIndex = rand() % handlerListSize;
  int index = beginIndex;
  do {
    LOG_DEBUG("try handler index = " << index);
    ThriftClientHandler *pHandler = (m_collectorHandlerList[index])->m_pHandler;
    bool isavailable = (m_collectorHandlerList[index])->m_isavailable;
    if (NULL != pHandler
        && isavailable
        && NULL != pHandler->m_transport
        && pHandler->m_transport->isOpen()) {
      LOG_DEBUG("get handler index = " << index
                                       << ", ip = " << pHandler->m_host
                                       << ", port = " << pHandler->m_port);
      return m_collectorHandlerList[index];
    } else if (NULL == pHandler) {
      LOG_ERROR("The ip: " << (m_collectorHandlerList[index])->m_host
                           << ", port: " << (m_collectorHandlerList[index])->m_port
                           << " pHandler is NULL");
      index = (index + 1) % handlerListSize;
    } else if (NULL == pHandler->m_transport) {
      LOG_ERROR("The ip: " << (m_collectorHandlerList[index])->m_host
                           << ", port: " << (m_collectorHandlerList[index])->m_port
                           << " pHandler -> m_transport is NULL");
      index = (index + 1) % handlerListSize;
    } else {
      LOG_WARN("The ip: " << (m_collectorHandlerList[index])->m_host
                          << ", port: " << (m_collectorHandlerList[index])->m_port << "is unavailable");
      index = (index + 1) % handlerListSize;
    }
  } while (index != beginIndex);
  LOG_ERROR("unable to get handler from handlerList");

  return NULL;
}

int RemoteLogCollector::deleteCollector(const std::string &host, int port) {
  //从m_collectorHandlerList中删除不可用节点
  std::vector<CollectorHandlerInfo *>::iterator iter
      = RemoteLogCollector::m_collectorHandlerList.begin();
  bool isFound = false;
  while (iter != RemoteLogCollector::m_collectorHandlerList.end()) {
    if ((*iter)->m_host == host && (*iter)->m_port == port) {
      LOG_DEBUG("begin to delete unavailable ip: " << host.c_str()
                                                   << " port is : " << port);
      try {
        CollectorHandlerInfo *handler = *iter;
        ThriftClientHandler *remoterLogHandler = handler->m_pHandler;
        SAFE_DELETE(remoterLogHandler);
        iter = RemoteLogCollector::m_collectorHandlerList.erase(iter);
        LOG_WARN("delete unavailable node from m_collectorHandlerList,\
                        ip: " << host.c_str()
                              << " port is : " << port);
        SAFE_DELETE(handler);
        sgserviceMgr.delOneSvr(host, port);
        LOG_WARN("delete unavailable node from sgserviceMgr,\
                        ip: " << host.c_str()
                              << " port is : " << port);
        isFound = true;
      }
      catch (std::exception &e) {
        LOG_ERROR(e.what());
      }
      break;
    }
    ++iter;
  }
  if (!isFound) {
    LOG_ERROR("failed to delete unavailable node, not find ip: "
                  << host.c_str()
                  << " port is : " << port);
  }
  return 0;
}
