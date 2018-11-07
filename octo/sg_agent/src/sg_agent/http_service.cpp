//
// Created by smartlife on 2017/11/17.
//
#include "http_service.h"

using namespace sg_agent;
extern MNS *g_mns;
extern GlobalVar *g_global_var;

muduo::MutexLock SgHttpService::s_cmutex;
SgHttpService *SgHttpService::s_instance = NULL;

const std::string listen_ip = "0.0.0.0";
const int http_port = 5267;
const int INVALID_METHOD = -1;
const int ADD_SERVICE = 0;
const int UPDATE_SERVICE = 1;
const int DELETE_SERVICE = 2;
const int GET_SERVICE = 3;
const int GET_MONITOR_SERVICE = 4;
const int REPLACE_SERVICE = 5;
const int HTTP_MNS_URL = 6;
const int HTTP_MONITOR_URL = 7;

SgHttpService *SgHttpService::GetInstance() {
  if (NULL == s_instance) {
    muduo::MutexLockGuard lock(s_cmutex);
    if (NULL == s_instance) {
      s_instance = new SgHttpService();
    }
  }
  return s_instance;
}
/*
 * parse data from http
 */
int SgHttpService::ParseServiceDataFromHttp(const char *postData, ServicePtr &service) {
  if (NULL == postData) {
    LOG_ERROR("the http post msg is null");
    return FAILURE;
  }
  cJSON *root = cJSON_Parse(postData);
  if (NULL == root) {
    LOG_ERROR("the cJSON_Parse error");
    return FAILURE;
  }
  cJSON *pItem = cJSON_GetObjectItem(root, "remoteAppkey");
  if (NULL != pItem) {
    std::string remote_appkey = pItem->valuestring;
    boost::trim(remote_appkey);
    if (!remote_appkey.empty()) {
      service->__set_remoteAppkey(remote_appkey);
    } else {
      LOG_ERROR("remoteappkey is empty.");
      cJSON_Delete(root);
      return FAILURE;
    }
  } else {
    LOG_ERROR("the flush service remoteappkey is null");
    cJSON_Delete(root);
    return FAILURE;
  }
  pItem = cJSON_GetObjectItem(root, "protocol");
  if (NULL != pItem) {
    std::string protocol = pItem->valuestring;
    boost::trim(protocol);
    if (!protocol.empty()) {
      service->__set_protocol(protocol);
    } else {
      LOG_ERROR("procotol is empty.");
      cJSON_Delete(root);
      return FAILURE;
    }
  } else {
    LOG_ERROR("the flush service procotol is null");
    cJSON_Delete(root);
    return FAILURE;
  }
  pItem = cJSON_GetObjectItem(root, "localAppkey");
  if (NULL != pItem) {
    service->__set_localAppkey(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(root, "version");
  if (NULL != pItem) {
    service->__set_version(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(root, "serviceList");
  if (NULL != pItem) {
    std::vector<SGService> servicelist;
    int size = cJSON_GetArraySize(pItem);
    cJSON *item = NULL;
    for (int i = 0; i < size; ++i) {
      SGService serviceInfo;
      item = cJSON_GetArrayItem(pItem, i);
      if (SUCCESS == ParseNodeData(item, serviceInfo)) {
        serviceInfo.__set_appkey(service->remoteAppkey);
        serviceInfo.__set_protocol(service->protocol);
        serviceInfo.__set_envir(g_global_var->gEnv);//获取本地sg_agent环境
        serviceInfo.__set_lastUpdateTime(time(NULL));//获取当前时间
        servicelist.push_back(serviceInfo);
      } else {
        LOG_WARN("Failed to parse the " << i << "th serviceInfo.");
      }
    }
    if (!servicelist.empty()) {
      service->__set_serviceList(servicelist);
    } else {
      LOG_WARN("servlist is empty , remoteAppkey = " << service->remoteAppkey
                                                     << ", protocol = " << service->protocol);
    }
  }
  cJSON_Delete(root);
  return SUCCESS;
}
/*
 * parse NodeData from json
 */
int SgHttpService::ParseNodeData(cJSON *root, SGService &serviceInfo) {
  cJSON *pItem = cJSON_GetObjectItem(root, "ip");
  if (NULL != pItem) {
    serviceInfo.__set_ip(pItem->valuestring);
  } else {
    LOG_ERROR("the flush service ip is null");
    return FAILURE;
  }
  pItem = cJSON_GetObjectItem(root, "port");
  if (NULL != pItem) {
    serviceInfo.__set_port(pItem->valueint);
  } else {
    LOG_ERROR("the flush port is null");
    return FAILURE;
  }
  pItem = cJSON_GetObjectItem(root, "version");
  if (NULL != pItem) {
    serviceInfo.__set_version(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(root, "weight");
  if (NULL != pItem) {
    serviceInfo.__set_weight(pItem->valueint);
  }
  pItem = cJSON_GetObjectItem(root, "status");
  if (NULL != pItem) {
    serviceInfo.__set_status(pItem->valueint);
  }
  pItem = cJSON_GetObjectItem(root, "role");
  if (NULL != pItem) {
    serviceInfo.__set_role(pItem->valueint);
  }
  pItem = cJSON_GetObjectItem(root, "extend");
  if (NULL != pItem) {
    serviceInfo.__set_extend(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(root, "fweight");
  if (NULL != pItem) {
    serviceInfo.__set_fweight(pItem->valuedouble);
  }
  pItem = cJSON_GetObjectItem(root, "serverType");
  if (NULL != pItem) {
    serviceInfo.__set_serverType(pItem->valueint);
  }
  pItem = cJSON_GetObjectItem(root, "heartbeatSupport");
  if (NULL != pItem) {
    serviceInfo.__set_heartbeatSupport(pItem->valueint);
  }
  pItem = cJSON_GetObjectItem(root, "swimlane");
  if (NULL != pItem) {
    serviceInfo.__set_swimlane(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(root, "hostname");
  if (NULL != pItem) {
    serviceInfo.__set_hostname(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(root, "cell");
  if (NULL != pItem) {
    serviceInfo.__set_cell(pItem->valuestring);
  }
  pItem = cJSON_GetObjectItem(root, "serviceInfo");
  if (pItem) {
    cJSON *svrNames = pItem;
    int size = cJSON_GetArraySize(svrNames);
    for (int i = 0; i < size; ++i) {
      cJSON *item = cJSON_GetArrayItem(svrNames, i);
      if (NULL != item) {
        std::string serviceName(item->string);
        LOG_INFO("service name is:" << serviceName);
        bool unifiedProto =
            (0 != cJSON_GetObjectItem(item, "unifiedProto")->valueint);
        ServiceDetail srv;
        srv.__set_unifiedProto(unifiedProto);
        serviceInfo.serviceInfo[serviceName] = srv;
      }
    }
  }
  LOG_INFO("node flush info: appkey:"
               << serviceInfo.appkey << " ip: "
               << serviceInfo.ip << " status:"
               << serviceInfo.status << " last update time: "
               << serviceInfo.lastUpdateTime);
  return SUCCESS;
}

void SgHttpService::StartService() {

  char localIp[LOCAL_IP], localMask[LOCAL_MASK];
  memset(localIp, '0', LOCAL_IP);
  memset(localMask, '0', LOCAL_MASK);

  if (getIntranet(localIp, localMask) < 0) {
    LOG_ERROR("getIntranet failed");
    return;
  } else {
    LOG_INFO("getIntranet ip and mask are " << localIp << "," << localMask);
  }

  struct event_base *base;
  struct evhttp *http;
  base = event_base_new();
  if (!base) {
    LOG_ERROR("event_base_new() failed");
    return;
  }
  http = evhttp_new(base);
  if (!http) {
    LOG_ERROR("evhttp_new() http server failed");
    event_base_free(base);
    return;
  }
  if (evhttp_bind_socket(http, listen_ip.c_str(), http_port)) {
    LOG_ERROR("http bind socket failed");
    evhttp_free(http);
    event_base_free(base);
    return;
  }

  evhttp_set_gencb(http, SgHttpHandler, this);
  event_base_dispatch(base);
  evhttp_free(http);
  event_base_free(base);

  return;
}

int SgHttpService::UpdateServiceInCache(const ServicePtr &service) {

  LOG_INFO("http service:update service ");
  int ret = g_mns->GetMnsPlugin()->UpdateSrvList(service);
  if (SUCCESS != ret) {
    LOG_ERROR("Run mns update srv list failed, ret = " << ret);
  }
  return ret;
}

int SgHttpService::GetServListAndCacheSize(ServListAndCache &list_and_cache,
                                           const std::string &protocol,
                                           const std::string &appkey) {
  LOG_INFO("http service:get service, protocol = " << protocol << " ,appkey = " << appkey);
  int ret = g_mns->GetMnsPlugin()->GetSrvListAndCacheSize(list_and_cache, protocol, appkey);
  if (SUCCESS != ret) {
    LOG_ERROR("Run mns get srvlist and buffer failed, ret = " << ret);
  }
  return ret;
}

int SgHttpService::RepalceServlistAndCache(const ServicePtr &service) {
  LOG_INFO("http service:replace service.");
  int ret = g_mns->GetMnsPlugin()->RepalceSrvlist(service);
  if (SUCCESS != ret) {
    LOG_ERROR("Run mns replace srvlist and cache failed, ret = " << ret);
  }
  return ret;
}

int SgHttpService::ServiceListByProtocol(const ProtocolRequest &req, ProtocolResponse &_return) {
  LOG_INFO("http service GET servicelist.");

  bool enable_swimlane2 = false;
  if (req.__isset.enableSwimlane2) {
    enable_swimlane2 = req.enableSwimlane2;
  }
  int ret = g_mns->GetMnsPlugin()->GetSrvList(_return.servicelist, req, false, !enable_swimlane2, !enable_swimlane2);
  LOG_INFO("ServiceListByProtocol size = " << _return.servicelist.size());
  _return.errcode = ret;
  if (SUCCESS != ret) {
    LOG_ERROR("ServiceListByProtocol failed, and ret = " << ret);
  }
  return ret;
}

void SgHttpService::SgHttpHandler(struct evhttp_request *httpRequest, void *info) {

  if (NULL == httpRequest || NULL == info) {
    LOG_ERROR("the http handler para is null");
    return;
  }
  u_char *reqBuf;
  int bufsize = EVBUFFER_LENGTH(httpRequest->input_buffer);
  reqBuf = EVBUFFER_DATA(httpRequest->input_buffer);
  const char *decode_uri = evhttp_request_uri(httpRequest);
  char *url = evhttp_decode_uri(decode_uri);
  LOG_INFO("the remote host is: " << httpRequest->remote_host
                                  << ", input url method = " << url
                                  << ",the req data = " << reqBuf
                                  << ", buffsize = " << bufsize);
  struct evbuffer *buf = evbuffer_new();//response buffer
  if (NULL == buf) {
    LOG_ERROR("Failed to create response buffer");
    return;
  }

  SgHttpService *httpService = (SgHttpService *) info;
  int http_err_code = HTTP_OK;
  if (EVHTTP_REQ_GET == httpRequest->type) {
    http_err_code = httpService->Reponse2RequestByGet(url, buf);
  } else if (EVHTTP_REQ_POST == httpRequest->type) {
    http_err_code = httpService->Response2RequestByPost(url, reqBuf, buf);
  }
  evhttp_send_reply(httpRequest, http_err_code, "decode the json data ok!", buf);
  if (NULL != buf) {
    evbuffer_free(buf);
  }
  SAFE_FREE(url);
}

int SgHttpService::ServiceListActionByPost(const int serviceMethod, const u_char *reqBuf,
                                           struct evbuffer *buf) {
  int http_err_code = HTTP_RESPONSE_OK;
  ServicePtr service(new getservice_res_param_t());
  int ret_code = ParseServiceDataFromHttp((const char *) reqBuf, service);
  if (SUCCESS != ret_code) {
    LOG_ERROR("Failed to parse data,  ret = " << ret_code);
    evbuffer_add_printf(buf, "request param error");
    return HTTP_PARAM_ERROR;
  }

  switch (serviceMethod) {
    case ADD_SERVICE: {
      LOG_INFO("http add method");
      break;
    }
    case UPDATE_SERVICE: {
      if (SUCCESS != UpdateServiceInCache(service)) {
        LOG_ERROR("http-update in agent failed");
        http_err_code = HTTP_INNER_ERROR;
      }
      break;
    }
    case DELETE_SERVICE: {
      LOG_INFO("http delete method");
      http_err_code = HTTP_NOT_SUPPORT;
      break;
    }
    case GET_SERVICE: {
      ServListAndCache listandcache;
      if (SUCCESS != GetServListAndCacheSize(listandcache,
                                             service->protocol,
                                             service->remoteAppkey)) {
        LOG_ERROR("http-get in agent failed");
        http_err_code = HTTP_INNER_ERROR;
      } else {
        std::string response = "";
        int ret = ServListAndCache2Json(listandcache, response);
        if (SUCCESS != ret) {
          LOG_ERROR("ServListAndCache(response) to str failed, ret = " << ret);
          http_err_code = HTTP_INNER_ERROR;
        } else {
          evbuffer_add_printf(buf, response.c_str());
        }
      }
      break;
    }
    case REPLACE_SERVICE: {
      if (SUCCESS != RepalceServlistAndCache(service)) {
        LOG_ERROR("http-replace in agent failed");
        http_err_code = HTTP_INNER_ERROR;
      }
      break;
    }
    default: {
      http_err_code = HTTP_NOT_SUPPORT;
      LOG_ERROR("unkown service method, disgardless");
      break;
    }
  }
  return http_err_code;

}
//获取监控信息reqBuf无需做空判断处理
int SgHttpService::MonitorActionByPost(int serviceMethod, const u_char *reqBuf,
                                       struct evbuffer *buf) {
  int http_err_code = HTTP_RESPONSE_OK;
  switch (serviceMethod) {
    case GET_MONITOR_SERVICE: {
      std::string response = "";
      int ret = SgMonitorCollector::GetInstance()->GetCollectorMonitorInfo(response);
      if (SUCCESS != ret) {
        LOG_ERROR("Get collectorMonitorInfo failed, ret = " << ret);
        http_err_code = HTTP_INNER_ERROR;
      } else {
        evbuffer_add_printf(buf, response.c_str());
      }
      break;
    }
    default:http_err_code = HTTP_INNER_ERROR;
      break;
  }
  return http_err_code;
}

/*
 *  response to HTTP requests
 *  缺省为服务列表操作，后续扩展其它
 */
int SgHttpService::Response2RequestByPost(const char *url, const u_char *reqBuf,
                                          struct evbuffer *buf) {

  int serviceMethod = GetServiceMethodFromHttp(url);
  switch (serviceMethod) {

    case GET_MONITOR_SERVICE:{
      return MonitorActionByPost(serviceMethod, reqBuf, buf);
    }
    default:{
      return ServiceListActionByPost(serviceMethod, reqBuf, buf);
    }
  }
  return HTTP_RESPONSE_OK;
}

/*
 * ServListAndCache struct to Json
 */
int SgHttpService::ServListAndCache2Json(const ServListAndCache &list_and_cache, std::string &response) {
  cJSON *json = cJSON_CreateObject();
  char *out;
  if (!json) {
    LOG_ERROR("json is NULL, create json_object failed.");
    return FAILURE;
  }
  cJSON_AddNumberToObject(json, "origin_servlist_size", list_and_cache.origin_servlist_size);
  cJSON_AddNumberToObject(json, "filte_servlist_size", list_and_cache.filte_servlist_size);
  cJSON_AddNumberToObject(json, "origin_cache_size", list_and_cache.origin_cache_size);
  cJSON_AddNumberToObject(json, "filte_cache_size", list_and_cache.filte_cache_size);
  int ret = Service2Json(list_and_cache.origin_servicelist, json, "origin_servicelist");
  if (SUCCESS != ret) {
    LOG_ERROR("Failed to change origin_servicelist to json, ret = " << ret);
    cJSON_Delete(json);
    return FAILURE;
  }
  ret = Service2Json(list_and_cache.filte_servicelist, json, "filte_servicelist");
  if (SUCCESS != ret) {
    LOG_ERROR("Failed to change filte_servicelist to json, ret = " << ret);
    cJSON_Delete(json);
    return FAILURE;
  }
  out = cJSON_Print(json);
  response = out;
  SAFE_FREE(out);
  cJSON_Delete(json);
  boost::trim(response);
  if (response.empty()) {
    LOG_ERROR("json to str failed, the response json is empty.");
    return FAILURE;
  }
  return SUCCESS;
}
/*
 * SGService to json
 */
int SgHttpService::Service2Json(const std::vector<SGService> &servicelist, cJSON *json, const char *type) {
  cJSON *all_srvlist_json = cJSON_CreateArray();
  if (!all_srvlist_json) {
    LOG_ERROR("all_srvlist_json is NULL, create json_object failed.");
    return FAILURE;
  }
  for (std::vector<SGService>::const_iterator iter = servicelist.begin();
       iter != servicelist.end(); ++iter) {
    cJSON *item = cJSON_CreateObject();
    int ret = SGService2Json(*iter, item);
    if (SUCCESS != ret) {
      LOG_ERROR("SGService2Json failed, ret = " << ret);
      return FAILURE;
    }
    cJSON_AddItemToArray(all_srvlist_json, item);
  }
  cJSON_AddItemToObject(json, type, all_srvlist_json);
  return SUCCESS;
}

int SgHttpService::SGService2Json(const SGService &oservice, cJSON *root) {
  cJSON_AddItemToObject(root, "appkey", cJSON_CreateString(oservice.appkey.c_str()));
  cJSON_AddItemToObject(root, "version", cJSON_CreateString(oservice.version.c_str()));
  cJSON_AddItemToObject(root, "ip", cJSON_CreateString(oservice.ip.c_str()));
  cJSON_AddNumberToObject(root, "port", oservice.port);
  cJSON_AddNumberToObject(root, "weight", oservice.weight);
  cJSON_AddNumberToObject(root, "status", oservice.status);
  cJSON_AddNumberToObject(root, "role", oservice.role);
  cJSON_AddNumberToObject(root, "env", g_global_var->gAppenv);
  cJSON_AddNumberToObject(root, "lastUpdateTime", oservice.lastUpdateTime);
  cJSON_AddItemToObject(root, "extend", cJSON_CreateString(oservice.extend.c_str()));
  //后续添加,注意有可能没有
  cJSON_AddNumberToObject(root, "fweight", oservice.fweight);
  cJSON_AddNumberToObject(root, "serverType", oservice.serverType);

  int32_t heartbeatSupport = oservice.heartbeatSupport;
  cJSON_AddNumberToObject(root, "heartbeatSupport", heartbeatSupport);
  cJSON_AddItemToObject(root, "protocol", cJSON_CreateString(oservice.protocol.c_str()));
  cJSON_AddItemToObject(root, "swimlane", cJSON_CreateString(oservice.swimlane.c_str()));
  cJSON_AddItemToObject(root, "cell", cJSON_CreateString(oservice.cell.c_str()));

  cJSON *item = cJSON_CreateObject();
  if (NULL == item) {
    LOG_ERROR("cJson failed to CreateObject. Item is serviceInfo");
    return FAILURE;
  }
  int ret = JsonZkMgr::cJson_AddServiceObject(oservice.serviceInfo, root,
                                              item, std::string("serviceInfo"));
  if (0 != ret) {
    LOG_ERROR("failed to add serviceName to root");
    return FAILURE;
  }
  //cJSON_AddItemToObject(root, "unifiedProto", cJSON_CreateBool(oservice.unifiedProto));

  // std::string out = cJSON_Print(root);
  // LOG_INFO("out: " << out);

  return SUCCESS;
}

int SgHttpService::GetKeyValueFromUrl(const std::string &url, const std::string &key, std::string &value) {
  int len = url.length();
  size_t pos = url.find(key);
  if (std::string::npos != pos) {
    for (int i = pos + key.length(); i < len && '&' != url[i]; ++i) {
      value += url[i];
    }
    if (value.empty()) {
      LOG_ERROR("key: " << key << " is empty.");
      return FAILURE;
    }
  } else {
    LOG_ERROR("key: " << key << " does not exist");
    return FAILURE;
  }
  return SUCCESS;
}

int SgHttpService::GetServiceParamFromUrl(const std::string &url, ProtocolRequest &params) {
  LOG_INFO("the MNS url is: " << url);
  std::string tmp_url = url;
  boost::trim(tmp_url);
  size_t pos = tmp_url.find("/api/servicelist?");
  size_t appkey_pos = tmp_url.find("appkey=");
  size_t protocol_pos = tmp_url.find("protocol=");
  if (std::string::npos != pos && std::string::npos != appkey_pos
      && std::string::npos != protocol_pos) {
    std::string env = "";
    int ret = GetKeyValueFromUrl(tmp_url, "env=", env);
    if (SUCCESS == ret) {
      if (env == g_global_var->gEnvStr) {
        LOG_INFO("env is " << env);
      } else {
        LOG_ERROR("env:" << env << " is different from local env:" << g_global_var->gEnvStr);
        return ERR_INVALID_ENV;
      }
    } else {
      LOG_WARN("env error, ret = " << ret);
    }
    std::string appkey = "";
    ret = GetKeyValueFromUrl(tmp_url, "appkey=", appkey);
    if (SUCCESS == ret) {
      params.remoteAppkey = appkey;
    } else {
      LOG_ERROR("appkey error, ret = " << ret);
      return HTTP_PARAM_ERROR;
    }
    std::string protocol = "";
    ret = GetKeyValueFromUrl(tmp_url, "protocol=", protocol);
    if (SUCCESS == ret) {
      params.protocol = protocol;
    } else {
      LOG_ERROR("protocol error, ret = " << ret);
      return HTTP_PARAM_ERROR;
    }
    std::string hostname = "";
    ret = GetKeyValueFromUrl(tmp_url, "hostname=", hostname);
    if (SUCCESS == ret) {
      LOG_INFO("hostname is " << hostname);
    } else {
      LOG_WARN("hostname error, ret = " << ret);
    }
    std::string localip = "";
    ret = GetKeyValueFromUrl(tmp_url, "localip=", localip);
    if (SUCCESS == ret) {
      LOG_INFO("localip is " << localip);
    } else {
      LOG_WARN("localip error ,ret = " << ret);
    }
  } else {
    LOG_ERROR("params are empty.");
    return HTTP_PARAM_ERROR;
  }
  return HTTP_OK;
}

int SgHttpService::Reponse2RequestByGet(const char *url, struct evbuffer *buf) {
  ProtocolRequest req;
  int http_code = GetServiceParamFromUrl(url, req);
  if (HTTP_OK != http_code) {
    LOG_ERROR("http response failed, err_code = " << http_code);
    return http_code;
  }
  std::vector<SGService> result;
  req.__set_remoteAppkey(req.remoteAppkey);
  req.__set_protocol(req.protocol);
  ProtocolResponse _return;
  int ret = ServiceListByProtocol(req, _return);
  if (SUCCESS != ret) {
    LOG_ERROR("Failed to get servicelist by http, ret = " << ret);
    return HTTP_INNER_ERROR;
  }
  std::string response = "";
  ret = ProtocolResponse2Json(_return, response);
  if (SUCCESS != ret) {
    LOG_ERROR("Failed to ProtocolResponse2Json, ret = " << ret);
    return HTTP_INNER_ERROR;
  }
  evbuffer_add_printf(buf, response.c_str());
  //LOG_INFO("response : " << response);
  return http_code;
}

int SgHttpService::ProtocolResponse2Json(const ProtocolResponse &res, std::string &response) {
  cJSON *parent = cJSON_CreateObject();
  char *out;
  if (NULL == parent) {
    LOG_ERROR("Failed to create json object:parent.");
    return FAILURE;
  }
  cJSON *child = cJSON_CreateObject();
  if (NULL == child) {
    LOG_ERROR("Failed to create json object:child.");
    return FAILURE;
  }
  cJSON_AddNumberToObject(parent, "ret", HTTP_RESPONSE_OK);
  cJSON_AddStringToObject(parent, "retMsg", "success");
  int ret = Service2Json(res.servicelist, child, "serviceList");
  if (SUCCESS != ret) {
    LOG_ERROR("Failed to parse service, ret = " << ret);
    cJSON_Delete(parent);
    return FAILURE;
  }

  cJSON_AddItemToObject(parent, "data", child);
  out = cJSON_Print(parent);
  response = out;
  boost::trim(response);
  if (response.empty()) {
    LOG_ERROR("json to str failed, the response json is empty.");
    SAFE_FREE(out);
    cJSON_Delete(parent);
    return FAILURE;
  }
  SAFE_FREE(out);
  cJSON_Delete(parent);
  return SUCCESS;
}

//todo:monitor子方法后续细分后解析
int SgHttpService::DecodeMonitorUrlMethod(const std::string &url) {
  LOG_INFO("the monitor url is:" << url);

  return GET_MONITOR_SERVICE;

}

int SgHttpService::DecodeMnsUrlMethod(const std::string &url) {

  LOG_INFO("the Mns Url is:" << url);
  std::string strtmp = "";
  strtmp.assign(url, strlen("/api/mns/provider/"), url.length());
  LOG_INFO("find method:" << strtmp);
  if (SUCCESS == strtmp.compare("add")) {
    return ADD_SERVICE;
  } else if (SUCCESS == strtmp.compare("delete")) {
    return DELETE_SERVICE;
  } else if (SUCCESS == strtmp.compare("update")) {
    return UPDATE_SERVICE;
  } else if (SUCCESS == strtmp.compare("get")) {
    return GET_SERVICE;
  } else if (SUCCESS == strtmp.compare("monitorinfo")) {//名字修改monitor
    return GET_MONITOR_SERVICE;
  } else if (SUCCESS == strtmp.compare("replace")) {//强制替换列表
    return REPLACE_SERVICE;
  } else {
    LOG_ERROR("unkown http sub method");
  }
  return INVALID_METHOD;

}

int SgHttpService::GetServiceMethodFromHttp(const char *inputMethod) {
  if (NULL == inputMethod) {
    LOG_ERROR("the inputMethod is null");
    return INVALID_METHOD;
  }
  std::string inputStr = inputMethod;
  LOG_INFO("Input str = " << inputStr);
  std::size_t pos_mns = inputStr.find("/api/mns/provider/");//todo
  if (std::string::npos != pos_mns && 0 == pos_mns) {
    return DecodeMnsUrlMethod(inputStr);
  }
  std::size_t pos_mon = inputStr.find("/api/monitor");//todo
  if (std::string::npos != pos_mon && 0 == pos_mon) {
    return DecodeMonitorUrlMethod(inputStr);
  }
  return INVALID_METHOD;
}
void SgHttpService::StartHttpServer() {

  LOG_INFO("http server init");
  m_http_loop = m_http_thread.startLoop();
  m_http_loop->runInLoop(boost::bind(&SgHttpService::StartService, this));

}






