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

#include <boost/algorithm/string/trim.hpp>
#include "util/idc_util.h"
#include "comm/log4cplus.h"
#include "util/SGAgentErr.h"
#include "zk_tools.h"
#include "zk_client.h"
#include "util/global_def.h"
#include "comm/inc_comm.h"

extern GlobalVar *g_global_var;

// 判断一个appkey是否需要加watcher
WhiteListMgr ZkTools::whiteListMgr;
//前端
SGAgentZkPath ZkTools::operation_;

std::string ZkTools::root_path_;

int ZkTools::Init(tinyxml2::XMLDocument &conf, const std::string &root_path) {
  int ret = operation_.Init(g_global_var->gEnvStr);
  if (SUCCESS != ret) {
    LOG_ERROR("zk_client load whitelist fail! ret = " << ret);
  }

  ret = whiteListMgr.Init();
  if (SUCCESS != ret) {
    LOG_ERROR("zk_client load nowatcherwhitelist fail! ret = " << ret);
  }

  size_t len = root_path.size();

  if (root_path.at(len - 1) == '/') {
    root_path_.assign(root_path, 0, len - 1);
  } else {
    root_path_ = root_path;
  }
  LOG_INFO("init zk root path " << root_path_);

  return InitZk(conf);
}

int ZkTools::InitZk(tinyxml2::XMLDocument &conf) {

  boost::shared_ptr<IDC> local_ip_idc = IdcUtil::GetIdc(g_global_var->gIp);
  std::string regionName = local_ip_idc ? local_ip_idc->get_region() : "beijing";
  if (IdcUtil::UNKNOWN == regionName || regionName.empty()) {
    regionName = "beijing";
  }
  LOG_INFO("identify the region name is " << regionName);

  const char *timeOut = conf.FirstChildElement("SGAgentMutableConf")
      ->FirstChildElement("Timeout")->GetText();
  const char *retryTime = conf.FirstChildElement("SGAgentMutableConf")
      ->FirstChildElement("Retry")->GetText();
  int timeout = atoi(timeOut);
  int retry = atoi(retryTime);

  //初始化ZkClient进行连接管理
  tinyxml2::XMLElement *xmlMnsHost = conf.FirstChildElement("SGAgentMutableConf")
      ->FirstChildElement("MnsHost");
  if (NULL == xmlMnsHost) {
    LOG_FATAL("miss Mnshost in sg_agent_mutable.xml");
    return ERR_CONFIG_PARAM_MISS;
  }
  tinyxml2::XMLElement *online_or_offline_element =
       xmlMnsHost->FirstChildElement(g_global_var->gIsOnline?"online":"offline");
  if(NULL == online_or_offline_element){
    LOG_ERROR("miss online or offline element in sg_agent_mutable.xml");
    return ERR_CONFIG_PARAM_MISS;
  }

  tinyxml2::XMLElement *xmlZkHost
      = online_or_offline_element->FirstChildElement(regionName.c_str());
  if (NULL == xmlZkHost) {
    LOG_FATAL("miss " + regionName + " in sg_agent_mutable.xml");
    return ERR_CONFIG_PARAM_MISS;
  }

  const char *zk_host = xmlZkHost->GetText();
  LOG_INFO("zk_region = " + regionName + ", zk_host = " << zk_host);
  std::string zk_str = IdcUtil::GetSameIdcZk(zk_host, g_global_var->gIp);
  boost::trim(zk_str);
  LOG_INFO("use zk host: " << zk_str);
  int ret = ZkClient::getInstance()->init(zk_str.c_str(), timeout, retry);
  if (ret != 0) {
    //ZK连接失败，直接报警，不退出sg_agent_worker
    LOG_FATAL("ERR init mns processor failed! zk_host: " << zk_host
                                                         << ", timeout : " << timeout
                                                         << ", retry : " << retry
                                                         << ", ret: " << ret);
  }
  return ret;
}

std::string ZkTools::get_root_path() {
  return root_path_;
}
