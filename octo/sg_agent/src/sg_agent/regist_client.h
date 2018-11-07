// =====================================================================================
//
//      Filename:  registclient.h
//
//      Description: regist service to zookeeper
//
//      Version:  1.0
//      Created:  2015/04/15
//      Revision:  none
//
//
// =====================================================================================

#ifndef __registclient__H__
#define __registclient__H__

#include <string>
#include <pthread.h>
#include <boost/bind.hpp>
#include <boost/shared_ptr.hpp>
#include <util/idc.h>
#include <muduo/base/Mutex.h>
#include "sgagent_service_types.h"
#include "sgagent_worker_service_types.h"
#include "comm/sgagent_mq.h"
#include "util/sg_agent_def.h"
#include "status_mgr.h"
#include "registe_zk_client.h"
#include "sgcommon/common_interface.h"
#include "mnscache_client.h"

namespace sg_agent { 

class RegistClient {
public:
  typedef boost::shared_ptr<SGService> SGServicePtr;
  typedef boost::shared_ptr<regist_req_param_t> RegParamPtr;
  
  class RegistHandler : public muduo::net::Task {
   public:
    RegistHandler(RegistClient* client, RegParamPtr service) 
      : client_(client), service_(service) {} 
    virtual void Run() {
      client_->RegistBackendHandler(service_);
    }
   private:
    RegParamPtr service_;
    RegistClient* client_;
  };
  
  class UnRegistHandler : public muduo::net::Task {
   public:
    UnRegistHandler(RegistClient* client, RegParamPtr service) 
      : client_(client), service_(service) {} 
    virtual void Run() {
      client_->UnRegistBackendHandler(service_);
    }
   private:
    RegParamPtr service_;
    RegistClient* client_;
  };

  /**
   * @brief:  To init EventLoop, retry_times. Need be called before sendModuleInvokeInfo.
   * @return: 0 is OK, other is ErrorCode.
   */
  int init(int retry = DEFAULT_REGIST_RETRY);

  void SetHotelRegisterWhiteList(const std::set<std::string> &appkeys,
                                 const std::vector<boost::shared_ptr<IDC> > &idcs);

  void ResetHoteltravelWhiteList(const std::set<std::string> &appkeys,
                                               const std::vector<boost::shared_ptr<IDC> > &idcs);

  bool IsHotelAllowRegister(const SGService &service);
  
  /**
   * @brief:  To send regist info to Server.
   * @param: uptCmd default is reset
   * @return: 0 is OK( which means succeed to insert message into MQ).
   *          Other is ErrorCode.
   */
  int registeService(const SGService& oservice, bool isNewCloud, int32_t uptCmd = UptCmd::ADD);
  
  /**
   * @brief:  To send unRegist info to Server.
   * @return: 0 is OK( which means succeed to insert message into MQ).
   *          Other is ErrorCode.
   */
  int unRegisteService(const SGService& oservice, bool isNewCloud);
  
  void setRetry(int retry);

  static RegistClient* getInstance();
  static void Destroy();

  void RegistBackendHandler(RegParamPtr service);

  void UnRegistBackendHandler(RegParamPtr service);

	RegisteZkClient* GetRegisteZkClient() {
		return &registeZkClient_;
	} 

private:
  RegistClient();
  ~RegistClient();

  int _unifiedProtocol(SGService& service);
  
  int _checkArgs(const SGService& oservice);
  
  int m_retry;
  
  StatusMgr* statusMgr;


  static RegistClient *m_registclient;
  static muduo::MutexLock s_cmutex;
  
  muduo::net::IEventLoopThreadProxy* loop_thread_;
  muduo::net::IEventLoopProxy* loop_;

  RegisteZkClient registeZkClient_;

  std::set<std::string> m_hotel_white_appkeys;
  std::vector<boost::shared_ptr<IDC> > m_hotel_white_idcs;
};
} //namespace

#endif

