#include "gen_zk_path_operation.h"
#include "sg_agent_def.h"
#include "comm/log4cplus.h"
#include "comm/tinyxml2.h"
#include "SGAgentErr.h"
#include "../../../common/cpp/lib/boost_1_59_0/boost/algorithm/string/trim.hpp"



// the util classes avoid using global variables
int SGAgentZkPath::Init(const std::string &env) {
  // the env is checked, while sg_agent init.
  m_env = env;
  int ret = InitWhiteList();
	LOG_ERROR("debug the test ret"<<ret<<"white list"<<whitelist.empty());
  if (SUCCESS != ret || whitelist.empty()) {
    LOG_ERROR("loadWhiteListFail. ret = " << ret << ", size = " << whitelist.size());
    return FAILURE;
  }
  return SUCCESS;
}

static bool AppkeyContains(const std::string &appkey, const std::set<std::string> &sub_appkeys) {
  LOG_DEBUG("To Find appkey: " << appkey);
  for (std::set<std::string>::const_iterator it = sub_appkeys.begin(); sub_appkeys.end() != it; ++it) {
    LOG_DEBUG("whiteList's appkey: " << *it);
    std::size_t found = appkey.find(*it);
    if (found != std::string::npos) {
      LOG_INFO(appkey << " is in the appkey white list.");
      return true;
    }
  }
  return false;
}

bool SGAgentZkPath::IsAllEnvFileCfgAppkeys(const std::string &appkey) {
  return AppkeyContains(appkey, m_all_env_filecfg_appkeys);
}

bool SGAgentZkPath::IsInWhiteList(const std::string &appkey) {
  return AppkeyContains(appkey, whitelist);
}

/**
 *
 * @param mutable_ptr please make sure that this ptr is not NULL
 * @param parent
 * @param item
 * @param list the return value.
 * @return 0: SUCCESS
 */
int SGAgentZkPath::ParseWhiteList(tinyxml2::XMLElement *mutable_ptr,
                          const char *parent,
                          const char *item,
                          std::set<std::string> &list) {

  tinyxml2::XMLElement *parent_ptr = mutable_ptr->FirstChildElement(parent);
  if (NULL != parent_ptr) {
    tinyxml2::XMLElement *item_ptr = parent_ptr->FirstChildElement(item);
    while (NULL != item_ptr) {
      const char *item_char = item_ptr->GetText();
      if (NULL != item_char) {
        std::string item_str(item_char);
        boost::trim(item_str);
        if (!item_str.empty()) {
          list.insert(item_str);
        }
      } else {
        // ignore what can not be parsed and log it.
        LOG_WARN("can not identify the text of xml element " << item);
      }
      item_ptr = item_ptr->NextSiblingElement("Item");
    }
  } else {
    LOG_ERROR("can not find the xml element " << parent);
    return FAILURE;
  }
  return SUCCESS;
}


int SGAgentZkPath::InitWhiteList() {
  tinyxml2::XMLDocument conf;
  LOG_INFO("Start to read WhiteList");
  tinyxml2::XMLError eResult = conf.LoadFile(sg_agent::SG_AGENT_MUTABLE_CONF.c_str());

  if (tinyxml2::XML_SUCCESS == eResult) {
    LOG_INFO("Start to load whitelist");
    tinyxml2::XMLElement *mutable_ptr = conf.FirstChildElement("SGAgentMutableConf");
    if (NULL != mutable_ptr) {

      int ret = ParseWhiteList(mutable_ptr, "AllEnvWhiteLists", "Item", whitelist);
      if (SUCCESS != ret) {
        // already log inside.
				LOG_ERROR("ParseWhiteList error");
        return ret;
      }

      ret = ParseWhiteList(mutable_ptr, "FileConfigAllEnvWhiteLists", "Item", m_all_env_filecfg_appkeys);
      if (SUCCESS != ret) {
        // already log inside.
				LOG_ERROR("ParseWhiteList error aa");
        return ret;
      }

    } else {
      LOG_ERROR("Failed to load WhiteList, errno = " << eResult);
      return FAILURE;
    }
  }
	LOG_ERROR("return sucess");
  return SUCCESS;
}

int SGAgentZkPath::GetEnvByAppkeyWhiteList(std::string &env_str, const std::string &appkey) {
  if (appkey.empty()) {
    LOG_WARN("appkey cannot be empty.");
    return FAILURE;
  }

  if (IsInWhiteList(appkey)) {
    //在白名单内的appkey, 环境强制改成prod
    LOG_INFO("the appkey: " << appkey << ", current the env is: " << env_str << " change to prod");
    env_str = "prod";
  }
  return SUCCESS;
}

int SGAgentZkPath::GenNodeType(std::string &nodeType, const std::string &protocol) {
  //protocol变量
  if (protocol.empty()) {
    LOG_ERROR("protocol cannot be empty.");
    return FAILURE;
  } else {
    //新接口访问,thrift或者http服务
    if ("http" == protocol) {
      nodeType += "-http";
    } else if ("thrift" == protocol) {
      LOG_DEBUG("nodeType: " << nodeType << " not changed");
    } else {
      nodeType = nodeType + "s/" + protocol;
    }
    LOG_DEBUG("nodeType in newInterface: " << nodeType);
  }
  return SUCCESS;
}

int SGAgentZkPath::GenProtocolZkPath(char (&zkPath)[MAX_BUF_SIZE],
                                     const std::string &appkey,
                                     const std::string &protocol,
                                     std::string &node_type) {
  std::string current_env(m_env); // 因为GetEnvByAppkeyWhiteList会修改第一个参数，因此必须使用临时变量
  int ret = GetEnvByAppkeyWhiteList(current_env, appkey);
  if (SUCCESS != ret) {
    return ret;
  }

  ret = GenNodeType(node_type, protocol);
  if (SUCCESS != ret) {
    return ret;
  }

  ret =
      snprintf(zkPath,
               sizeof(zkPath),
               "/mns/sankuai/%s/%s/%s",
               current_env.c_str(),
               appkey.c_str(),
               node_type.c_str());
  if (ret <= 0) {
    LOG_ERROR("fail to generate appkey = " << appkey
                                           << ", protocol = " << protocol
                                           << ", nodeType = " << node_type);
    return FAILURE;
  }

  return SUCCESS;
}

int SGAgentZkPath::genRegisterZkPath(char (&zkPath)[MAX_BUF_SIZE],
                                     const std::string &appkey,
                                     const std::string &protocol,
                                     const int serverType) {
  std::string current_env(m_env);// 因为GetEnvByAppkeyWhiteList会修改第一个参数，因此必须使用临时变量
  int ret = GetEnvByAppkeyWhiteList(current_env, appkey);
  if (SUCCESS != ret) {
    return ret;
  }

  //获取nodeType
  std::string providerType = "provider";
  if ((!protocol.empty())) {
    if ("http" == protocol) {
      providerType += "-http";
    } else if ("thrift" == protocol) {
      LOG_DEBUG("thrift in newInterface: " << providerType);
    } else {
      providerType = providerType + "s/" + protocol;
    }
    LOG_INFO("provider in newInterface: " << providerType);
  } else {
    //为了应对前端未修改protocol的情况
    if (sg_agent::HTTP_TYPE == serverType) {
      providerType += "-http";
    }
    LOG_DEBUG("provider in oldInterface: " << providerType);
  }
  LOG_INFO("zkPath provider prefix: " << providerType);

  //拼接zkPath
  ret =
      snprintf(zkPath,
               sizeof(zkPath),
               "/mns/sankuai/%s/%s/%s",
               current_env.c_str(),
               appkey.c_str(),
               providerType.c_str());
  if (ret <= 0) {
    LOG_ERROR("gen registerService fail! appkey = " << appkey
                                                    << ", protocol = " << protocol
                                                    << ", nodeType = " << providerType
                                                    << ", serverType = " << serverType);
    return -1;
  }

  return 0;
}

int SGAgentZkPath::genServiceNameZkPath(char (&zkPath)[MAX_BUF_SIZE],
                                        const std::string &serviceName,
                                        const std::string &appkey) {
  // appkey可以为空
  std::string current_env(m_env);// 因为GetEnvByAppkeyWhiteList会修改第一个参数，因此必须使用临时变量
  GetEnvByAppkeyWhiteList(current_env, appkey);

  std::string new_servicename = ReplaceHttpServiceName(serviceName);
  //拼接zkPath
  int ret = snprintf(zkPath, sizeof(zkPath), "/mns/service/%s/%s", current_env.c_str(), new_servicename.c_str());
  if (ret <= 0) {
    LOG_ERROR("gen ServiceName Path fail! serviceName = "
                  << serviceName);
    return -1;
  }

  return 0;
}

int SGAgentZkPath::genServiceNameZkPathNode(char (&zkPath)[MAX_BUF_SIZE],
                                            const std::string &serviceName,
                                            const std::string &protocol,
                                            const std::string &appkey) {
  std::string current_env(m_env);// 因为GetEnvByAppkeyWhiteList会修改第一个参数，因此必须使用临时变量
  GetEnvByAppkeyWhiteList(current_env, appkey);
  std::string new_servicename = ReplaceHttpServiceName(serviceName);

  //拼接zkPath
  int ret = snprintf(zkPath,
                     sizeof(zkPath),
                     "/mns/service/%s/%s/%s",
                     current_env.c_str(),
                     new_servicename.c_str(),
                     protocol.c_str());
  if (ret <= 0) {
    LOG_ERROR("gen ServiceName Node fail! serviceName = "
                  << serviceName
                  << ", protocol = " << protocol);
    return -1;
  }

  return 0;
}

std::string SGAgentZkPath::ReplaceHttpServiceName(const std::string &servicename) {
  std::string result;
  for (int i = 0; i < servicename.size(); ++i) {
    // replace the / with ^
    result.push_back('/' == servicename[i] ? '^' : servicename[i]);
  }
  return result;
}

int SGAgentZkPath::genDescZkPath(char (&zkPath)[MAX_BUF_SIZE], const std::string &appkey) {

  //TODO:目前只存prod进行获取
  std::string current_env(m_env);// 因为GetEnvByAppkeyWhiteList会修改第一个参数，因此必须使用临时变量
  int ret = GetEnvByAppkeyWhiteList(current_env, appkey);
  if (SUCCESS != ret) {
    return ret;
  }

  //拼接zkPath
  ret = snprintf(zkPath, sizeof(zkPath), "/mns/sankuai/%s/%s/%s", "prod", appkey.c_str(), "desc");
  if (ret <= 0) {
    LOG_ERROR("gen desc fail! appkey = " << appkey << ", env = " << current_env);
    return FAILURE;
  }

  return SUCCESS;
}

