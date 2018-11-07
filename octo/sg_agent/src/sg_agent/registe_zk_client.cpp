// =====================================================================================
// 
//       Filename:  zkclient.cpp
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-08-27 16时43分26秒
//       Revision:  none
// 
// =====================================================================================
#include <unistd.h>
#include <string.h>
#include <map>
#include <sstream>
#include <iostream>

#include "registe_zk_client.h"
#include "util/json_zk_mgr.h"
#include "zk_tools.h"
#include "comm/log4cplus.h"
#include "comm/thrift_serialize.h"
#include "util/sg_agent_def.h"
#include "util/SGAgentErr.h"
#include "util/appkey_path_operation.h"

#include "util/global_def.h"

extern GlobalVar *g_global_var;

static const int HeartbeatUnSupport = 0;

RegisteZkClient::RegisteZkClient() : retry_(3) {
}

RegisteZkClient::~RegisteZkClient() {
}

int RegisteZkClient::RegisterService(const SGService &oservice,
                                     RegistCmd::type regCmd, int uptCmd) {
  //比较重要的服务，日志级别提高
  std::string serviceName = "";
  std::string unifiedProto = "";
  for (std::map<std::string, ServiceDetail>::const_iterator iter = oservice.serviceInfo.begin();
       iter != oservice.serviceInfo.end(); ++iter) {
    serviceName += iter->first + " ";
    unifiedProto += iter->second.unifiedProto + " ";
  }
  LOG_INFO("to register service to ZK"
               << ", appkey : " << oservice.appkey
               << ", ip : " << oservice.ip
               << ", version : " << oservice.version
               << ", port : " << oservice.port
               << ", env : " << oservice.envir
               << ", local env : " << g_global_var->gEnv
               << ", status : " << oservice.status
               << "; fweight : " << oservice.fweight
               << "; protocol: " << oservice.protocol
               << "; serverType : " << oservice.serverType
               << "; siwmlane : " << oservice.swimlane
               << "; cell : " << oservice.cell
               << "; serviceName : " << serviceName
               << "; unifiedProto : " << unifiedProto
               << "; regCmd : " << regCmd);

  int loop_times = 0;
  do {
    int ret = RegisterServiceToZk(oservice, regCmd, uptCmd);
    if (ret == 0 || ERR_ILLEGAL_REGISTE == ret || ERR_NODE_NOTFIND == ret) {
      return ret;
    } else {
      LOG_INFO("retry to registry , appkey is : " << oservice.appkey);

      //超过最大重试次数
      if (loop_times > retry_) {
        LOG_ERROR("register service to ZK fail! loop_times > retry_, loop_times is : "
                      << loop_times
                      << ", appkey : " << oservice.appkey.c_str()
                      << ", ip : " << oservice.ip
                      << ", port : " << oservice.port
                      << ", env : " << oservice.envir
                      << ", local env : " << g_global_var->gEnv
                      << ", status : " << oservice.status
                      << "; fweight : " << oservice.fweight
                      << "; serverType : " << oservice.serverType
                      << "; protocol: " << oservice.protocol
                      << ", default retry times is : " << retry_);
        return ERR_REGIST_SERVICE_ZK_FAIL;
      }
    }
  } while (retry_ > loop_times++);
}

int RegisteZkClient::RegisterServiceToZk(const SGService &oservice,
                                         RegistCmd::type regCmd, int uptCmd) {
  int ret = RegisterServiceNodeToZk(oservice, regCmd, uptCmd);

  if (0 == ret && RegistCmd::REGIST == regCmd) {
    return RegisteServiceNameToZk(oservice, uptCmd);
  }
  return ret;
}

int RegisteZkClient::RegisterServiceNodeToZk(const SGService &oservice,
                                             RegistCmd::type regCmd, int uptCmd) {

  char zkProviderPath[MAX_BUF_SIZE] = {0};
  int ret =
      ZkTools::operation_.genRegisterZkPath(zkProviderPath, oservice.appkey, oservice.protocol, oservice.serverType);
  if (0 != ret) {
    LOG_ERROR("_gen registerService fail! appkey = " << oservice.appkey
                                                     << ", protocol = " << oservice.protocol
                                                     << ", serverType = " << oservice.serverType);
    return ret;
  }
  LOG_INFO("_gen registerService zkProviderPath: " << zkProviderPath);

  /// Firstly, check appkey node.
  /// Dont create one. Just let service owner register it by MSGP.
  shared_ptr<ZkExistsRequest> zk_exists_req = shared_ptr<ZkExistsRequest>(new ZkExistsRequest());
  zk_exists_req->path = zkProviderPath;
  zk_exists_req->watch = 0;
  ret = ZkClient::getInstance()->ZkExists(zk_exists_req.get());
  if (ZNONODE == ret || ret == -1) {
    LOG_ERROR("appkey not exist.zkpath:" << zkProviderPath
                                         << ", ip : " << oservice.ip
                                         << ", port : " << oservice.port
                                         << "; fweight : " << oservice.fweight
                                         << "; serverType : " << oservice.serverType
                                         << "; protocol: " << oservice.protocol
                                         << ", or zk handle fail ret: " << ret);
    return ret;
  }

  /// Check service node, create or update it
  char zkPath[MAX_BUF_SIZE] = {0};
  snprintf(zkPath, sizeof(zkPath), "%s/%s:%d", zkProviderPath,
           oservice.ip.c_str(), oservice.port);

  std::string strJson;
  zk_exists_req->path = zkPath;
  zk_exists_req->watch = 0;
  ret = ZkClient::getInstance()->ZkExists(zk_exists_req.get());
  if (ZNONODE == ret) {
    if (RegistCmd::UNREGIST == regCmd || UptCmd::DELETE == uptCmd) {
      // if the zk node don't exist, ignore unRegister
      LOG_WARN("ignore unRegister, because the zk node don't exist"
                   << ", zkPath: " << zkPath
                   << ", status : " << oservice.status
                   << "; fweight : " << oservice.fweight
                   << "; serverType : " << oservice.serverType
                   << "; protocol : " << oservice.protocol
                   << "; swimlane: " << oservice.swimlane
                   << "; cell: " << oservice.cell
                   << ", ip : " << oservice.ip
                   << ", regCmd: " << regCmd
                   << ", uptCmd: " << uptCmd);
      return ERR_NODE_NOTFIND;
    }
    // 合法验证
    if (!CheckLegal(oservice)) {
      LOG_ERROR("appkey is not legal to regist in this ip, appkey = "
                    << oservice.appkey);
      return ERR_ILLEGAL_REGISTE;
    }

    SGService oTmp = const_cast<SGService &>(oservice);
    JsonZkMgr::SGService2Json(oTmp, strJson, g_global_var->gEnv);

    char szPathBuffer[MAX_BUF_SIZE] = {0};

    LOG_INFO("register: create a new zk node"
                 << ", zkPath: " << zkPath
                 << ", appkey : " << oTmp.appkey
                 << ", env : " << oTmp.envir
                 << ", local env : " << g_global_var->gEnv
                 << ", status : " << oTmp.status
                 << "; fweight : " << oTmp.fweight
                 << "; serverType : " << oTmp.serverType
                 << "; protocol : " << oTmp.protocol
                 << "; swimlane: " << oTmp.swimlane
                 << ", ip : " << oTmp.ip);
    ZkCreateRequest zk_create_req;
    ZkCreateInvokeParams zk_create_params;
    zk_create_req.path = zkPath;
    zk_create_req.value = strJson;
    zk_create_req.value_len = strJson.size();
    zk_create_params.zk_create_request = zk_create_req;
    ret = ZkClient::getInstance()->ZkCreate(&zk_create_params);
    if (ZOK != ret || ret == -1) {
      LOG_WARN("WARN zoo_create failed zkPath:" << zkPath
                                                << ", zValue:" << strJson.c_str()
                                                << ", zValue size:" << strJson.size()
                                                << ", ret:" << ret);
      return ret;
    }
  } else if (ZOK == ret) {
    int datalen = kZkContentSize;
    std::string strOrgJson;
    struct Stat stat;

    ZkGetRequest zk_get_req;
    ZkGetInvokeParams zk_get_param;
    zk_get_req.path = zkPath;
    zk_get_req.watch = 0;
    zk_get_param.zk_get_request = zk_get_req;
    ret = ZkClient::getInstance()->ZkGet(&zk_get_param);
    if (ZOK != ret) {
      LOG_WARN("zoo_get origin content fail or zk handle is null, ret: " << ret
                                                                         << ", zkPath: " << zkPath);
      return ret;
    }
    strOrgJson = zk_get_param.zk_get_response.buffer;
    datalen = zk_get_param.zk_get_response.buffer_len;
    stat = zk_get_param.zk_get_response.stat;

    SGService orgService;
    ret = JsonZkMgr::Json2SGService(strOrgJson, orgService);
    if (ret != 0) {
      LOG_ERROR("Json2SGService failed! strJson = " << strOrgJson
                                                    << "ret = " << ret);
      return ret;
    }

    if (fb_status::STOPPED == orgService.status || fb_status::STARTING == orgService.status) {
      std::string status_str = fb_status::STOPPED == orgService.status ? "STOPPED" : "STARTING";
      LOG_INFO("the zk node status is " << status_str << ", don't change its status, appkey = "
                                        << oservice.appkey
                                        << ", ip = " << oservice.ip
                                        << ", port = " << oservice.port);
    } else {
      LOG_INFO("the zk node status(" << orgService.status <<
                                     ") is not equals to the one which is defined by user, use the later("
                                     << oservice.status << ")")
      orgService.status = oservice.status;
    }
    //reset oService last_update_time
    orgService.lastUpdateTime = time(0);

    if (RegistCmd::REGIST == regCmd) {
      orgService.version = oservice.version;
      orgService.extend = oservice.extend;
      orgService.__set_swimlane(oservice.swimlane);
      orgService.__set_cell(oservice.cell);

      // 对于新版本回退到旧版时的兼容性措施, 此处为旧版本
      if (version_.IsOldVersion(oservice.version)) {
        LOG_INFO("version = " << orgService.version
                              << ", need to clear serviceInfo");
        orgService.heartbeatSupport = HeartbeatUnSupport;
        orgService.serviceInfo.clear();
      } else {
        orgService.heartbeatSupport = oservice.heartbeatSupport;
        EditServiceName(orgService, oservice, uptCmd);
      }
    }

    strOrgJson = "";
    //NOTICE: sg2Service always return 0
    if (JsonZkMgr::SGService2Json(orgService, strOrgJson, g_global_var->gEnv) < 0) {
      LOG_ERROR("_SGService2Json failed");
      return -1;
    }

    LOG_INFO("to regist when node exists"
                 << ", uptCmd : " << uptCmd
                 << ", appkey : " << orgService.appkey
                 << ", env : " << orgService.envir
                 << ", local env : " << g_global_var->gEnv
                 << ", status : " << orgService.status
                 << "; fweight : " << orgService.fweight
                 << "; serverType : " << orgService.serverType
                 << "; protocol: " << orgService.protocol
                 << ", ip : " << orgService.ip);
    shared_ptr<ZkSetRequest> zk_set_req = shared_ptr<ZkSetRequest>(new ZkSetRequest());
    zk_set_req->path = zkPath;
    zk_set_req->buffer = strOrgJson;
    zk_set_req->version = stat.version;
    ret = ZkClient::getInstance()->ZkSet(zk_set_req.get());
    //ret = ZkClient::getInstance()->zk_set(zkPath, strOrgJson.c_str(), strOrgJson.size(), -1);
    if (ZOK != ret || ret == -1) {
      LOG_FATAL("ERR zoo_set failed zkPath:" << zkPath
                                             << " szValue:" << strOrgJson.c_str()
                                             << " szValue size:" << strOrgJson.size()
                                             << "ret:" << ret);
      return ret;
    }
  } else if (ZNOAUTH == ret) {
    LOG_FATAL("ZNOAUTH in registerService. ret = " << ret);
    return ret;
  } else {
    LOG_FATAL("ERR other error: " << ret << " in registerService");
    return ret;
  }

  /// update service node last modified time
  CProviderNode oprovider;
  oprovider.appkey = oservice.appkey;
  oprovider.lastModifiedTime = time(NULL);

  strJson = "";
  JsonZkMgr::ProviderNode2Json(oprovider, strJson);
  shared_ptr<ZkSetRequest> zk_set_req = shared_ptr<ZkSetRequest>(new ZkSetRequest());
  zk_set_req->path = zkProviderPath;
  zk_set_req->buffer = strJson;
  zk_set_req->version = -1;
  ret = ZkClient::getInstance()->ZkSet(zk_set_req.get());
  //ret = ZkClient::getInstance()->zk_set(zkProviderPath, strJson.c_str(), strJson.size(), -1);
  if (ZOK != ret || ret == -1) {
    LOG_ERROR("ERR zoo_set provider failed zkPath:" << zkProviderPath
                                                    << " szValue:" << strJson.c_str()
                                                    << " szValue size:" << strJson.size()
                                                    << "ret: " << ret);
    return ret;
  }

  return 0;
}

int RegisteZkClient::RegisteServiceNameToZk(const SGService &oservice, int uptCmd) {
  if (0 == oservice.serviceInfo.size()) {
    return 0;
  }

  for (std::map<std::string, ServiceDetail>::const_iterator iter = oservice.serviceInfo.begin();
       iter != oservice.serviceInfo.end();
       ++iter) {
    char zkServicePath[MAX_BUF_SIZE] = {0};
    std::string serviceName = iter->first;
    int ret = ZkTools::operation_.genServiceNameZkPath(
        zkServicePath, serviceName,
        oservice.appkey);
    if (0 != ret) {
      return ret;
    }
    LOG_INFO("_gen serviceNamePath: " << zkServicePath);

    // Firstly, check service node. Create it or update it
    shared_ptr<ZkExistsRequest> zk_exists_req = shared_ptr<ZkExistsRequest>(new ZkExistsRequest());
    zk_exists_req->path = zkServicePath;
    zk_exists_req->watch = 0;
    ret = ZkClient::getInstance()->ZkExists(zk_exists_req.get());
    if (ZNONODE == ret || ret == -1) {
      LOG_INFO("servicename not exist.zkpath:"
                   << zkServicePath);
      std::string serviceJson;
      ret = JsonZkMgr::ServiceNameNode2Json(serviceName, serviceJson);
      if (0 != ret) {
        return ret;
      }

      char szPathBuffer[MAX_BUF_SIZE] = {0};
      LOG_INFO("to create serviceName when node not exit"
                   << ", zkPath: " << zkServicePath
                   << ", servicename : " << serviceName);
      ZkCreateRequest zk_create_req;
      ZkCreateInvokeParams zk_create_params;
      zk_create_req.path = zkServicePath;
      zk_create_req.value = serviceJson;
      zk_create_req.value_len = serviceJson.size();
      zk_create_params.zk_create_request = zk_create_req;
      ret = ZkClient::getInstance()->ZkCreate(&zk_create_params);
      if (ZOK != ret || ret == -1) {
        LOG_ERROR("WARN zoo_create failed zkPath:" << zkServicePath
                                                   << ", zValue:" << serviceJson.c_str()
                                                   << ", zValue size:" << serviceJson.size()
                                                   << ", ret:" << ret);
        return ret;
      }
    } else if (ZNOAUTH == ret) {
      LOG_FATAL("ZNOAUTH in serviceName. ret = " << ret);
      return ret;
    }

    /// Check service protocol node, create or update it
    char zkPath[MAX_BUF_SIZE] = {0};
    ret = ZkTools::operation_.genServiceNameZkPathNode(
        zkPath, serviceName, oservice.protocol,
        oservice.appkey);
    if (0 != ret) {
      return ret;
    }
    LOG_INFO("_gen serviceNamePathNode: " << zkPath);

    std::string strJson;
    zk_exists_req->path = zkPath;
    zk_exists_req->watch = 0;
    ret = ZkClient::getInstance()->ZkExists(zk_exists_req.get());
    if (ZNONODE == ret) {
      ServiceNode oTmp;
      oTmp.serviceName = serviceName;
      oTmp.appkeys.insert(oservice.appkey);
      oTmp.lastUpdateTime = time(0);

      JsonZkMgr::ServiceNode2Json(oTmp, strJson);

      char szPathBuffer[MAX_BUF_SIZE] = {0};

      LOG_INFO("to regist when node not exit"
                   << ", serviceName: " << serviceName
                   << ", appkey : " << oservice.appkey);
      ZkCreateRequest zk_create_req;
      ZkCreateInvokeParams zk_create_params;
      zk_create_req.path = zkPath;
      zk_create_req.value = strJson;
      zk_create_req.value_len = strJson.size();
      zk_create_params.zk_create_request = zk_create_req;
      ret = ZkClient::getInstance()->ZkCreate(&zk_create_params);
      if (ZOK != ret || ret == -1) {
        LOG_WARN("WARN zoo_create failed zkPath:" << zkPath
                                                  << ", zValue:" << strJson.c_str()
                                                  << ", zValue size:" << strJson.size()
                                                  << ", ret:" << ret);
        return ret;
      }
    } else if (ZOK == ret) {
      int datalen = kZkContentSize;
      std::string strOrgJson;
      struct Stat stat;

      ZkGetInvokeParams zk_get_param;
      zk_get_param.zk_get_request.path = zkPath;
      zk_get_param.zk_get_request.watch = 0;
      ret = ZkClient::getInstance()->ZkGet(&zk_get_param);
      if (ZOK != ret) {
        LOG_WARN("zoo_get origin content fail or zk handle is null, ret: " << ret
                                                                           << ", zkPath: " << zkPath);
        return ret;
      }
      strOrgJson = zk_get_param.zk_get_response.buffer;
      datalen = zk_get_param.zk_get_response.buffer_len;
      stat = zk_get_param.zk_get_response.stat;

      ServiceNode orgService;
      ret = JsonZkMgr::Json2ServiceNode(strOrgJson, orgService);
      if (ret != 0) {
        LOG_ERROR("Json2ServiceNode failed! strJson = " << strOrgJson
                                                        << "ret = " << ret);
        return ret;
      }

      //reset oService last_update_time
      orgService.lastUpdateTime = time(0);
      orgService.appkeys.insert(oservice.appkey);

      strOrgJson = "";
      //NOTICE: sg2Service always return 0
      if (JsonZkMgr::ServiceNode2Json(orgService, strOrgJson) < 0) {
        LOG_ERROR("ServiceName2Json failed");
        return -1;
      }

      LOG_INFO("to regist when node exists"
                   << ", serviceName : " << orgService.serviceName
                   << ", appkey : " << oservice.appkey);
      shared_ptr<ZkSetRequest> zk_set_req = shared_ptr<ZkSetRequest>(new ZkSetRequest());
      zk_set_req->path = zkPath;
      zk_set_req->buffer = strOrgJson;
      zk_set_req->version = stat.version;   //set的版本号设置为get获取的版本号，不再强制覆盖
      ret = ZkClient::getInstance()->ZkSet(zk_set_req.get());
      //ret = ZkClient::getInstance()->zk_set(zkPath, strOrgJson.c_str(), strOrgJson.size(), -1);
      if (ZOK != ret || ret == -1) {
        LOG_FATAL("ERR zoo_set failed zkPath:" << zkPath
                                               << " szValue:" << strOrgJson.c_str()
                                               << " szValue size:" << strOrgJson.size()
                                               << "ret:" << ret);
        return ret;
      }
    } else if (ZNOAUTH == ret) {
      LOG_FATAL("ZNOAUTH in registerService. ret = " << ret);
      return ret;
    } else {
      LOG_FATAL("ERR other error: " << ret << " in registerService");
      return ret;
    }
  }

  return 0;
}

int RegisteZkClient::EditServiceName(SGService &desService,
                                     const SGService &srcService,
                                     int uptCmd) {
  switch (uptCmd) {
    case UptCmd::RESET:desService.serviceInfo = srcService.serviceInfo;
      break;
    case UptCmd::ADD:
      for (std::map<std::string, ServiceDetail>::const_iterator iter =
          srcService.serviceInfo.begin();
           iter != srcService.serviceInfo.end();
           ++iter) {
        desService.serviceInfo[iter->first] = iter->second;
      }
      break;
    case UptCmd::DELETE:
      for (std::map<std::string, ServiceDetail>::const_iterator iter =
          srcService.serviceInfo.begin();
           iter != srcService.serviceInfo.end();
           ++iter) {
        desService.serviceInfo.erase(iter->first);
      }
      break;
    default: LOG_ERROR("unknown uptCmd: " << uptCmd);
      return -1;
  }
  return 0;
}

bool RegisteZkClient::CheckLegal(const SGService &service) {
  if (ZkTools::whiteListMgr.IsAppkeyInRegistUnlimitWhitList(service.appkey)) {
    LOG_INFO("appkey: " << service.appkey << " is in regist unlimit white list");
    return true;
  }

  if (IsLimitOnZk(service)) {
    return CheckLegalOnOps(service);
  }
  return true;
}

bool RegisteZkClient::IsLimitOnZk(const SGService &service) {
  bool res = false;
  char zk_desc_path[MAX_BUF_SIZE] = {0};
  int ret = ZkTools::operation_.genDescZkPath(zk_desc_path, service.appkey);

  std::string str_json;
  shared_ptr<ZkExistsRequest> zk_exists_req = shared_ptr<ZkExistsRequest>(new ZkExistsRequest());
  zk_exists_req->path = zk_desc_path;
  zk_exists_req->watch = 0;
  ret = ZkClient::getInstance()->ZkExists(zk_exists_req.get());
  if (ZNONODE == ret) {
    LOG_ERROR("appkey desc is not exists, appkey = " << service.appkey
                                                     << ", env = " << g_global_var->gEnvStr);
    return false; //默认反馈合法
  } else if (ZOK == ret) { // 节点存在， 不需要进行合法验证
    int datalen = kZkContentSize;
    struct Stat stat;
    ZkGetRequest zk_get_req;
    ZkGetInvokeParams zk_get_param;
    zk_get_req.path = zk_desc_path;
    zk_get_req.watch = 0;
    zk_get_param.zk_get_request = zk_get_req;
    ret = ZkClient::getInstance()->ZkGet(&zk_get_param);
    if (ZOK != ret) {
      LOG_WARN("zoo_get origin content fail or zk handle is null, ret: " << ret
                                                                         << ", zkPath: " << zk_desc_path);
      return false;
    }
    str_json = zk_get_param.zk_get_response.buffer;
    datalen = zk_get_param.zk_get_response.buffer_len;
    stat = zk_get_param.zk_get_response.stat;

    cJSON *root = cJSON_Parse(str_json.c_str());
    if (NULL == root) {
      LOG_ERROR("failed to parse json: " << str_json);
      return false;
    }

    cJSON *pItem = cJSON_GetObjectItem(root, "regLimit");
    if (pItem) {
      int reg_limit = pItem->valueint;
      if (reg_limit == REG_LIMIT_LEGAL) {
        res = true;
      }
    }

    cJSON_Delete(root);
  }
  LOG_INFO("check appkey: " << service.appkey << " is reglimit: " << res);
  return res;
}

bool RegisteZkClient::CheckLegalOnOps(const SGService &service) {
  std::ifstream fi(g_global_var->gAppkeysFile.c_str());
  if (!fi.is_open()) {
    LOG_INFO("faild to open file: " << g_global_var->gAppkeysFile);
    fi.close();
    return true;
  }

  std::string appkey;
  while (std::getline(fi, appkey)) {
    LOG_INFO("appkey: " << appkey);
    if (appkey == service.appkey) {
      fi.close();
      return true;
    }
  }

  fi.close();
  LOG_INFO(service.appkey << " is not in appkeys");
  return false;

}
