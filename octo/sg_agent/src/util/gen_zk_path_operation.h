#ifndef _gen_zk_path_H__
#define _gen_zk_path_H__

#include <comm/tinyxml2.h>
#include <muduo/base/Mutex.h>
#include "sgagent_common_types.h"
#include "sg_agent_def.h"
#include "idc.h"

using namespace sg_agent;

class SGAgentZkPath {
 public:
  int Init(const std::string &env);

  //根据传入的字段，拼接getServiceList zkPath路径
  int GenProtocolZkPath(char (&zkPath)[MAX_BUF_SIZE],
                        const std::string &appkey,
                        const std::string &protocol,
                        std::string &nodeType);

  //根据传入的字段，拼接registerService zkPath路径
  int genRegisterZkPath(char (&zkPath)[MAX_BUF_SIZE],
                        const std::string &appkey,
                        const std::string &protocol,
                        const int serverType);

  //根据传入的字段，拼接servicename -> appkey路径
  int genServiceNameZkPath(char (&zkPath)[MAX_BUF_SIZE],
                           const std::string &serviceName,
                           const std::string &appkey = "");

  //根据传入的字段，拼接servicename -> appkey节点路径
  int genServiceNameZkPathNode(char (&zkPath)[MAX_BUF_SIZE],
                               const std::string &serviceName,
                               const std::string &protocol,
                               const std::string &appkey = "");

  //根据传入的字段，拼接registerService zkPath路径
  int genDescZkPath(char (&zkPath)[MAX_BUF_SIZE], const std::string &appkey);

  int GetEnvByAppkeyWhiteList(std::string &env_str, const std::string &appkey);

  int GenNodeType(std::string &nodeType, const std::string &protocol);

  bool IsInWhiteList(const std::string &appkey);

  // check the argument appkey containing what is defined in m_all_env_filecfg_appkeys.
  bool IsAllEnvFileCfgAppkeys(const std::string &appkey);

  static int ParseWhiteList(tinyxml2::XMLElement *mutable_ptr,
                            const char *parent,
                            const char *item,
                            std::set<std::string> &list);
  std::string ReplaceHttpServiceName(const std::string &servicename);
 private:

  int InitWhiteList();

  std::set<std::string> whitelist;
  std::string m_env;

  // all env appkey list for file config. see sg_agent_mutable.xml
  std::set<std::string> m_all_env_filecfg_appkeys;

};

#endif
