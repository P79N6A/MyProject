#ifndef CMDLOG_DEV_INFO_H
#define CMDLOG_DEV_INFO_H

#include <arpa/inet.h>
#include <boost/property_tree/detail/xml_parser_error.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include <log4cplus/tblog.h>

#define CLOG_LIKELY(x)  (__builtin_expect(!!(x), 1))
#define CLOG_UNLIKELY(x)  (__builtin_expect(!!(x), 0))

namespace cmdlog {

class DevInfo {
private:
  static void IntranetIp(char ip[INET_ADDRSTRLEN]);
  static void GetIsMacAndHostIPInfo(void); //fill ip, isMac, host,hostname

  static void
  GetEnvFromOfficalEnvFile(void) throw(boost::property_tree::ptree_error);

  static bool CheckIfMac(const std::string &str_host) {
    return (std::string::npos != str_host.find("macbook")
        || std::string::npos != str_host.find("mac.local"));
  }

  static void replace_all_distinct(const std::string &old_value,
							const std::string &new_value,
							std::string *p_str);							
  static std::string strToLower(const std::string &str_tmp);

  enum ONOFFLINE {
      ONLINE,
      OFFLINE,
  };

public:
  static const std::string kStrOfficalEnvFileWithPath;
  static const std::string kStrOfficalEnvElement;
  static const std::string kStrOfficalBackupEnvElement;
  static std::string str_env_;
  static std::string str_octo_env_;
  static std::string str_local_ip_;
  static std::string str_host_;
  static std::string str_hostname_;
  static bool b_isMac_;
  static ONOFFLINE enum_onoffline_env_;
 
  DevInfo(void);
  bool isOnlineDev(void);
};
}
#endif
