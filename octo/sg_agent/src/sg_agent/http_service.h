//
// Created by smartlife on 2017/11/18.
//

#ifndef SG_AGENT_HTTP_SERVICE_H
#define SG_AGENT_HTTP_SERVICE_H

#pragma once

#include <assert.h>
#include <string>
#include <map>
#include <list>
#include <iterator>
#include <stdio.h>
#include <evhttp.h>
#include <boost/algorithm/string/trim.hpp>
#include "../comm/cJSON.h"
#include "sgagent_service_types.h"
#include "../comm/inc_comm.h"
#include "muduo/net/EventLoop.h"
#include "muduo/net/EventLoopThread.h"
#include "boost/bind.hpp"
#include "util/json_zk_mgr.h"
#include "mns/mns_comm.h"
#include "mns.h"
#include "util/global_def.h"
//#include "util/falcon_mgr.h"
#include "../remote/monitor/monitor_collector.h"

#define LOCAL_IP 32
#define LOCAL_MASK 64
typedef boost::shared_ptr<getservice_res_param_t> ServicePtr;

namespace sg_agent {


class SgHttpService {
 private:
  SgHttpService() : m_http_loop(NULL) {};
 public:
  ~SgHttpService() {};

  void StartService();
  void StartHttpServer();
  static SgHttpService *GetInstance();
  int UpdateServiceInCache(const ServicePtr &service);
  int GetServListAndCacheSize(ServListAndCache &list_and_cache,
                               const std::string &protocol,
                               const std::string &appkey);
  int RepalceServlistAndCache(const ServicePtr &service);

 private:

  static void SgHttpHandler(struct evhttp_request *httpRequest, void *info);
  int ParseNodeData(cJSON *root, SGService &serviceInfo);
  int GetServiceMethodFromHttp(const char *inputMethod);
  int ParseServiceDataFromHttp(const char *postData, ServicePtr &service);
  int Response2RequestByPost(const char* url, const u_char *reqBuf,
                       struct evbuffer *buf);

  int Service2Json(const std::vector<SGService> &servicelist, cJSON *json, const char* type);

  int ServListAndCache2Json(const ServListAndCache &list_and_cache, std::string &response);

  int GetServiceParamFromUrl(const std::string& url, ProtocolRequest &params);

  int ServiceListByProtocol(const ProtocolRequest &req, ProtocolResponse &_return);

  int Reponse2RequestByGet(const char* url, struct evbuffer *buf);

  int ProtocolResponse2Json(const ProtocolResponse &res, std::string &response);

  int SGService2Json(const SGService& oservice, cJSON *root);

  int GetKeyValueFromUrl(const std::string &url, const std::string &key, std::string &value);
  int DecodeMonitorUrlMethod(const std::string& url);
  int DecodeMnsUrlMethod(const std::string& url);

  int ServiceListActionByPost(const int serviceMethod,const u_char *reqBuf,
                                             struct evbuffer *buf);
  int MonitorActionByPost(int serviceMethod,const u_char *reqBuf,
                                         struct evbuffer *buf);

  static SgHttpService *s_instance;
  static muduo::MutexLock s_cmutex;
  muduo::net::EventLoopThread m_http_thread;
  muduo::net::EventLoop *m_http_loop;

};

}

#endif //SG_AGENT_HTTP_SERVICE_H
