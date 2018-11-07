//
// Created by Chao Shu on 16/3/6.
//
#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string/regex.hpp>
#include <boost/foreach.hpp>

#include <boost/property_tree/ini_parser.hpp>
#include <boost/property_tree/xml_parser.hpp>

#include "cthrift_sgagent.h"

using namespace std;
using namespace cthrift;

const string CthriftSgagent::kStrSgagentAppkey = "com.sankuai.inf.sg_agent";
const uint16_t CthriftSgagent::kU16DefaultSgagentPort = 5266;

const string
    CthriftSgagent::kStrOnlineSgagentSentinelHostURL = "mns.sankuai.com";

const string
    CthriftSgagent::kStrOnlineSgagentSentinelFullURL =
    "http://" + CthriftSgagent::kStrOnlineSgagentSentinelHostURL
        + "/api/servicelist";

const string
    CthriftSgagent::kStrOfflineSgagentSentinelHostURL =
    "mns.inf.test.sankuai.com";

const string CthriftSgagent::kStrOfflineSgagentSentinelFullURL =
    "http://" + CthriftSgagent::kStrOfflineSgagentSentinelHostURL
        + "/api/servicelist";

const double CthriftSgagent::kDGetSvrListIntervalSecs = 10.0;
const double CthriftSgagent::kDGetAuthTokenIntervalSecs = 10.0;

const string CthriftSgagent::kStrSgagentEnvFileWithPath =
    "/opt/meituan/apps/sg_agent/sg_agent_env.xml";

const string CthriftSgagent::kStrIDCRegionInfoFileFullPath =
    "/opt/meituan/apps/sg_agent/idc.xml";

const string CthriftSgagent::kStrSgagentEnvElement = "SGAgentConf.MnsPath";

const string CthriftSgagent::kStrOfficalEnvFileWithPath =
    "/data/webapps/appenv";

const string CthriftSgagent::kStrOfficalEnvElement = "env";
const string CthriftSgagent::kStrOfficalBackupEnvElement = "deployenv";

const double CthriftSgagent::kDFirstRegionMin = 1.0;
const double CthriftSgagent::kDSecondRegionMin = 0.001;

CthriftSgagent::ONOFFLINE
    CthriftSgagent::enum_onoffline_env_ = CthriftSgagent::OFFLINE;

string CthriftSgagent::str_env_("");
string CthriftSgagent::str_swimlane_("");
string CthriftSgagent::str_octo_env_("");
string CthriftSgagent::str_local_ip_;
bool CthriftSgagent::b_isMac_ = false;
bool CthriftSgagent::b_is_open_mtrace_ = true;
bool CthriftSgagent::b_is_open_cat_ = true;
bool CthriftSgagent::b_is_open_sentinel_ = true;
string CthriftSgagent::str_host_;
string CthriftSgagent::str_hostname_;
string CthriftSgagent::str_sentinel_full_url_;
string CthriftSgagent::str_sentinel_http_request_;
muduo::net::InetAddress CthriftSgagent::sentinel_url_addr_(80);

MapRegionMapIdcInfo CthriftSgagent::map_region_map_idc_info_[2];
boost::unordered_map<int, LocateInfo> CthriftSgagent::map_ip_locate_[2];
boost::unordered_set<int> CthriftSgagent::ip_mask_list_[2];
AtomicInt32 CthriftSgagent::s_atomic_spin_;
string CthriftSgagent::str_local_ip_region_;
string CthriftSgagent::str_local_ip_idc_;
string CthriftSgagent::str_local_ip_center_;
string CthriftSgagent::str_svr_name_;

const CthriftSgagent g_cthrift_sgagent;

//get local_ip, host, hostname, on/off line info, octo env info
CthriftSgagent::CthriftSgagent(void) throw
(TException) {
  GetIsMacAndHostIPInfo();//fill str_local_ip_, b_isMac_, str_host_

  int count = 0;
  while (0 != InitAppenv() && count++ < 10) {
    //如果/data/webapp/env解析结果出错，打印日志sleep 1min 重试，暂且不作exit处理。
    CLOG_STR_WARN("Init Appenv failed, sleep 1min and retry");
    sleep(60);
  }

  if (count >= 10) {
    CLOG_STR_ERROR("Init Appenv failed after try 10 times, exit. ");
    sleep(5);
    exit(-2);
  }

  string str_sentinel_host_url;
  if (ONLINE == enum_onoffline_env_) {
    str_sentinel_full_url_.assign(
        kStrOnlineSgagentSentinelFullURL);
    str_sentinel_host_url.assign(kStrOnlineSgagentSentinelHostURL);
  } else {
    str_sentinel_full_url_.assign(
        kStrOfflineSgagentSentinelFullURL);
    str_sentinel_host_url.assign(kStrOfflineSgagentSentinelHostURL);
  }

  CLOG_STR_DEBUG("str_sentinel_full_url_ " << str_sentinel_full_url_
            << " str_sentinel_host_url "
            << str_sentinel_host_url);

  if (!muduo::net::InetAddress::resolve(str_sentinel_host_url,
                                        &sentinel_url_addr_)) {   //ONLY fill
    // IP address
    CLOG_STR_DEBUG("resolve " << str_sentinel_full_url_
              << " failed"); //NOT quite since we may not need it if local sgagent work
  }

  CLOG_STR_DEBUG("sentinel ip port " << sentinel_url_addr_.toIpPort());
  CLOG_STR_DEBUG("str_env " << str_env_);

  str_sentinel_http_request_.assign(
      "GET /api/servicelist?appkey=com.sankuai.inf.sg_sentinel&env="
          + str_env_ +
          "&host=" + str_host_ + "&hostname="
          + str_hostname_ + "&ip="
          + str_local_ip_ + " HTTP/1.1\r\nHost: "
          + str_sentinel_host_url
          + "\r\n\r\n");

  CLOG_STR_DEBUG("str_sentinel_http_request_ " << str_sentinel_http_request_);

  InitIDCRegionInfo();
}

void CthriftSgagent::InitIDCRegionInfo(void) {
  boost::property_tree::ptree bPTree4IDCRegionFile;
  LocateInfo tmp_locate_info;
  string str_ip_addr;
  string str_mask;
  int int_mask;

  int idx_spin = (s_atomic_spin_.get() + 1) % 2;
  map_region_map_idc_info_[idx_spin].clear();
  map_ip_locate_[idx_spin].clear();
  ip_mask_list_[idx_spin].clear();

  try {
    boost::property_tree::xml_parser::read_xml(CthriftSgagent::kStrIDCRegionInfoFileFullPath,
                                               bPTree4IDCRegionFile);

    BOOST_FOREACH(boost::property_tree::ptree::value_type &v,
                  bPTree4IDCRegionFile.get_child("SGAgent")) { //loop every node under SGAgent
            if ("Region" == v.first) {
              BOOST_FOREACH(boost::property_tree::ptree::value_type &v_region, v
                  .second) {
                      tmp_locate_info.str_region.assign(v.second.get<string>("RegionName"));
                      boost::unordered_map<string, vector<IdcInfo> >
                          &map_idc2vecinfo =
                              map_region_map_idc_info_[idx_spin][tmp_locate_info.str_region];

                      if ("IDC" == v_region.first) {
                        //fetch center name，assume ONLY ONE sub node named
                        // "CenterName"
                        try {
                          tmp_locate_info.str_center.assign(v_region.second.get<string>(
                              "CenterName"));
                        } catch (boost::property_tree::ptree_error e) {
                          CLOG_STR_DEBUG("fetch CenterName failed, "
                              "reason: " << e.what() << ", if old "
                                        "idc file, it's ok");

                          tmp_locate_info.str_center.assign("UNKNOWN");  //Init
                        }
                        CLOG_STR_DEBUG("str_center " << tmp_locate_info.str_center);

                        tmp_locate_info.str_idc.assign(v_region.second.get<string>("IDCName"));
                        vector<IdcInfo>
                            &vec_idc_info =
                            map_idc2vecinfo[tmp_locate_info.str_idc]; //assume IDCName MUST exist and
                        // match!!

                        BOOST_FOREACH(boost::property_tree::ptree::value_type
                                          &v_idc, v_region.second) {
                                if ("Item" == v_idc.first) {
                                  str_ip_addr.assign(v_idc.second.get<string>("IP"));
                                  str_mask.assign(v_idc.second.get<string>("MASK"));
                                  vec_idc_info.push_back(IdcInfo(str_ip_addr,
                                                                  str_mask,
                                                                 tmp_locate_info.str_center));
				  int_mask = mask_to_int(str_mask);
                                  tmp_locate_info.mask = int_mask;
                                  map_ip_locate_[idx_spin][get_ipv4(str_ip_addr) & int_mask] = tmp_locate_info;
                                  (ip_mask_list_[idx_spin]).insert(int_mask);
                                }
                              }
                      }
                    }
            }
          }

  } catch (boost::property_tree::ptree_error
           e) {
    CLOG_STR_ERROR("fetch local idc info failed, reason: " << e.what()
              << ", please "
                  "make sure " << CthriftSgagent::kStrIDCRegionInfoFileFullPath
              << " "
                  "exist");
    return;
  }

  if (0 == s_atomic_spin_.get()) {
    map_region_map_idc_info_[0] = map_region_map_idc_info_[1];
    map_ip_locate_[0] = map_ip_locate_[1];
    ip_mask_list_[0] = ip_mask_list_[1];
  }

  //spin to next buffer
  s_atomic_spin_.increment();

  if (0 != FetchRegionIDCOfIP(str_local_ip_, &str_local_ip_region_,
                         &str_local_ip_idc_, &str_local_ip_center_)) {
    CLOG_STR_ERROR("fetch local ip region & idc & center info failed");
  } else {
    CLOG_STR_DEBUG("local ip " << str_local_ip_ << " in region: "
                               << str_local_ip_region_ << " idc: " << str_local_ip_idc_ << " "
                                       "center: " << str_local_ip_center_);
  }

  //print idc region info
  MapRegionMapIdcInfoIter it = map_region_map_idc_info_[idx_spin].begin();
  while (it != map_region_map_idc_info_[idx_spin].end()) {
    CLOG_STR_DEBUG("Region: " << it->first);

    boost::unordered_map<string, vector<IdcInfo> >::iterator it_idc_vec_info =
        (it->second).begin();
    while (it_idc_vec_info != (it->second).end()) {
      CLOG_STR_DEBUG(" Idc: " << it_idc_vec_info->first);

      vector<IdcInfo>::iterator it_vec = (it_idc_vec_info->second).begin();
      while (it_vec != (it_idc_vec_info->second).end()) {
        CLOG_STR_DEBUG("   " + it_vec->IdcInfo2String());

        ++it_vec;
      }

      ++it_idc_vec_info;
    }
    ++it;
  }

}

int8_t CthriftSgagent::CheckIfSameRegionIDC(const string &str_region1,
                                            const string &str_idc1,
                                            const string &str_region2,
                                            const string &str_idc2,
                                            bool *p_b_is_same_region,
                                            bool *p_b_is_same_idc) {
  *p_b_is_same_region = (str_region1 == str_region2);
  *p_b_is_same_idc = (str_idc1 == str_idc2);

  if (!(*p_b_is_same_region) && (*p_b_is_same_idc)) {
    CLOG_STR_ERROR("differ region (region1 " << str_region1
              << " compared region2 " << str_region2 << ") but in same "
                  "idc " << str_idc1);
    return -1;
  }

  return 0;
}


int8_t
CthriftSgagent::CheckIfSameRegionIDCWithTwoIPs(const string &str_ip1,
                                               const string &str_ip2,
                                               bool *p_b_is_same_region,
                                               bool *p_b_is_same_idc) {
  string str_region1;
  string str_idc1;
  string str_center1;
  if (0 != FetchRegionIDCOfIP(str_ip1, &str_region1,
                         &str_idc1, &str_center1)) {
    CLOG_STR_ERROR("fetch ip " << str_ip1 << " region & idc info failed");
    return -1;
  }

  string str_region2;
  string str_idc2;
  string str_center2;
  if (0 != FetchRegionIDCOfIP(str_ip2, &str_region2,
                         &str_idc2, &str_center2)) {
    CLOG_STR_ERROR("fetch ip " << str_ip2 << " region & idc info failed");
    return -1;
  }

  if (CheckIfSameRegionIDC(str_region1, str_idc1, str_region2, str_idc2,
                           p_b_is_same_region, p_b_is_same_idc)) {
    CLOG_STR_ERROR("CheckIfSameRegionIDC failed, ip1 " << str_ip1 << " ip2 " <<
              str_ip2);
    return -1;
  }

  CLOG_STR_DEBUG("ip1 " << str_ip1 << " compared ip " << str_ip2
            << " is_same_region " << *p_b_is_same_region << " is_same_idc " <<
            *p_b_is_same_idc);

  return 0;
}

int8_t
CthriftSgagent::CheckIfSameRegionIDCWithLocalIP(const string &str_ip, bool
*p_b_is_same_region, bool *p_b_is_same_idc) {
  string str_region;
  string str_idc;
  string str_center;
  if (0 != FetchRegionIDCOfIP(str_ip, &str_region,
                         &str_idc, &str_center)) {
    CLOG_STR_ERROR("fetch ip " << str_ip << " region & idc info failed");
    return -1;
  }

  if (str_local_ip_region_.empty() || str_local_ip_idc_.empty()) {
    CLOG_STR_ERROR("local ip " << str_local_ip_ << " has NO region & idc info");
    return -1;
  }

  if (CheckIfSameRegionIDC(str_region,
                           str_idc,
                           str_local_ip_region_,
                           str_local_ip_idc_,
                           p_b_is_same_region,
                           p_b_is_same_idc)) {
    CLOG_STR_ERROR("CheckIfSameRegionIDC failed, ip1 " << str_ip << " local ip  "
              << str_local_ip_);
    return -1;
  }

  CLOG_STR_DEBUG("local ip " << str_local_ip_ << " compared ip " << str_ip
            << " is_same_region " << *p_b_is_same_region << " is_same_idc " <<
            *p_b_is_same_idc);

  return 0;
}


int8_t CthriftSgagent::GetLocateInfo(const string &str_ip, LocateInfo
*p_locate_info) {
  if (CTHRIFT_UNLIKELY(!p_locate_info)) {
    CLOG_STR_ERROR("p_locate_info NULL");
    return -1;
  }

  int idx_spin = s_atomic_spin_.get() % 2;
  //*p_locate_info = locate_list[map_ip_locate[str_ip]];
  boost::unordered_map<int, LocateInfo>::iterator map_it;
  boost::unordered_set<int>::iterator it = (ip_mask_list_[idx_spin]).begin();
  for (; it != (ip_mask_list_[idx_spin]).end(); ++it) {
    map_it =  (map_ip_locate_[idx_spin]).find(get_ipv4(str_ip) & (*it));
    if ((map_ip_locate_[idx_spin]).end() != map_it &&
        (map_it->second).mask == *it) {
      *p_locate_info = map_it->second;
    }
  }

  CLOG_STR_DEBUG("ip " << str_ip << " locateinfo:" << p_locate_info->ToString());
  return 0;
}

int8_t CthriftSgagent::GetLocateInfoByIP(const string &str_ip, LocateInfo
*p_locate_info) {
  if (CTHRIFT_UNLIKELY(!p_locate_info)) {
    CLOG_STR_ERROR("p_locate_info NULL");
    return -1;
  }

  if (0!= FetchRegionIDCOfIP(str_ip,
                         &(p_locate_info->str_region),
                         &(p_locate_info->str_idc),
                         &(p_locate_info->str_center))) {
    CLOG_STR_ERROR("fetch ip " << str_ip << " region & idc info failed");
    return -1;
  }

  CLOG_STR_DEBUG("ip " << str_ip << " locateinfo:" << p_locate_info->ToString());
  return 0;
}


int8_t CthriftSgagent::FetchRegionIDCOfIP(const string &str_ip,
                                          string *p_str_region,
                                          string
                                          *p_str_idc, string *p_str_center) {
  int idx_spin = s_atomic_spin_.get() % 2;
  vector<IdcInfo>::iterator it_vec_idc_info;
  boost::unordered_map<string, vector<IdcInfo> >::iterator it_map_idc_vec_info;

  boost::unordered_map<string, boost::unordered_map<string, vector<IdcInfo> >
  >::iterator it = map_region_map_idc_info_[idx_spin].begin();

  while (it != map_region_map_idc_info_[idx_spin].end()) {
    //LOG_DEBUG << "Region: " << it->first;

    it_map_idc_vec_info = (it->second).begin();
    while (it_map_idc_vec_info != (it->second).end()) {
      //LOG_DEBUG << "IDC: " << it_map_idc_vec_info->first;

      it_vec_idc_info = (it_map_idc_vec_info->second).begin();
      while (it_vec_idc_info != (it_map_idc_vec_info->second).end()) {
        if (it_vec_idc_info->IsSameNetSegment(str_ip)) {
          p_str_region->assign(it->first);
          p_str_idc->assign(it_map_idc_vec_info->first);
          p_str_center->assign((it_map_idc_vec_info->second)[0].str_center);
          //assume every idc has ip segment

          CLOG_STR_DEBUG("ip " << str_ip << " in region " << *p_str_region << ""
              " idc " << *p_str_idc << " center " << *p_str_center);
          return 0;
        }

        ++it_vec_idc_info;
      }

      ++it_map_idc_vec_info;
    }

    ++it;
  }

  CLOG_STR_WARN("ip " << str_ip << " NOT hit idc info");
  return -1;
}

typedef enum {
    PROD, STAGING, DEV, PPE, TEST
} Appenv;

int8_t CthriftSgagent::InitAppenv() {
  string deployenv_str;
  string env_str;
  string swimlane_str;
  ifstream appenv_fin;
  try {
    appenv_fin.open(kStrOfficalEnvFileWithPath.c_str(), std::ios::in);
    if (!appenv_fin.is_open()) {
      CLOG_STR_ERROR("Failed to open /data/webapp/appenv, Maybe file is not exist" << kStrOfficalEnvFileWithPath);
      return -1;
    } else {
      string buffer_str;
      while (getline(appenv_fin,buffer_str) && (env_str.empty() || deployenv_str.empty() || swimlane_str.empty())) {
        size_t pos = buffer_str.find_first_of("=");
        if (string::npos != pos) {
          string key = buffer_str.substr(0, pos);
          string value = buffer_str.substr(pos + 1);
          boost::trim(key);
          boost::trim(value);
          if ("env" == key) {
            env_str = value;
            CLOG_STR_INFO("parsing env: " << buffer_str);
          } else if ("deployenv" == key) {
            deployenv_str = value;
            CLOG_STR_INFO("parsing deployenv: " << buffer_str);
          } else if ("swimlane" == key) {
            swimlane_str = value;
            CLOG_STR_INFO("swimlane deployenv: " << buffer_str);
          }
        }
        buffer_str.clear();
      }
    }
    appenv_fin.close();
  } catch (exception &e) {
    appenv_fin.close();
    CLOG_STR_ERROR("fail to load " <<kStrOfficalEnvFileWithPath
              << "OR fetch deployenv/appenv failed, reason: " << e.what());
    return -1;
  }
  transform(deployenv_str.begin(), deployenv_str.end(), deployenv_str.begin(), ::tolower);
  transform(env_str.begin(), env_str.end(), env_str.begin(), ::tolower);
  transform(swimlane_str.begin(), swimlane_str.end(), swimlane_str.begin(), ::tolower);
  CLOG_STR_INFO("get env = " << env_str <<
           ", deployenv = " << deployenv_str <<
           ", swimlane = " << swimlane_str <<
           " from " << kStrOfficalEnvFileWithPath);
  if (!swimlane_str.empty()) {
    str_swimlane_ = swimlane_str;
    CLOG_STR_INFO("set str_swimlane_ value: " << str_swimlane_);
  }

  CLOG_STR_INFO("start to parse the host env, env = " << env_str << ", deployenv = " << deployenv_str);
  Appenv mAppenv;
  // 优先解释env字段，无法解释时，再解释deployenv。请勿优化以下if语句
  if ("prod" == env_str) {
    mAppenv = PROD;
  } else if ("staging" == env_str) {
    mAppenv = STAGING;
  } else if ("dev" == env_str) {
    mAppenv = DEV;
  } else if ("ppe" == env_str) {
    mAppenv = PPE;
  } else if ("test" == env_str) {
    mAppenv = TEST;
  } else if ("product" == deployenv_str || "prod" == deployenv_str) {
    mAppenv = PROD;
  } else if ("staging" == deployenv_str) {
    mAppenv = STAGING;
  } else if ("dev" == deployenv_str || "alpha" == deployenv_str) {
    mAppenv = DEV;
  } else if ("ppe" == deployenv_str || "prelease" == deployenv_str) {
    mAppenv = PPE;
  } else if ("qa" == deployenv_str || "test" == deployenv_str) {
    mAppenv = TEST;
  }else{
    CLOG_STR_ERROR("str_env_ is empty, fetch from appenv file failed, Please contact with SRE to handle this problem. ");
    return -1;
  }
  switch(mAppenv){
    case PROD:
      str_env_ = "prod";
          str_octo_env_ = "prod";
          enum_onoffline_env_ = ONLINE;
          CLOG_STR_INFO("success to init host env = prod (online)");
          break;
    case STAGING:
      str_env_ = "stage";
          str_octo_env_ = "stage";
          enum_onoffline_env_ = ONLINE;
          CLOG_STR_INFO("success to init host env = staging (online)");
          break;
    case DEV:
      str_env_ = "prod";
          str_octo_env_ = "dev";
          enum_onoffline_env_ = OFFLINE;
          CLOG_STR_INFO("success to init host env = dev (offline)");
          break;
    case PPE:
      str_env_ = "stage";
          str_octo_env_ = "ppe";
          enum_onoffline_env_ = OFFLINE;
          CLOG_STR_INFO("success to init host env = ppe (offline)");
          break;
    case TEST:
      str_env_ = "test";
          str_octo_env_ = "test";
          enum_onoffline_env_ = OFFLINE;
          CLOG_STR_INFO("success to init host env = test (offline)");
          break;
    default:
        CLOG_STR_ERROR("fail to init host env.");
        return -1;
  }
  CLOG_STR_DEBUG("enum_onoffline_env_ " << enum_onoffline_env_);
  return 0;
}

void CthriftSgagent::PackDefaultSgservice(const string &str_svr_appkey,
                                          const string &str_local_ip,
                                          const uint16_t &u16_port,
                                          SGService *p_sgservice) {
  p_sgservice->__set_appkey(str_svr_appkey);
  p_sgservice->__set_version(cthrift::version);
  p_sgservice->__set_ip(str_local_ip);
  p_sgservice->__set_port(u16_port);
  p_sgservice->__set_weight(10);
  if (kStrSgagentAppkey == str_svr_appkey) {
    //针对sg_agent的状态，是客户端cthrift内部使用，不可能依靠Scanner修改状态，必须直接设置为"正常"状态
    p_sgservice->__set_status(2);
  } else {
    //对齐octo整体状态流程，一般服务启动时状态为"未启动"，等待Scanner探活后设置为"正常"状态。
    p_sgservice->__set_status(0);
  }
  p_sgservice->__set_lastUpdateTime(static_cast<int32_t>(time(0)));
  p_sgservice->__set_fweight(10.0);
  p_sgservice->__set_serverType(0);
  p_sgservice->__set_heartbeatSupport(2);

  if(!str_swimlane_.empty()){
      p_sgservice->__set_swimlane(str_swimlane_);
  }

  if(!str_svr_name_.empty()){
      CLOG_STR_DEBUG("User set Uniform Protocol  serverName :" << str_svr_name_);
      ServiceDetail detail;
      detail.__set_unifiedProto(true);
      map<string, ServiceDetail> serviceInfo;
      //str_svr_name_ 即package + service， 正确性由业务方进行保障
      serviceInfo.insert(make_pair(str_svr_name_,detail));
      p_sgservice->__set_serviceInfo(serviceInfo);
  }else{
      CLOG_STR_ERROR("User don't  set Uniform Protocol  serverName :" << str_svr_name_);
  }
}

string CthriftSgagent::SGService2String(const SGService &sgservice) {
  string str_ret
      ("appkey:" + sgservice.appkey + " version:" + sgservice.version + " ip:"
           + sgservice.ip);

  string str_tmp;

  try {
    str_ret.append(" port:" + boost::lexical_cast<string>(sgservice.port));
  } catch (boost::bad_lexical_cast &e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "sgservice.port : " << sgservice.port);
  }


  try {
    str_ret.append(
        " weight:" + boost::lexical_cast<string>(sgservice.weight));
  } catch (boost::bad_lexical_cast &e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "sgservice.weight : " << sgservice.weight);
  }

  try {
    str_ret.append(
        " status:" + boost::lexical_cast<string>(sgservice.status));
  } catch (boost::bad_lexical_cast &e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "sgservice.status : " << sgservice.status);
  }

  try {
    str_ret.append(" role:" + boost::lexical_cast<string>(sgservice.role));
  } catch (boost::bad_lexical_cast &e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "sgservice.role : " << sgservice.role);
  }

  try {
    str_ret.append(
        " envir:" + boost::lexical_cast<string>(sgservice.envir));
  } catch (boost::bad_lexical_cast &e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "sgservice.envir : " << sgservice.envir);
  }

  try {
    str_ret.append(" lastUpdateTime:"
                       + boost::lexical_cast<string>(sgservice.lastUpdateTime)
                       + " extend:" + sgservice.extend);
  } catch (boost::bad_lexical_cast &e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "sgservice.lastUpdateTime : " << sgservice.lastUpdateTime);
  }

  try {
    str_ret.append(
        " fweight:" + boost::lexical_cast<string>(sgservice.fweight));
  } catch (boost::bad_lexical_cast &e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "sgservice.fweight : " << sgservice.fweight);
  }

  try {
    str_ret.append(" serverType:"
                       + boost::lexical_cast<string>(sgservice.serverType));
  } catch (boost::bad_lexical_cast &e) {

    CLOG_STR_ERROR("boost::bad_lexical_cast :" << e.what()
              << "sgservice.serverType : " << sgservice.serverType);
  }

  str_ret.append(" swimlane:" + sgservice.swimlane);

  return str_ret;
}

void CthriftSgagent::IntranetIp(char ip[INET_ADDRSTRLEN]) {
  struct ifaddrs *ifAddrStruct = NULL;
  struct ifaddrs *ifa = NULL;
  void *tmpAddrPtr = NULL;
  int addrArrayLen = 32;
  char addrArray[addrArrayLen][INET_ADDRSTRLEN];
  getifaddrs(&ifAddrStruct);
  int index = 0;
  for (ifa = ifAddrStruct; ifa != NULL; ifa = ifa->ifa_next) {
    if (!ifa->ifa_addr) {
      continue;
    }
    if (0 == strcmp(ifa->ifa_name, "vnic"))
      continue;
    if (ifa->ifa_addr->sa_family == AF_INET) { // check it is IP4
      //tmpAddrPtr = &((struct sockaddr_in *) ifa->ifa_addr)->sin_addr;
      tmpAddrPtr =
          &(reinterpret_cast<struct sockaddr_in *>(ifa->ifa_addr))->sin_addr;
      inet_ntop(AF_INET, tmpAddrPtr, addrArray[index], INET_ADDRSTRLEN);
      if (0 == strcmp(addrArray[index], "127.0.0.1"))
        continue;
      strcpy(ip, addrArray[index]);
      if (++index >= addrArrayLen - 1)
        break;
    }
  }
  if (index > 1) {
    int idx = 0;
    while (idx < index) {
      if (NULL != strstr(addrArray[idx], "10.")
          && 0 == strcmp(addrArray[idx], strstr(addrArray[idx], "10."))) {
        strcpy(ip, addrArray[idx]);
      }
      idx++;
    }
  }
  if (ifAddrStruct != NULL)
    freeifaddrs(ifAddrStruct);
  return;
}

//fill ip, isMac, host, hostname
void CthriftSgagent::GetIsMacAndHostIPInfo(void) {
  char ip[INET_ADDRSTRLEN] = {0};

  IntranetIp(ip);
  if (CTHRIFT_UNLIKELY(0 == strlen(ip))) {
    CLOG_STR_WARN("Cannot get local ip, wait 5 secs");
    muduo::CurrentThread::sleepUsec(5000 * 1000);

    IntranetIp(ip);
  }

  if (CTHRIFT_UNLIKELY(0 == strlen(ip))) {
    CLOG_STR_WARN("After wait 5 secs, still cannot get local ip, set to be 127"
        ".0.0.1");
    str_local_ip_.assign("127.0.0.1");
  } else {
    str_local_ip_.assign(ip);
    CLOG_STR_INFO("local ip " << str_local_ip_);
  }

  char hostCMD[64] = {0};
  strncpy(hostCMD, "host ", 5);
  strncpy(hostCMD + 5, ip, INET_ADDRSTRLEN);

  FILE *fp = popen(hostCMD, "r");
  char hostInfo[256] = {0};

  if (CTHRIFT_LIKELY(!fgets(hostInfo, 256, fp))) {
    int iRet = ferror(fp);
    if (CTHRIFT_UNLIKELY(iRet)) {
      CLOG_STR_ERROR("fgets error, iRet " << iRet);
      pclose(fp);
      return;
    }
  }
  hostInfo[strlen(hostInfo) - 1] = '\0';  //del line token

  str_host_.assign(hostInfo);
  str_host_.assign(strToLower(str_host_));
  replace_all_distinct(" ", "%20", &str_host_);
  CLOG_STR_INFO("host info: " << hostInfo);

  pclose(fp);
  memset(hostCMD, 0, sizeof(hostCMD));

  strncpy(hostCMD, "hostname ", 9);
  fp = popen(hostCMD, "r");
  char hostname[256] = {0};
  if (CTHRIFT_LIKELY(!fgets(hostname, 256, fp))) {
    int iRet = ferror(fp);
    if (CTHRIFT_UNLIKELY(iRet)) {
      CLOG_STR_ERROR("fgets error, iRet " << iRet);
      pclose(fp);
      return;
    }
  }

  hostname[strlen(hostname) - 1] = '\0';  //del line token

  str_hostname_.assign(hostname);
  str_hostname_.assign(strToLower(str_hostname_));
  replace_all_distinct(" ", "%20", &str_hostname_);
  CLOG_STR_INFO("host name: " << hostname);
  pclose(fp);

  b_isMac_ = CheckIfMac(str_host_);
  CLOG_STR_DEBUG("is mac " << b_isMac_);
}

/*void CthriftSgagent::GetOnOffLineInfo(void) {
  if (CTHRIFT_LIKELY(!b_isMac_)
      && !boost::algorithm::contains(str_host_, ".corp.sankuai.com")
      && !boost::algorithm::contains(str_host_, ".office.mos")
      && (boost::algorithm::contains(str_host_, ".sankuai.com")
          || "product" == str_env_ || "staging" == str_env_
          || "stage" == str_env_)) {
    enum_onoffline_env_ = ONLINE;
  }
}*/

void CthriftSgagent::SetIsOpenCat(const bool &b_is_open)
{
  b_is_open_cat_ = b_is_open;
}

void CthriftSgagent::SetIsOpenMtrace(const bool &b_is_open)
{
  b_is_open_mtrace_ = b_is_open;
}

void CthriftSgagent::SetIsOpenSentinel(const bool &b_is_open)
{
    b_is_open_sentinel_ = b_is_open;
}

/*{"ret":200,"data":{"serviceList":[{"appkey":"com.sankuai.inf.sg_sentinel","version":"original","ip":"10.4.246.240","port":5266,"weight":10,"fweight":0.0,"status":2,"role":0,"env":3,"lastUpdateTime":1460009990,"extend":"","serverType":0},{"appkey":"com.sankuai.inf.sg_sentinel","version":"original","ip":"10.4.241.125","port":5266,"weight":10,"fweight":0.0,"status":2,"role":0,"env":3,"lastUpdateTime":1460035753,"extend":"","serverType":0},{"appkey":"com.sankuai.inf.sg_sentinel","version":"original","ip":"10.4.241.165","port":5266,"weight":10,"fweight":0.0,"status":2,"role":0,"env":3,"lastUpdateTime":1460009990,"extend":"","serverType":0}]}}*/

int8_t CthriftSgagent::ParseSentineSgagentList(const string &str_req,
                                               vector<SGService> *p_vec_sgservice) {
  rapidjson::Document reader;
  if ((reader.Parse(str_req.c_str())).HasParseError()) {
    CLOG_STR_WARN("json parse string " << str_req << " failed");
    return -1;    //maybe NOT error
  }

  rapidjson::StringBuffer buffer;
  rapidjson::Writer<rapidjson::StringBuffer> writer(buffer);
  reader.Accept(writer);
  CLOG_STR_DEBUG("receive body " << string(buffer.GetString()));

  int i_ret;
  if (FetchInt32FromJson("ret", reader, &i_ret)) {
    return -1;
  }
  CLOG_STR_DEBUG("i_ret " << i_ret);

  if (200 != i_ret) {
    CLOG_STR_ERROR("fetch santine sgagent list failed, i_ret " << i_ret);
    return -1;
  }

  rapidjson::Value::MemberIterator it_data;
  if (FetchJsonValByKey4Doc(reader, "data", &it_data)
      || !((it_data->value).IsObject())) {
    CLOG_STR_ERROR("FetchJsonValByKey4Doc Wrong OR data is NOT object");
    return -1;
  }

  rapidjson::Value &data_val = it_data->value;

  rapidjson::Value::MemberIterator it;
  if (FetchJsonValByKey4Val(data_val, "serviceList", &it)) {
    return -1;
  }

  rapidjson::Value &srv_list = it->value;
  if (!(srv_list.IsArray())) {
    CLOG_STR_ERROR("srv_list NOT array");
    return -1;
  }

  rapidjson::Document::MemberIterator itr_sing;
  for (rapidjson::Value::ValueIterator itr = srv_list.Begin();
       itr != srv_list.End(); ++itr) {
    SGService sgservice;
    const rapidjson::Value &srvlist_info = *itr;

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "appkey",
                              &itr_sing)
        || !((itr_sing->value).IsString())
        || "com.sankuai.inf.sg_sentinel" != string((itr_sing->value).GetString
            ())) {
      CLOG_STR_ERROR("appkey NOT EXIST OR NOT string OR NOT com.sankuai.inf.sg_sentinel");
      continue;
    }

    //sgservice.appkey.assign((itr_sing->value).GetString());
    sgservice.appkey.assign(CthriftSgagent::kStrSgagentAppkey); //replace to be sgagent appkey!!

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "version",
                              &itr_sing)
        || !((itr_sing->value).IsString())) {
      CLOG_STR_ERROR("version NOT EXIST OR NOT string");
      continue;
    }

    sgservice.version.assign((itr_sing->value).GetString());

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "ip",
                              &itr_sing)
        || !((itr_sing->value).IsString())) {
      CLOG_STR_ERROR("version NOT EXIST OR NOT string");
      continue;
    }

    sgservice.ip.assign((itr_sing->value).GetString());

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "port",
                              &itr_sing)
        || !((itr_sing->value).IsInt())) {
      CLOG_STR_ERROR("version NOT EXIST OR NOT int");
      continue;
    }

    sgservice.port = (itr_sing->value).GetInt();


    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "weight",
                              &itr_sing)
        || !((itr_sing->value).IsInt())) {
      CLOG_STR_ERROR("weight NOT EXIST OR NOT int");
      continue;
    }

    sgservice.weight = (itr_sing->value).GetInt();

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "status",
                              &itr_sing)
        || !((itr_sing->value).IsInt())) {
      CLOG_STR_ERROR("status NOT EXIST OR NOT int");
      continue;
    }

    sgservice.status = (itr_sing->value).GetInt();

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "role",
                              &itr_sing)
        || !((itr_sing->value).IsInt())) {
      CLOG_STR_ERROR("role NOT EXIST OR NOT int");
      continue;
    }

    sgservice.role = (itr_sing->value).GetInt();

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "env",
                              &itr_sing)
        || !((itr_sing->value).IsInt())) {
      CLOG_STR_ERROR("envir NOT EXIST OR NOT int");
      continue;
    }

    sgservice.envir = (itr_sing->value).GetInt();

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "lastUpdateTime",
                              &itr_sing)
        || !((itr_sing->value).IsInt())) {
      CLOG_STR_ERROR("lastUpdateTime NOT EXIST OR NOT int");
      continue;
    }

    sgservice.lastUpdateTime = (itr_sing->value).GetInt();

    if (-1
        == FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                                 "extend",
                                 &itr_sing)
        || !((itr_sing->value).IsString())) {
      CLOG_STR_ERROR("extend NOT EXIST OR NOT string");
      continue;
    }

    sgservice.extend = (itr_sing->value).GetString();

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "fweight",
                              &itr_sing)
        || !((itr_sing->value).IsDouble())) {
      CLOG_STR_ERROR("fweight NOT EXIST OR NOT double");
      continue;
    }

    sgservice.fweight = (itr_sing->value).GetDouble();

    if (FetchJsonValByKey4Val(const_cast<rapidjson::Value &>(srvlist_info),
                              "serverType",
                              &itr_sing)
        || !((itr_sing->value).IsInt())) {
      CLOG_STR_ERROR("serverType NOT EXIST OR NOT int");
      continue;
    }

    sgservice.serverType = (itr_sing->value).GetInt();
    CLOG_STR_DEBUG("sgservice content: " << SGService2String(sgservice));

    p_vec_sgservice->push_back(sgservice);
  }

  return 0;
}

double CthriftSgagent::FetchOctoWeight(const double &fweight,
                                       const double &weight) {
  return (!CheckDoubleEqual(fweight, weight)
      && !CheckDoubleEqual(fweight, static_cast<double>(0))) ? fweight : weight;
}

pthread_once_t CthriftSgagent::cat_once_ = PTHREAD_ONCE_INIT;
std::string CthriftSgagent::cat_appkey_ = "com.sankuai.inf.newct.client";

void CthriftSgagent::InitCat() {
  catClientInit(cat_appkey_.c_str());
  CLOG_STR_INFO("init catclient for sdk version collection");
}

void CthriftSgagent::VersionCollection(const std::string &type,
                                       const std::string &name) {
    logEvent(type.c_str(), name.c_str(), CAT_SUCCESS, NULL);
}
