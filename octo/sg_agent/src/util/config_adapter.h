#ifndef SG_AGENT_CONFIG_ADAPTER_H
#define SG_AGENT_CONFIG_ADAPTER_H

/*
 * 功能：配置文件的动态适配
 * 实现：支持获取实时配置参数
 * */
#include <string>
#include "../comm/inc_comm.h"
#include "../comm/md5.h"
#include "muduo/net/EventLoop.h"
#include "muduo/net/EventLoopThread.h"
#include "boost/bind.hpp"
#include "boost/unordered_map.hpp"
#include "idc.h"
#include <boost/shared_ptr.hpp>
#include <sgcommon/event_loop_thread_proxy.h>
#include <boost/algorithm/string/trim.hpp>
#include "sg_agent_def.h"
#include "gen_zk_path_operation.h"
#include "sg_agent/regist_client.h"
#include "whitelist_mgr.h"
namespace sg_agent {
const static std::string idc_file_name = "idc.xml";
const static std::string agent_file_name = "sg_agent_mutable.xml";
const static std::string log4cplus_file_name = "log4cplus.conf";
const static std::string file_path = "/opt/meituan/apps/sg_agent/";
const static std::string idc_xml_file = "/opt/meituan/apps/sg_agent/idc.xml";
const static std::string agent_xml_file = "/opt/meituan/apps/sg_agent/sg_agent_mutable.xml";
const static std::string appenv_xml_file = "/opt/meituan/apps/appenv";

enum ConfigType {
  HOSTELTRAVELAPPKEYS,
  HOTELTRAVELREGISTERIDC
};

class ConfigAdapter {

 private:
  ConfigAdapter()
      : m_check_loop(NULL),
        m_hoteltravelappkeys_md5(""),
        m_hoteltravelregisteridc_md5(""){};
 public:
  ~ConfigAdapter() {};

  void Init();

  static ConfigAdapter *GetInstance();

 private:
  void CheckAllFile();
  bool CheckMutableXml(std::set<std::string> & appkey, std::vector<boost::shared_ptr<IDC> > &idcs);
  bool IsModify(const std::string &val, ConfigType type);
  bool Md5Check(const std::string &config, std::string &md5);
  bool IsModifyMutable(tinyxml2::XMLElement *pListElement,
                       const std::string &parent,
                       ConfigType type,
                       std::set<std::string> &appkey);

  bool IsModifyHostelIdc(tinyxml2::XMLElement *pListElement,
                                        std::vector<boost::shared_ptr<IDC> > &idcs);

  static ConfigAdapter *s_instance;
  static muduo::MutexLock s_cmutex;

  std::string m_hoteltravelappkeys_md5;
  std::string m_hoteltravelregisteridc_md5;

  muduo::net::EventLoopThread m_check_thread;
  muduo::net::EventLoop *m_check_loop;

};
}

#endif //SG_AGENT_CONFIG_ADAPTER_H
