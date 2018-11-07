// =====================================================================================
// 
//       Filename:  mtconfig_server_client.cpp
// 
//    Description:  
// 
//        Version:  1.0
//       Revision:  none
// 
// =====================================================================================

#include "comm/tinyxml2.h"
#include "comm/log4cplus.h"
#include "mtconfig_server_client.h"
#include "util/sgagent_filter.h"
#include "util/SGAgentErr.h"
#include "sg_agent_conf.h"
#include "util/sg_agent_def.h"
#include <boost/algorithm/string/trim.hpp>

#include "mns/mns_iface.h"
#include "util/global_def.h"
#include "mns.h"

using namespace tinyxml2;
using namespace sg_agent;

extern GlobalVar *g_global_var;
extern MNS *g_mns;
bool MtConfigCollector::is_mcc_cluster_init = false;



boost::unordered_map<std::string, std::string> MtConfigCollector::m_bgAppkeyList;

int MtConfigCollector::deleteCollector(const std::string &host, int port) {
  //从m_collectorHandlerList中删除不可用节点
  std::vector<MtconfigCollectorInfo *>::iterator iter;
  for (iter = MtConfigCollector::m_collectorHandlerList.begin();
       iter != MtConfigCollector::m_collectorHandlerList.end(); ++iter) {
    if ((*iter)->m_host == host && (*iter)->m_port == port) {
      (*iter)->m_isavailable = false;
      SAFE_DELETE((*iter)->m_pHandler);
      SAFE_DELETE(*iter);
      iter = MtConfigCollector::m_collectorHandlerList.erase(iter);
      LOG_WARN("delete unavailable ip: " << host.c_str()
                                         << " port is : " << port);
      break;
    }
  }
  return 0;
}


MtConfigCollector::MtConfigCollector() : is_inited_(false){}
int MtConfigCollector::LoadMccCfg() {

  XMLDocument conf;
  tinyxml2::XMLError e_result = conf.LoadFile(SG_AGENT_MUTABLE_CONF.c_str());
  if (tinyxml2::XML_SUCCESS != e_result) {
    LOG_ERROR("failed to load mcc config: " << SG_AGENT_MUTABLE_CONF
                                            << ", errorCode = " << e_result);
    return FAILURE;
  }
  tinyxml2::XMLElement *xml_mcc_appkeys = conf.FirstChildElement("SGAgentMutableConf")->FirstChildElement("MccAppkeys");

  if (NULL == xml_mcc_appkeys) {
    LOG_ERROR("the xmlMccAppkeys is null");
    return FAILURE;
  }

  tinyxml2::XMLElement *xml_bg_name = xml_mcc_appkeys->FirstChildElement("BG");

  while (NULL != xml_bg_name) {
    tinyxml2::XMLElement *mcc_appkey = xml_bg_name->FirstChildElement("MccAppkey");
    std::string mcc_appkey_name = NULL != mcc_appkey ? mcc_appkey->GetText() : "";
    boost::trim(mcc_appkey_name);
    if (mcc_appkey_name.empty()) {
      LOG_ERROR("MccAppkey is empty!");
      xml_bg_name = xml_bg_name->NextSiblingElement("BG");
      continue;
    }
    tinyxml2::XMLElement *xml_item = xml_bg_name->FirstChildElement("BgAppkeys");
    while (NULL != xml_item) {

      tinyxml2::XMLElement *bg_appkey = xml_item->FirstChildElement("appkey");
      while (NULL != bg_appkey) {
        std::string bg_appkey_name = NULL != bg_appkey ? bg_appkey->GetText() : "";
        boost::trim(bg_appkey_name);
        if (bg_appkey_name.empty()) {
          LOG_ERROR("BgAppkey is empty!");
          bg_appkey = bg_appkey->NextSiblingElement("appkey");
          continue;
        }
        m_bgAppkeyList.insert(std::make_pair<std::string, std::string>(bg_appkey_name,mcc_appkey_name));
        bg_appkey = bg_appkey->NextSiblingElement("appkey");
      }
      xml_item = xml_item->NextSiblingElement("BgAppkeys");

    }
    xml_bg_name = xml_bg_name->NextSiblingElement("BG");
  }
  boost::unordered_map<std::string, std::string>::const_iterator iter;
  for (iter = m_bgAppkeyList.begin(); m_bgAppkeyList.end() != iter; ++iter) {
    LOG_INFO("mcc appkey = " << iter->second << "bg appkeys = " << iter->first);
  }
  is_mcc_cluster_init = true;
  return SUCCESS;
}

int MtConfigCollector::init() {
  if (!is_mcc_cluster_init) {
    if (0 != LoadMccCfg() ) {
      LOG_ERROR("load the mtconfig xml failed");
    }
  }
  XMLDocument conf;
  tinyxml2::XMLError eResult = conf.LoadFile(SG_AGENT_MUTABLE_CONF.c_str());
  if (tinyxml2::XML_SUCCESS != eResult) {
    LOG_ERROR("failed to load mcc config: " << SG_AGENT_MUTABLE_CONF.c_str()
                                            << ", errorCode = " << eResult);
    return FAILURE;
  }
  const char *remoteAppkey = conf.FirstChildElement("SGAgentMutableConf")
      ->FirstChildElement("MtConfigServerAppkey")->GetText();
  LOG_INFO("set mtconfig appkey: " << remoteAppkey);
  m_appkey = remoteAppkey;
  m_serviceList.clear();
  m_collectorHandlerList.clear();
  is_inited_ = true;

  return SUCCESS;
}

/*  
 *  获取配置信息，重试三次
 *  */
int MtConfigCollector::getConfigData(proc_conf_param_t &oparam) {
  int count = 0;
  int state = 999;

  do {
    state = getConfig(oparam);
    ++count;
  } while (state != sg_agent::MTCONFIG_OK && count < sg_agent::MTCONFIG_RETRY);
  oparam.__set_err(state);

  return state;
}

/*
 * 从mtconfig_server获取配置信息 
 */
int MtConfigCollector::getConfig(proc_conf_param_t &oparam) {
  int err = 0;
  ThriftClientHandler *pCollector = getOneCollector(err, oparam.appkey);
  if (!pCollector) {
    LOG_ERROR("ERR getConfigData failed, getOneCollector fail. err = " << err);
    return err;
  }

  //定义获取配置返回结构体
  ConfigDataResponse confRes;
  //定义请求参数结构体
  GetMergeDataRequest reqConf;
  reqConf.__set_appkey(oparam.appkey);
  reqConf.__set_env(oparam.env);
  reqConf.__set_path(oparam.path);
  reqConf.__set_version(oparam.version);
  reqConf.__set_requestIp(g_global_var->gIp);
  reqConf.__set_swimlane(oparam.swimlane);
  reqConf.__set_cell(oparam.cell);
  try {
    //调用mtconfig-server接口
    LOG_DEBUG("getMergeData input : appkey : " << reqConf.appkey
                                               << ", env : " << reqConf.env
                                               << ", path : " << reqConf.path
                                               << " , version : " << reqConf.version
                                               << ", localIp : " << reqConf.requestIp
                                               << ", swimlane: " << reqConf.swimlane
                                               << ", cell: " << reqConf.cell);

    //使用时强制类型转换成对应的对象
    void *pClient = pCollector->getClient();
    if (!pClient) {
      LOG_ERROR("get MtConfigServiceClient failed!");
      return ERR_GET_HANDLER_FAIL;
    }
    MtConfigServiceClient *ptr = reinterpret_cast<MtConfigServiceClient *>(pClient);
    ptr->getMergeData(confRes, reqConf);
    //设置返回conf
    oparam.__set_conf(confRes.configData.data);
    oparam.__set_version(confRes.configData.version);
  }
  catch (TException &e) {
    //异常关闭连接
    pCollector->closeConnection();
    //释放内存
    SAFE_DELETE(pCollector);
    LOG_ERROR("getMergeData catch error! msg: " << e.what()
                                                << ", appkey : " << reqConf.appkey
                                                << ", env : " << reqConf.env
                                                << ", path : " << reqConf.path
                                                << ", version : " << reqConf.version
                                                << ", localIp : " << reqConf.requestIp);
    CountRequest::GetInstance()->CountConfigReq(false);
    return ERR_GETMERGEDATA_TEXCEPTION;
  }
  CountRequest::GetInstance()->CountConfigReq(true);
  //关闭连接
  pCollector->closeConnection();
  //释放内存
  SAFE_DELETE(pCollector);

  //返回错误码code
  return confRes.code;
}

/**
 *  获取文件配置信息，重试三次
 **/
int MtConfigCollector::getFileConfig(file_param_t &oparam) {
  int count = 0;
  int state = 999;

  do {
    state = getFileConfigData(oparam);
    ++count;
  } while (state != sg_agent::MTCONFIG_OK && count < sg_agent::MTCONFIG_RETRY);

  return state;
}

/*
 * 从mtconfig_server获取文件配置信息 
 */
int MtConfigCollector::getFileConfigData(file_param_t &oparam) {
  int err = 0;
  ThriftClientHandler *pCollector = getOneCollector(err, oparam.appkey);
  if (!pCollector) {
    LOG_ERROR("ERR getFileConfigData failed, getOneCollector fail. err = " << err);
    return err;
  }

  file_param_t res_param;
  res_param.__set_appkey(oparam.appkey);
  res_param.__set_env(oparam.env);
  res_param.__set_path(oparam.path);
  res_param.__set_cmd(oparam.cmd);
  res_param.__set_configFiles(oparam.configFiles);
  res_param.__set_ip(oparam.ip);
  try {
    //调用mtconfig-server接口
    LOG_DEBUG("getFileConfig input: appkey = " << oparam.appkey
                                               << ", env = " << oparam.env
                                               << ", path = " << oparam.path
                                               << ", ip = " << oparam.ip);
    //使用时强制类型转换成对应的对象
    MtConfigServiceClient *ptr = static_cast<MtConfigServiceClient *>(pCollector->getClient());
    if (!ptr) {
      LOG_ERROR("static_cast MtConfigServiceClient failed!");
      SAFE_DELETE(pCollector);
      return ERR_GET_HANDLER_FAIL;
    }
    ptr->getFileConfig(res_param, oparam);
    //将参数设置到返回结构体
    oparam.__set_configFiles(res_param.configFiles);
    oparam.__set_err(res_param.err);
  }
  catch (TException &e) {
    //异常关闭连接
    pCollector->closeConnection();
    //释放内存
    SAFE_DELETE(pCollector);
    LOG_ERROR("getFileConfig catch error! msg: " << e.what()
                                                 << ", appkey = " << oparam.appkey
                                                 << ", env = " << oparam.env
                                                 << ", path = " << oparam.path
                                                 << ", ip = " << oparam.ip);
    CountRequest::GetInstance()->CountFConfigReq(false);
    return ERR_GETFILECONF_TEXCEPTION;
  }
  CountRequest::GetInstance()->CountFConfigReq(true);
  //关闭连接
  pCollector->closeConnection();
  //释放内存
  SAFE_DELETE(pCollector);

  //返回错误码code
  return oparam.err;
}

/*
 *  添加配置信息，重试三次
 *  */
int MtConfigCollector::setConfigData(proc_conf_param_t &oparam) {
  int count = 0;
  int state = 999;

  do {
    state = setConfig(oparam);
    ++count;
  } while (state != sg_agent::MTCONFIG_OK && count < sg_agent::MTCONFIG_RETRY);

  return state;
}

/*
 * 将配置信息添加到config-server
 *  */
int MtConfigCollector::setConfig(proc_conf_param_t &oparam) {
  int ret = 0;

  int err = 0;
  ThriftClientHandler *pCollector = getOneCollector(err, oparam.appkey);
  if (!pCollector) {
    LOG_ERROR("ERR setConfig failed, getOneCollector fail. err = " << err);
    return err;
  }

  try {
    //调用mtconfig-server接口, setData
    LOG_INFO("setData,  appkey: " << oparam.appkey
                                  << ", env : " << oparam.env
                                  << ", version : " << oparam.version
                                  << ", path : " << oparam.path
                                  << ", conf : " << oparam.conf);
    START_TIME
    //使用时强制类型转换成对应的对象
    MtConfigServiceClient *ptr = static_cast<MtConfigServiceClient *>(pCollector->getClient());
    if (!ptr) {
      LOG_ERROR("static_cast MtConfigServiceClient failed!");
      return ERR_GET_HANDLER_FAIL;
    }

    SetConfigRequest request;
    request.__set_appkey(oparam.appkey);
    request.__set_env(oparam.env);
    request.__set_path(oparam.path);
    request.__set_conf(oparam.conf);

    if (oparam.__isset.swimlane) {
      request.__set_swimlane(oparam.swimlane);
    }
    if (oparam.__isset.token) {
      request.__set_token(oparam.token);
    }
    if (oparam.__isset.cell) {
      request.__set_cell(oparam.cell);
    }
    SetConfigResponse response;
    ptr->setConfig(response, request);
    ret = response.code;

    if (ret != sg_agent::MTCONFIG_OK) {
      LOG_ERROR("setData failed! appkey: " << oparam.appkey
                                           << ", env : " << oparam.env
                                           << ", version : " << oparam.version
                                           << ", path : " << oparam.path
                                           << ", swimlane : " << oparam.swimlane
                                           << ", token : " << oparam.token
                                           << ", ret : " << ret
                                           << ", errMsg :" << response.errMsg
                                           << ", conf : " << oparam.conf);
    }
    char info[128] = {0};
    snprintf(info, sizeof(info), "setData ok! ret is : %d", ret);
    END_TIME(info)
  }
  catch (TException &e) {
    //异常关闭连接
    pCollector->closeConnection();
    //释放内存
    SAFE_DELETE(pCollector);
    LOG_ERROR("setData failed, error: " << e.what()
                                        << ", appkey: " << oparam.appkey
                                        << ", env : " << oparam.env
                                        << ", version : " << oparam.version
                                        << ", path : " << oparam.path
                                        << ", conf : " << oparam.conf);
    CountRequest::GetInstance()->CountConfigReq(false);
    return ERR_SETDATA_TEXCEPTION;
  }
  CountRequest::GetInstance()->CountConfigReq(true);
  //关闭连接
  pCollector->closeConnection();
  //释放内存
  SAFE_DELETE(pCollector);

  //返回setData返回码
  return ret;
}

/*
 *  同步共享内存中appkey, env , path到mtconfig-server
 */
int MtConfigCollector::syncRelation(proc_conf_param_t &param) {
  int count = 0;
  int state = -1;

  do {
    state = syncRelationToMtconfig(param);
    ++count;
  } while (state != sg_agent::MTCONFIG_OK && count < sg_agent::MTCONFIG_RETRY);

  return state;
}

int MtConfigCollector::syncRelationToMtconfig(proc_conf_param_t &param) {
  int ret = 0;

  int err = 0;

  if (param.appkey.empty()) {
    LOG_ERROR("appkey is empty!");
    return FAILURE;
  }
  if ( param.configNodeList.empty()) {
    LOG_ERROR("configNodeList is empty!");
    return FAILURE;
  }

  ThriftClientHandler *pCollector = getOneCollector(err, param.appkey);
  if (!pCollector) {
    LOG_ERROR("ERR syncRelationToMtconfig failed, getOneCollector fail! err = " << err);
    return err;
  }

  try {
    //使用时强制类型转换成对应的对象
    MtConfigServiceClient *ptr = static_cast<MtConfigServiceClient *>(pCollector->getClient());
    if (!ptr) {
      LOG_ERROR("static_cast MtConfigServiceClient failed!");
      return ERR_GET_HANDLER_FAIL;
    }

    //调用mtconfig-server接口,同步config配置节点关系
    ret = ptr->syncRelation(param.configNodeList, g_global_var->gIp);
    if (ret != sg_agent::MTCONFIG_OK) {
      LOG_ERROR("syncRelation fail! ret : " << ret
                                            << ", size: " << param.configNodeList.size());
    }
  }
  catch (TException &e) {
    //异常关闭连接
    pCollector->closeConnection();
    //释放内存
    SAFE_DELETE(pCollector);
    LOG_ERROR("syncRelation failed, error: " << e.what()
                                             << ", ret: " << ret
                                             << ", size: " << param.configNodeList.size());
    CountRequest::GetInstance()->CountConfigReq(false);
    return ERR_SYNCRELATION_TEXCEPTION;
  }
  CountRequest::GetInstance()->CountConfigReq(true);
  //关闭连接
  pCollector->closeConnection();
  //释放内存
  SAFE_DELETE(pCollector);

  return ret;
}

//新增配置同步接口
int MtConfigCollector::SyncFileConf(FileConfigSyncResponse &resparam, FileConfigSyncRequest &oparam) {

  int count = 0;
  int state = -1;

  do {
    state = SyncFileConfPeriodicToMtconfig(resparam, oparam);
    ++count;
  } while (state != sg_agent::MTCONFIG_OK && count < sg_agent::MTCONFIG_RETRY);

  return state;

}
int MtConfigCollector::SyncFileConfPeriodicToMtconfig(FileConfigSyncResponse &res, FileConfigSyncRequest &req) {

  int ret = 0;
  int err = 0;

  ThriftClientHandler *pCollector = getOneCollector(err, req.appkey);
  if (!pCollector) {
    LOG_ERROR("ERR syncFileConfPeriodicToMtconfig failed, getOneCollector fail! err = " << err);
    return err;
  }
  try {
    //使用时强制类型转换成对应的对象
    MtConfigServiceClient *ptr = static_cast<MtConfigServiceClient *>(pCollector->getClient());
    if (!ptr) {
      LOG_ERROR("static_cast MtConfigServiceClient failed!");
      return ERR_GET_HANDLER_FAIL;
    }

    //调用mtconfig-server接口,同步config配置节点关系
    res.code = FAILURE;
    ptr->syncFileConfig(res, req);
    if (sg_agent::MTCONFIG_OK != res.code) {
      LOG_ERROR("syncFileConfig fail! ret : " << ret
                                              << ", size: " << req.appkey << ",res code" << res.code);
    }
  }
  catch (TException &e) {
    //异常关闭连接
    pCollector->closeConnection();
    //释放内存
    SAFE_DELETE(pCollector);
    LOG_ERROR("syncFileConf failed, error: " << e.what()
                                             << ", ret: " << ret
                                             << ", size: " << req.appkey);
    CountRequest::GetInstance()->CountFConfigReq(false);
    return ERR_SYNCRELATION_TEXCEPTION;
  };
  CountRequest::GetInstance()->CountFConfigReq(true);
  //关闭连接
  pCollector->closeConnection();
  //释放内存
  SAFE_DELETE(pCollector);
  return ret;
}
/*
 * 对req_appkey进行mcc集群匹配
 */
int MtConfigCollector::GetMccClusterAppkey(std::string &req_appkey, std::string &mcc_appkey) {

  //use mcc_appkey default
  mcc_appkey = m_appkey;
  boost::trim(req_appkey);
  if (req_appkey.empty()) {
    LOG_WARN("get mcc cluster appkey is empty!");
    return FAILURE;
  }
  LOG_DEBUG("get mcc cluster appkey = " << req_appkey);

  boost::unordered_map<std::string, std::string>::iterator iter;
  for (iter = m_bgAppkeyList.begin();m_bgAppkeyList.end() != iter; iter++) {
    if (0 == req_appkey.find(iter->first)) {
      mcc_appkey = iter->second;
      LOG_DEBUG("using the cluster appkey .mcc appkeys = " << iter->second
                                                          << " ,bg appkeys has req appkey = "
                                                          << iter->first);
      return SUCCESS;
    }
  }
  LOG_DEBUG("using the default mtconfig appkey = " << mcc_appkey);
  return FAILURE;
}

//获取服务列表,放入m_serviceList中，
int MtConfigCollector::getServiceList(std::string &req_appkey, std::string& mcc_appkey) {


  bool is_common_cluter = (SUCCESS == GetMccClusterAppkey(req_appkey, mcc_appkey)) ?
                          false : true;//默认公共集群

  ProtocolRequest req;
  req.__set_localAppkey("sgAgent");
  req.__set_protocol("thrift");
  req.__set_remoteAppkey(mcc_appkey);
  std::vector<SGService> serviceList;
  //TODO:
  int ret = g_mns->GetMnsPlugin()->GetSrvList(serviceList, req, false, true, true);

  if (SUCCESS != ret) {
    LOG_ERROR("ERR getService list failed  when try to get mtconfig, ret: "
                  << ret << "is_common_cluter : " << is_common_cluter);
    return ERR_FAILEDTOGETCONFSERVLIST;
  }

  if (is_common_cluter && serviceList.empty()) {
    LOG_ERROR("ERR mtconfig service list return null" << "is_common_cluter : " << is_common_cluter);
    return ERR_SERVICELIST_NULL;
  }
  sg_agent::SGAgent_filter::filterUnAlive(serviceList);
  if (!is_common_cluter && serviceList.empty()) {
    req.__set_remoteAppkey(m_appkey);
    ret = g_mns->GetMnsPlugin()->GetSrvList(serviceList, req, false, true, true);
    if (SUCCESS != ret) {
      LOG_ERROR("ERR getService list failed  when try to get mtconfig, ret: "
                    << ret << "is_common_cluter : " << is_common_cluter);
      return ERR_FAILEDTOGETCONFSERVLIST;
    }

    if (serviceList.empty()) {
      LOG_ERROR("ERR mtconfig service list return null" << "is_common_cluter : " << is_common_cluter);
      return ERR_SERVICELIST_NULL;
    }
    sg_agent::SGAgent_filter::filterUnAlive(serviceList);
  }

  ret = sg_agent::SGAgent_filter::FilterWeight(serviceList, sg_agent::IdcThresHold);
  if (SUCCESS != ret) {
    LOG_DEBUG("result from IDC filte is empty");
    ret = sg_agent::SGAgent_filter::FilterWeight(serviceList, sg_agent::RegionThresHold);
    if (SUCCESS != ret) {
      LOG_WARN("result from Region filte is empty");
    }
  }
  //更新服务列表
  m_serviceList = serviceList;

  return SUCCESS;
}

ThriftClientHandler *MtConfigCollector::getOneCollector(int &err, std::string &req_appkey) {
  //获取mtconfig_server服务列表
  std::string mcc_appkey = "";
  int ret = getServiceList(req_appkey, mcc_appkey);
  if (SUCCESS != ret) {
    LOG_ERROR("getServiceList failed, ret" << ret);
    return NULL;
  }

  //若获取的为空，直接返回
  int handlerListSize = m_serviceList.size();
  if (handlerListSize <= 0) {
    LOG_ERROR("Appkey:" << req_appkey << " ,serviceList is empty");
    err = ERR_CONFSERVLIST_EMPTY;
    return NULL;
  }

  //随机选择一个server,创建连接
  int beginIndex = rand() % handlerListSize;
  int index = beginIndex;
  do {
    if ( fb_status::ALIVE  == m_serviceList[index].status ) {
      ThriftClientHandler *pCollector = new ThriftClientHandler();
      if (pCollector) {
        int ret = pCollector->init(m_serviceList[index].ip, m_serviceList[index].port, MTCONFIG);
        if ((SUCCESS == ret) && pCollector->m_transport->isOpen()) {
          m_serviceList.clear();
          return pCollector;
        } else {
          LOG_ERROR("MtConfig handler init failed! index = " << index
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
  m_serviceList.clear();
  LOG_ERROR("getOneCollector fail!");
  err = ERR_CONFSERV_CONNFAILED;
  return NULL;
}

