// =====================================================================================
//
//      Filename:  registclient.cpp
//
//      Description: regist service to zookeeper
//
//      Version:  1.0
//      Created:  2015/04/15
//      Revision:  none
//
// =====================================================================================
#include <string.h>
#include <dlfcn.h>
#include <util/global_def.h>
#include "regist_client.h"
#include "util/falcon_mgr.h"
#include "sgcommon_invoker.h"
#include "util/global_def.h"
using namespace muduo::net;

extern GlobalVar *g_global_var;

RegistClient *RegistClient::m_registclient = NULL;
muduo::MutexLock RegistClient::s_cmutex;

namespace sg_agent {

static const size_t kMaxPendingTasks = 100;


RegistClient::RegistClient()
    : loop_(NULL),
      loop_thread_(NULL),
      statusMgr(NULL),
      m_retry(DEFAULT_REGIST_RETRY) {
}

RegistClient::~RegistClient() {
  EventLoopThreadProxyDestroyer(loop_thread_);
}

int RegistClient::init(int retry) {

  m_retry = retry;
  if (NULL == statusMgr) {
    statusMgr = new StatusMgr();
  }
  loop_thread_ = EventLoopThreadProxyMaker();
  if (NULL == loop_thread_) {
    LOG_ERROR("fail to init loop_thread_");
    return FAILURE;
  }

  // 启动worker处理线程
  loop_ = loop_thread_->startLoop();
  return 0;
}

bool RegistClient::IsHotelAllowRegister(const SGService &service) {
  if (PPE == g_global_var->gAppenv || TEST == g_global_var->gAppenv) {
    // only check ppe and test
		muduo::MutexLockGuard lock(s_cmutex);
		std::set<std::string>::const_iterator appkey_iter = m_hotel_white_appkeys.find(service.appkey);
    if (m_hotel_white_appkeys.end() != appkey_iter) {
      bool is_allow = false;
      // only check the hotel appkeys
      for (std::vector<boost::shared_ptr<IDC> >::const_iterator iter = m_hotel_white_idcs.begin();
           m_hotel_white_idcs.end() != iter; ++iter) {
        if ((*iter)->IsSameIdc(service.ip)) {
          // match
          is_allow = true;
          break;
        }
      }
      return is_allow;
    }
  }
  return true;
}

void RegistClient::ResetHoteltravelWhiteList(const std::set<std::string> &appkeys,
                                             const std::vector<boost::shared_ptr<IDC> > &idcs) {
  muduo::MutexLockGuard lock(s_cmutex);
  if (appkeys.empty()) {
    LOG_WARN("appkeys is empty , can not insert");
  } else {
    m_hotel_white_appkeys = appkeys;
  }
  if (idcs.empty()) {
    LOG_WARN("appkeys is empty, can not insert");
  } else {
    m_hotel_white_idcs = idcs;
  }
}

void RegistClient::SetHotelRegisterWhiteList(const std::set<std::string> &appkeys,
                                             const std::vector<boost::shared_ptr<IDC> > &idcs) {
  muduo::MutexLockGuard lock(s_cmutex);
  m_hotel_white_appkeys = appkeys ;
  m_hotel_white_idcs =  idcs;
}

int RegistClient::registeService(const SGService &oservice, bool isNewCloud, int32_t uptCmd) {
  int ret = _checkArgs(oservice);
  if (SUCCESS != ret) {
    return ret;
  }

  if (!IsHotelAllowRegister(oservice)) {
    // not allow to register
    LOG_INFO("hotel appkey and the ip is not in the special idc, not allow to register service to ZK , appkey = "
                 << oservice.appkey
                 << "; version = " << oservice.version
                 << "; ip = " << oservice.ip
                 << "; port = " << oservice.port
                 << "; weight = " << oservice.weight
                 << "; status = " << oservice.status
                 << "; role = " << oservice.role
                 << "; envir = " << oservice.envir
                 << "; fweight = " << oservice.fweight
                 << "; lastUpdateTime = " << oservice.lastUpdateTime
                 << "; protocol = " << oservice.protocol
                 << "; swimlane = " << oservice.swimlane
                 << "; serverType = " << oservice.serverType
                 << "; uptCmd = " << uptCmd
                 << "; extend = " << oservice.extend);
    return SUCCESS;
  }

  LOG_INFO("register service to ZK , appkey = "
               << oservice.appkey
               << "; version = " << oservice.version
               << "; ip = " << oservice.ip
               << "; port = " << oservice.port
               << "; weight = " << oservice.weight
               << "; status = " << oservice.status
               << "; role = " << oservice.role
               << "; envir = " << oservice.envir
               << "; fweight = " << oservice.fweight
               << "; lastUpdateTime = " << oservice.lastUpdateTime
               << "; protocol = " << oservice.protocol
               << "; swimlane = " << oservice.swimlane
               << "; cell = " << oservice.cell
               << "; serverType = " << oservice.serverType
               << "; uptCmd = " << uptCmd
               << "; extend = " << oservice.extend);

  for (std::map<std::string, ServiceDetail>::const_iterator iter
      = oservice.serviceInfo.begin();
       iter != oservice.serviceInfo.end(); ++iter) {
    LOG_INFO("service: " << iter->first
                         << "; unifiedProto: "
                         << iter->second.unifiedProto);
  }
 //注册env时以本地sg_agent为准
  SGService service_env(oservice);
  service_env.__set_envir(g_global_var->gEnv);

  RegisterResponse res;
  //use sg_agent::MNSC_OK to init RegisterResponse, in order to be compatible with the existing register logic.
  res.__set_code(sg_agent::MNSC_OK);
  res.__set_allowRegister(true);
  MnsCacheCollector::getInstance()->registerService(res, service_env);

  LOG_INFO("mnsc register callback msg,res code: " << res.code << ", allow register flag " << res.allowRegister
                                                   << ", res msg:" << res.msg);
  if (sg_agent::MNSC_OK == res.code && !res.allowRegister) {
    LOG_INFO("not allowed register, appkey:"
                 << oservice.appkey
                 << ", protocol" << oservice.protocol
                 << ", ip" << oservice.ip
                 << ", port" << oservice.port);
    return FAILURE;
  }
  SGServicePtr servicePtr(new SGService(oservice));

  if (isNewCloud) {
    char ip[INET_ADDRSTRLEN];
    char mask[INET_ADDRSTRLEN];
    ret = getIntranet(ip, mask);
    if (0 != ret) {
      LOG_ERROR("failed to get IP by getIntranet, don't change service's ip");
    } else {
      LOG_INFO("swap reg service ip from " << servicePtr->ip << " to " << ip);
    }

    servicePtr->ip = ip;
  }

  ret = _unifiedProtocol(*servicePtr);
  if (SUCCESS != ret) {
    // already log inside.
    return ret;
  }

  RegParamPtr regParamPtr(new regist_req_param_t);
  regParamPtr->sgservice = *servicePtr;
  regParamPtr->uptCmd = uptCmd;
  regParamPtr->regCmd = RegistCmd::REGIST;

  size_t pending_tasks_size = loop_->queueSize();
  FalconMgr::SetRegisteQueueSize(pending_tasks_size);

  boost::shared_ptr<RegistHandler> task(new RegistHandler(this, regParamPtr));
  if (pending_tasks_size < kMaxPendingTasks) {
    loop_->runInLoop(task);
  } else {
    LOG_WARN("registe backend thread overload, task queue size: " << pending_tasks_size);
  }
  return ret;

}

int RegistClient::unRegisteService(const SGService &oservice, bool isNewCloud) {
  int ret = _checkArgs(oservice);
  if (0 != ret) {
    return ret;
  }
  if (!IsHotelAllowRegister(oservice)) {
    // not allow to unregister
    LOG_INFO("hotel appkey and the ip is not in the special idc, not allow to unregister service to ZK , appkey = "
                 << "; version = " << oservice.version
                 << "; ip = " << oservice.ip
                 << "; port = " << oservice.port
                 << "; weight = " << oservice.weight
                 << "; status = " << oservice.status
                 << "; role = " << oservice.role
                 << "; envir = " << oservice.envir
                 << "; fweight = " << oservice.fweight
                 << "; lastUpdateTime = " << oservice.lastUpdateTime
                 << "; serverType = " << oservice.serverType);
    return SUCCESS;
  }

  LOG_INFO("unRegister service to ZK , appkey = " << oservice.appkey
                                                  << "; version = " << oservice.version
                                                  << "; ip = " << oservice.ip
                                                  << "; port = " << oservice.port
                                                  << "; weight = " << oservice.weight
                                                  << "; status = " << oservice.status
                                                  << "; role = " << oservice.role
                                                  << "; envir = " << oservice.envir
                                                  << "; fweight = " << oservice.fweight
                                                  << "; lastUpdateTime = " << oservice.lastUpdateTime
                                                  << "; serverType = " << oservice.serverType);

  SGServicePtr servicePtr(new SGService(oservice));
  if (isNewCloud) {
    char ip[INET_ADDRSTRLEN];
    char mask[INET_ADDRSTRLEN];
    ret = getIntranet(ip, mask);
    if (0 != ret) {
      LOG_ERROR("failed to get IP by getIntranet, \
                don't change service's ip");
    } else {
      LOG_INFO("swap reg service ip from "
                   << servicePtr->ip
                   << " to " << ip);
    }

    servicePtr->ip = ip;
  }

  ret = _unifiedProtocol(*servicePtr);
  if (0 != ret) {
    return ret;
  }

  // the default status of unRegister is DEAD.
  servicePtr->status = fb_status::DEAD;

  RegParamPtr regParamPtr(new regist_req_param_t);
  regParamPtr->sgservice = *servicePtr;
  regParamPtr->regCmd = RegistCmd::UNREGIST;

  size_t pending_tasks_size = loop_->queueSize();
  FalconMgr::SetRegisteQueueSize(pending_tasks_size);

  boost::shared_ptr<UnRegistHandler> task(new UnRegistHandler(this, regParamPtr));
  if (pending_tasks_size < kMaxPendingTasks) {
    loop_->runInLoop(task);
  } else {
    LOG_WARN("registe backend thread overload, task queue size: "
                 << pending_tasks_size);
  }
  return ret;
}

void RegistClient::setRetry(int retry) {
  m_retry = retry;
}

RegistClient *RegistClient::getInstance() {
  if (NULL == m_registclient) {
    muduo::MutexLockGuard lock(s_cmutex);
    if (NULL == m_registclient) {
      m_registclient = new RegistClient();
    }
  }
  return m_registclient;
}

void RegistClient::Destroy() {
  SAFE_DELETE(m_registclient);
}

int RegistClient::_unifiedProtocol(SGService &service) {
  if (service.protocol.empty()) {
    if (THRIFT_TYPE == service.serverType) {
      service.protocol = "thrift";
    } else if (HTTP_TYPE == service.serverType) {
      service.protocol = "http";
    } else {
      LOG_ERROR("Appkey: " << service.appkey
                           << " serverType: " << service.serverType
                           << " is wrong!");
      return FAILURE;
    }
    LOG_WARN("the original protocol defined by user is empty, now identify the protocol="
                 << service.protocol << " based on serverType=" << service.serverType);
  }
  return SUCCESS;
}

int RegistClient::_checkArgs(const SGService &oservice) {
  // args check
  std::string appkey = oservice.appkey;
  if (appkey.empty()) {
    LOG_ERROR("fail to register, because the appkey is empty.");
    return ERR_EMPTY_APPKEY;
  } else if (!IsAppkeyLegal(appkey)) {
    LOG_ERROR("Invalid appkey in regist, appkey = " << appkey);
    return ERR_INVALIDAPPKEY;
  }

  if (!(0 <= oservice.weight && 100 >= oservice.weight)) {
    LOG_ERROR("invalid weight: " << oservice.weight
                                 << ", appkey: " << oservice.appkey
                                 << ", ip: " << oservice.ip
                                 << ", port: " << oservice.port);
    return ERR_INVALID_WEIGHT;
  }

  if (!IsIpAndPortLegal(oservice.ip, oservice.port)) {
    LOG_ERROR("invalid port: " << oservice.port
                               << ", appkey: " << oservice.appkey
                               << ", ip: " << oservice.ip
                               << ", weight: " << oservice.weight);
    return ERR_INVALID_PORT;
  }
  return SUCCESS;
}

void RegistClient::RegistBackendHandler(RegParamPtr regParamPtr) {
  registeZkClient_.RegisterService(
      regParamPtr->sgservice,
      RegistCmd::REGIST,
      regParamPtr->uptCmd);
}

void RegistClient::UnRegistBackendHandler(RegParamPtr regParamPtr) {
  registeZkClient_.RegisterService(
      regParamPtr->sgservice,
      RegistCmd::UNREGIST,
      regParamPtr->uptCmd);
}

}
