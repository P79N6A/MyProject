//
// Created by Chao Shu on 16/3/6.
//

#ifndef CTHRIFT_CTHRIFT_SGAGENT_H
#define CTHRIFT_CTHRIFT_SGAGENT_H

#include <boost/property_tree/detail/xml_parser_error.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include "cthrift_common.h"

namespace cthrift {
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

struct IdcInfo {
  std::string str_ip_prefix; //still full ip
  std::string str_ip_mask;
  std::string str_center;

  IdcInfo(const std::string &str_ip_prefix_tmp,
          const std::string &str_ip_mask_tmp, const std::string
          &str_center_tmp) :
      str_ip_prefix(str_ip_prefix_tmp), str_ip_mask(str_ip_mask_tmp),
      str_center(str_center_tmp) { ; }

  bool IsSameNetSegment(const std::string &str_ip1) {
    int i_mask = mask_to_int(str_ip_mask);
    return (i_mask & get_ipv4(str_ip1)) == (i_mask & get_ipv4(str_ip_prefix));
  }

  std::string IdcInfo2String(void) const {
    return std::string(
        "ip_prefix: " + str_ip_prefix + " ip_mask: " + str_ip_mask + " center: "
            + str_center);
  }
};

typedef boost::unordered_map<std::string, boost::unordered_map<std::string,
                                                               std::vector<
                                                                   IdcInfo> > >
    MapRegionMapIdcInfo;
typedef boost::unordered_map<std::string, boost::unordered_map<std::string,
                                                               std::vector<
                                                                   IdcInfo> > >::iterator
    MapRegionMapIdcInfoIter;

class CthriftSgagent {
 private:
  static void IntranetIp(char ip[INET_ADDRSTRLEN]);
  static void GetIsMacAndHostIPInfo(void); //fill ip, isMac, host,hostname

  static void
  GetEnvFromOfficalEnvFile(void) throw(boost::property_tree::ptree_error);

  static bool CheckIfMac(const std::string &str_host) {
    return (std::string::npos != str_host.find("macbook")
        || std::string::npos != str_host.find("mac.local"));
  }

  enum ONOFFLINE {
    ONLINE,
    OFFLINE
  };

  static void GetOnOffLineInfo(void);
  static void InitIDCRegionInfo(void);

  static int8_t
  FetchRegionIDCOfIP(const std::string &str_ip,
                     std::string *p_str_region,
                     std::string
                     *p_str_idc, std::string
                     *p_str_center);

  static int8_t CheckIfSameRegionIDC(const string &str_region1,
                                     const string &str_idc1,
                                     const string &str_region2,
                                     const string &str_idc2,
                                     bool *p_b_is_same_region,
                                     bool *p_b_is_same_idc);

 public:
  static const std::string kStrSgagentAppkey;
  static const uint16_t kU16DefaultSgagentPort;

  static const std::string kStrOnlineSgagentSentinelHostURL;
  static const std::string kStrOnlineSgagentSentinelFullURL;

  static const std::string kStrOfflineSgagentSentinelHostURL;
  static const std::string kStrOfflineSgagentSentinelFullURL;

  static const double kDGetSvrListIntervalSecs;

  static const std::string kStrSgagentEnvFileWithPath;
  static const std::string kStrSgagentEnvElement;

  static const std::string kStrOfficalEnvFileWithPath;
  static const std::string kStrOfficalEnvElement;
  static const std::string kStrOfficalBackupEnvElement;

  static const double kDFirstRegionMin;
  static const double kDSecondRegionMin;

  static ONOFFLINE enum_onoffline_env_;

  static std::string str_env_;
  static std::string str_octo_env_;
  static std::string str_local_ip_;
  static bool b_isMac_;
  static bool b_is_open_mtrace_;
  static bool b_is_open_cat_;
  static bool b_is_open_sentinel_;
  static std::string str_host_;
  static std::string str_hostname_;

  static std::string str_sentinel_full_url_;
  static std::string str_sentinel_http_request_;
  static muduo::net::InetAddress sentinel_url_addr_;

  static const std::string kStrIDCRegionInfoFileFullPath;

  static MapRegionMapIdcInfo map_region_map_idc_info_;
  static std::string str_local_ip_region_;
  static std::string str_local_ip_idc_;
  static std::string str_local_ip_center_;
  static std::string str_svr_name_;

  CthriftSgagent(void) throw(TException);
  static void PackDefaultSgservice(const std::string &str_svr_appkey,
                                   const std::string &str_local_ip,
                                   const uint16_t &u16_port,
                                   SGService *p_sgservice);

  static std::string SGService2String(const SGService &sgservice);

  static int8_t ParseSentineSgagentList
      (const std::string &str_req, std::vector<SGService> *p_vec_sgservice);

  static double
  FetchOctoWeight(const double &fweight, const double &weight);

  static void OCTOInfo(void) {
    LOG_INFO << "env " << str_env_ << " octo env " << str_octo_env_ << " local "
        "ip " <<
             str_local_ip_ <<
             " "
                 "is "
                 "Mac " << b_isMac_ << " host " << str_host_ << " hostname "
             << str_hostname_ << " sentinel url " << str_sentinel_full_url_
             << " sentinel http request " << str_sentinel_http_request_;
  }

  static int8_t CheckIfSameRegionIDCWithLocalIP(const std::string &str_ip, bool
  *p_b_is_same_region, bool *p_b_is_same_idc);

  static int8_t CheckIfSameRegionIDCWithTwoIPs(const std::string &str_ip1,
                                               const std::string &str_ip2,
                                               bool *p_b_is_same_region,
                                               bool *p_b_is_same_idc);

  static int8_t GetLocateInfoByIP(const std::string &str_ip, LocateInfo
  *p_locate_info);

  static void SetIsOpenCat(const bool &b_is_open);
  static void SetIsOpenMtrace(const bool &b_is_open);
  static void SetIsOpenSentinel(const bool &b_is_open);

  static void
  GetEnvFromSgagentEnvFile(void) throw
  (boost::property_tree::ptree_error);
};

}

extern const cthrift::CthriftSgagent g_cthrift_sgagent;


#endif //CTHRIFT_CTHRIFT_SGAGENT_H