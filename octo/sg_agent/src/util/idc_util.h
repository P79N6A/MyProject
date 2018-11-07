#ifndef UTIL_IP_UTIL_H_
#define UTIL_IP_UTIL_H_
#include <string>
#include <vector>
#include <boost/shared_ptr.hpp>
#include <muduo/base/Mutex.h>
#include "idc.h"
#include "../comm/inc_comm.h"
#include "../comm/md5.h"
#include "muduo/net/EventLoop.h"
#include "muduo/net/EventLoopThread.h"
#include "boost/bind.hpp"
#include <boost/shared_ptr.hpp>
#include "sg_agent_def.h"
namespace sg_agent {

class IdcUtil {
 public:
  static std::string UNKNOWN;
  static std::string NOCENTER;
  static boost::shared_ptr<IDC> GetIdc(const std::string &ip);
  static bool IsSameIdc(const boost::shared_ptr<IDC> &idc1, const boost::shared_ptr<IDC> &idc2);
  static bool IsSameCenter(const boost::shared_ptr<IDC> &idc1, const boost::shared_ptr<IDC> &idc2);
  static bool IsInitIdc() { return is_init_; }
  static std::string GetSameIdcZk(const char *zk_host, const std::string &ip);

  static void ReloadIdcCfg();
  static void Reset(boost::shared_ptr<std::vector<boost::shared_ptr<IDC> > > new_idcs);
  static void StartCheck();
  static void ResetModifiedFlag(bool reset) { is_config_modified = reset; }
  static int GetModifiedFlag() { return is_config_modified; }
 private:
  static boost::shared_ptr<std::vector<boost::shared_ptr<IDC> > > idcs_;
  static boost::shared_ptr<std::vector<boost::shared_ptr<IDC> > > new_idcs_;
  static muduo::MutexLock init_lock_;
  static muduo::MutexLock init_config_lock;
  static muduo::MutexLock s_swap_lock;
  static bool is_init_;
  static bool is_idc_xml_valid_;
  static bool is_idc_xml_check_;
  static bool is_thread_has_init;
  static bool is_config_has_init;
  static bool is_config_modified;
  static muduo::net::EventLoopThread m_config_check;
  static muduo::net::EventLoop *m_check_loop;
  static std::string m_config_content;
  static std::string m_config_md5;
  static std::string m_config_init_md5;

  static void CheckConfig();
  static void Init();
  static void LoadIdcXml(bool is_init);
  static void SetInitConfigMd5(const std::string& load_name,const std::string& load_path);
};

}
#endif
