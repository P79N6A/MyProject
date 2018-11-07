// =====================================================================================
// 
//       Filename:  mnscache_client.cpp
// 
//    Description:  
// 
//        Version:  1.0
//       Revision:  none
// 
// =====================================================================================

#include "comm/tinyxml2.h"
#include "comm/log4cplus.h"
#include "mnscache_client.h"
#include "util/sgagent_filter.h"
#include "util/SGAgentErr.h"
#include "util/sg_agent_def.h"
#include "sgcommon/common_interface.h"
#include "mns/mns_iface.h"
#include "util/global_def.h"
#include "mns.h"
#include "sgcommon_invoker.h"

using namespace tinyxml2;
using namespace sg_agent;

extern GlobalVar *g_global_var;
extern MNS *g_mns;

static const int RETRY = 3;
//请求mns_cache失败后，sleep时间, 单位us
const int MNSC_DEFAULT_SLEEPTIME = 30000;
//请求mns_cache超时设置：600ms, 单位us
const int m_cache_timeout = 600000;

pthread_mutex_t MnsCacheCollector::m_CMutex = PTHREAD_MUTEX_INITIALIZER;
MnsCacheCollector *MnsCacheCollector::mnsCacheCollector = NULL;

MnsCacheCollector::MnsCacheCollector() {
  XMLDocument conf;
  tinyxml2::XMLError eResult = conf.LoadFile(SG_AGENT_MUTABLE_CONF.c_str());
  const char *remoteAppkey;
  if(tinyxml2::XML_SUCCESS == eResult){
    XMLElement* findChildElement = conf.FirstChildElement("SGAgentMutableConf");
    if(NULL != findChildElement){
      remoteAppkey = findChildElement->FirstChildElement("MNSCacheAppkey")->GetText();
    }
    m_appkey = remoteAppkey;
    m_lastCheckTime = 0;
    m_serviceList.clear();
  }
}

/*
 * 从mnscache获取服务列表信息 
 */
int MnsCacheCollector::getMNSCache(
    std::vector<SGService> &serviceList,
    const std::string &appkey,
    const std::string &version,
    const std::string &env,
    const std::string &protocol) {
  ThriftClientHandler *pCollector = _getOneCollector();
  if (!pCollector) {
    LOG_ERROR("ERR getMNSCache's getOneCollector failed!");
    return ERR_GET_HANDLER_FAIL;
  }

  //定义获取mnscache返回结构体
  MNSResponse mnscacheRes;
  try {
    //使用时强制类型转换成对应的对象
    MNSCacheServiceClient *ptr = static_cast<MNSCacheServiceClient *>(pCollector->getClient());
    if (!ptr) {
      LOG_ERROR("static_cast MNSCacheServiceClient failed!");
      SAFE_DELETE(pCollector);
      return ERR_GET_HANDLER_FAIL;
    }

    //调用mnscache接口
    if ("thrift" == protocol) {
      ptr->getMNSCache(mnscacheRes, appkey, version, env);
    } else if ("http" == protocol) {
      ptr->getMNSCache4HLB(
          mnscacheRes, appkey, version, env);
    } else {
      LOG_ERROR("invalid protocol: " << protocol);
    }
    //提取返回的服务列表
    if (sg_agent::MNSC_OK == mnscacheRes.code) {
      serviceList = mnscacheRes.defaultMNSCache;
      LOG_DEBUG("getMNSCache ok! appkey : " << appkey
                                            << ", serviceList size is : " << serviceList.size()
                                            << ", env is : " << env);
    } else {
      LOG_ERROR("getMNSCache wrong errcode. appkey : " << appkey
                                                       << ", env is : " << env
                                                       << ", errCode : " << mnscacheRes.code);
    }
  }
  catch (TException &e) {
    //异常关闭连接
    pCollector->closeConnection();
    //释放内存
    SAFE_DELETE(pCollector);
    LOG_ERROR("getMNSCache catch error! msg: " << e.what()
                                               << ", serviceList size is : " << serviceList.size()
                                               << ", env is : " << env
                                               << ", appkey : " << appkey);
    CountRequest::GetInstance()->CountMnscReq(false);
    return ERR_MNSC_GET_MNSCACHE;
  }
  CountRequest::GetInstance()->CountMnscReq(true);
  //使用完成，关闭连接
  pCollector->closeConnection();
  //释放内存
  SAFE_DELETE(pCollector);

  //返回错误码code
  return mnscacheRes.code;
}

int MnsCacheCollector::getServiceList(
    std::vector<SGService> &serviceList,
    const int &providerSize,
    const std::string &appKey,
    const std::string &version,
    const std::string &env,
    const std::string &protocol) {
  timeval tvalStart;
  timeval tvalEnd;
  long deltaTime;
  int count = 0;

  gettimeofday(&tvalStart, NULL);
  int ret_code = 0;
  do {
    //访问MNS_Cache
    ret_code = getMNSCache(serviceList,
                           appKey, version, g_global_var->gEnvStr, protocol);
    if (sg_agent::MNSC_OK == ret_code) {
      //getCache正常返回, 且获取到的节点数与sg_agent获取的一致，则返回
      if (serviceList.size() == providerSize) {
        break;
      } else {
        LOG_ERROR("getMNSCache zk_node size not the same, serveceList size : "
                      << serviceList.size()
                      << " , sg_agent getZK provider size  : "
                      << providerSize
                      << ", appkey : " << appKey
                      << ", version : " << version);
        gettimeofday(&tvalEnd, NULL);
        deltaTime = (tvalEnd.tv_sec - tvalStart.tv_sec)
            * 1000000L
            + (tvalEnd.tv_usec - tvalStart.tv_usec);
        usleep(MNSC_DEFAULT_SLEEPTIME);
      }
    } else if (sg_agent::MNSC_UPDATING == ret_code) {
      //getCache返回500, 表示MNSCache正在watcher更新, sleep重试一次
      gettimeofday(&tvalEnd, NULL);
      deltaTime = (tvalEnd.tv_sec - tvalStart.tv_sec)
          * 1000000L
          + (tvalEnd.tv_usec - tvalStart.tv_usec);
      usleep(MNSC_DEFAULT_SLEEPTIME);
    } else {
      //其他错误，表示MNSCache异常，重试3次，
      //若都失败则退出循环，直连ZK再次getServerList
      ++count;
      if (count < sg_agent::MNSC_RETRY_TIME) {
        LOG_WARN("getMNSCache fail , ret code : "
                     << ret_code
                     << ", appkey : " << appKey
                     << ", need to retry, count : " << count);
        continue;
      } else {
        break;
      }
    }
  } while ((long) m_cache_timeout > deltaTime);
  return ret_code;
}

//获取服务列表,放入m_serviceList中，
int MnsCacheCollector::_getServiceList() {
  //如果缓存数据非空，则每隔10秒更新一次缓存数据; 若缓存为空，则立即获取列表
  int cur_time = time(0);
  if ((cur_time < (m_lastCheckTime + 10)) && (m_serviceList.size() != 0)) {
    return 0;
  }

  //更新时间
  m_lastCheckTime = cur_time;

  ProtocolRequest req;
  req.__set_remoteAppkey(m_appkey);
  req.__set_localAppkey("sgAgent");
  req.__set_protocol("thrift");

  std::vector<SGService> serviceList;
  //TODO:
  int ret = g_mns->GetMnsPlugin()->GetSrvList(serviceList, req, false, true, true);
  if (ret != 0) {
    LOG_ERROR("get mnscache serviceList failed! appkey: " << m_appkey
                                                          << ", ret = " << ret);
    return ret;
  }

  if (serviceList.size() == 0) {
    LOG_ERROR("ERR mnscache service list return null. appkey = " << m_appkey);
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

  //更新服务列表
  m_serviceList = serviceList;
  return 0;
}
int MnsCacheCollector::registerService(RegisterResponse &res, const SGService &req) {

  if ("thrift" != req.protocol) {
    // only check thrift protocol
    res.__set_code(sg_agent::MNSC_OK);
    res.__set_allowRegister(true);
    res.__set_msg("allow to register to mns");
    return res.code;
  }
  ThriftClientHandler *pCollector = _getOneCollector();
  if (!pCollector) {
    LOG_ERROR("ERR registerService's getOneCollector failed!");
    return ERR_GET_HANDLER_FAIL;
  }

  try {
    //使用时强制类型转换成对应的对象
    MNSCacheServiceClient *ptr = static_cast<MNSCacheServiceClient *>(pCollector->getClient());
    if (!ptr) {
      LOG_ERROR("static_cast MNSCacheServiceClient failed!");
      return ERR_GET_HANDLER_FAIL;
    }
    //调用mnscache接口
    ptr->registerService(res, req);
    //提取返回的结果判断
    if (sg_agent::MNSC_OK == res.code) {
      LOG_DEBUG("allow register.appkey : " << req.appkey
                                           << ", env is : " << req.envir);
    } else {
      LOG_ERROR("register failured. appkey : " << res.code
                                               << ", env is : " << req.envir
                                               << ", errCode : " << res.code
                                               << ", reason : " << res.msg);
    }

  } catch (TException &e) {
    //异常关闭连接
    pCollector->closeConnection();
    //释放内存
    SAFE_DELETE(pCollector);
    LOG_ERROR("registerMNSCache catch error! msg: " << e.what()
                                                    << ", env is : " << req.envir
                                                    << ", appkey : " << req.appkey);
    CountRequest::GetInstance()->CountMnscReq(false);
    return ERR_MNSC_GET_MNSCACHE;
  }
  CountRequest::GetInstance()->CountMnscReq(true);
  // close the mnsc connection after using.
  pCollector->closeConnection();

  // delete the handler of mnsc connection.
  SAFE_DELETE(pCollector);

  // return the errorcode from mnsc.
  return res.code;
}
MnsCacheCollector *MnsCacheCollector::getInstance() {
  if (NULL == mnsCacheCollector) {

    pthread_mutex_lock(&m_CMutex);
    if (NULL == mnsCacheCollector) {
        mnsCacheCollector = new MnsCacheCollector();
    }
    pthread_mutex_unlock(&m_CMutex);
  }
  return mnsCacheCollector;
}

void MnsCacheCollector::Destroy() {
  SAFE_DELETE(mnsCacheCollector);
}

ThriftClientHandler *MnsCacheCollector::_getOneCollector() {
  //获取服务列表
  int ret = _getServiceList();
  if (ret != 0) {
    LOG_ERROR("get mnscache, getServiceList fail! ret = " << ret);
    return NULL;
  }

  //若获取的为空，直接返回
  int handlerListSize = m_serviceList.size();
  if (handlerListSize <= 0) {
    LOG_ERROR("getServiceList size <= 0 !");
    return NULL;
  }
  //随机选择一个server,创建连接
  int beginIndex = rand() % handlerListSize;
  int index = beginIndex;
  do {
    if (m_serviceList[index].status == 2) {
      ThriftClientHandler *pCollector = new ThriftClientHandler();
      if (pCollector) {
        int ret = pCollector->init(m_serviceList[index].ip, m_serviceList[index].port, MNSCache);
        if ((ret == 0) && pCollector->m_transport->isOpen()) {
          return pCollector;
        } else {
          LOG_ERROR("MNSC handler init failed! index = " << index
                                                         << ", ip = " << m_serviceList[index].ip
                                                         << ", port = " << m_serviceList[index].port
                                                         << ", ret = " << ret);

          SAFE_DELETE(pCollector);
          index = (index + 1) % handlerListSize;
        }
      }
    } else {
      //如果本次server不可用，则尝试连接下一个server
      index = (index + 1) % handlerListSize;
    }
  } while (index != beginIndex);

  return NULL;
}

