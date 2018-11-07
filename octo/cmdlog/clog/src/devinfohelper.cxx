#include <arpa/inet.h>
#include <ifaddrs.h>
#include <string.h>
#include <stdio.h>

#include <iostream>
#include <string>
#include <boost/algorithm/string.hpp>
#include <boost/property_tree/detail/xml_parser_error.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include <log4cplus/devinfohelper.h>

namespace cmdlog {

const std::string DevInfo::kStrOfficalEnvFileWithPath =
    "/data/webapps/appenv";
const std::string DevInfo::kStrOfficalEnvElement = "env";
const std::string DevInfo::kStrOfficalBackupEnvElement = "deployenv";

DevInfo::ONOFFLINE DevInfo::enum_onoffline_env_ = OFFLINE;

std::string DevInfo::str_env_;
std::string DevInfo::str_octo_env_("prod");
std::string DevInfo::str_local_ip_;
std::string DevInfo::str_host_;
std::string DevInfo::str_hostname_;

bool DevInfo::b_isMac_ = false;

//get local_ip, host, hostname, on/off line info, octo env info
DevInfo::DevInfo(void){
    GetIsMacAndHostIPInfo();//fill str_local_ip_, b_isMac_, str_host_

}

void DevInfo::replace_all_distinct(const std::string &old_value,
                                   const std::string &new_value,
                                   std::string *p_str) {
  for (std::string::size_type pos(0); pos != std::string::npos;
       pos += new_value.length()) {
    if ((pos = p_str->find(old_value, pos)) != std::string::npos)
      p_str->replace(pos, old_value.length(), new_value);
    else break;
  }
}

std::string DevInfo::strToLower(const std::string &str_tmp){
  std::string str_lower(str_tmp);
  transform(str_lower.begin(),str_lower.end(),str_lower.begin(), ::tolower);

  return str_lower;
}


void DevInfo::IntranetIp(char ip[INET_ADDRSTRLEN]) {
  struct ifaddrs *ifAddrStruct = NULL;
  struct ifaddrs *ifa = NULL;
  void *tmpAddrPtr = NULL;
  char addrArray[3][INET_ADDRSTRLEN];
  getifaddrs(&ifAddrStruct);
  int index = 0;
  for (ifa = ifAddrStruct; ifa != NULL; ifa = ifa->ifa_next) {
    if (!ifa->ifa_addr) {
      continue;
    }
    if (0 == strcmp(ifa->ifa_name, "vnic"))
      continue;
    if (ifa->ifa_addr->sa_family == AF_INET) { // check it is IP4
      tmpAddrPtr =
          &(reinterpret_cast<struct sockaddr_in *>(ifa->ifa_addr))->sin_addr;
      inet_ntop(AF_INET, tmpAddrPtr, addrArray[index], INET_ADDRSTRLEN);
      if (0 == strcmp(addrArray[index], "127.0.0.1"))
        continue;
      strcpy(ip, addrArray[index]);
      if (++index >= 2)
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

void DevInfo::GetIsMacAndHostIPInfo(void) {
  char ip[INET_ADDRSTRLEN] = {0};

  IntranetIp(ip);
  if (CLOG_UNLIKELY(0 == strlen(ip))) {
    TBSYS_LOG(DEBUG, "Cannot get local ip, wait 5 secs"); //local ip 10.32.159.91
	sleep(5000);
    IntranetIp(ip);
  }

  if (CLOG_UNLIKELY(0 == strlen(ip))) {
    str_local_ip_.assign("127.0.0.1");
  } else {
    str_local_ip_.assign(ip);
    TBSYS_LOG(DEBUG,"local ip %s" ,str_local_ip_.c_str());
  }

  char hostCMD[64];
  strncpy(hostCMD, "host ", 5);
  strncpy(hostCMD + 5, ip, INET_ADDRSTRLEN);

  FILE *fp = popen(hostCMD, "r");
  char hostInfo[256] = {0};

  if (CLOG_LIKELY(!fgets(hostInfo, 256, fp))) {
    int iRet = ferror(fp);
    if (CLOG_UNLIKELY(iRet)) {
      return;
    }
  }
  hostInfo[strlen(hostInfo) - 1] = '\0';  //del line token

  str_host_.assign(hostInfo);
  str_host_.assign(strToLower(str_host_));
  replace_all_distinct(" ", "%20", &str_host_);

  pclose(fp);
  memset(hostCMD, 0, sizeof(hostCMD));

  strncpy(hostCMD, "hostname ", 9);
  fp = popen(hostCMD, "r");
  char hostname[256] = {0};
  if (CLOG_LIKELY(!fgets(hostname, 256, fp))) {
    int iRet = ferror(fp);
    if (CLOG_UNLIKELY(iRet)) {
      TBSYS_LOG(ERROR, "fgets error, iRet %d",iRet);
      return;
    }
  }

  hostname[strlen(hostname) - 1] = '\0';  //del line token

  str_hostname_.assign(hostname);
  str_hostname_.assign(strToLower(str_hostname_));
  replace_all_distinct(" ", "%20", &str_hostname_);
  TBSYS_LOG(DEBUG, "host name: %s", hostname);//host name: dx-inf-imgsrv-staging01.dx.sankuai.com
  pclose(fp);

  b_isMac_ = CheckIfMac(str_host_);
}

void DevInfo::GetEnvFromOfficalEnvFile(void) throw(boost::property_tree::ptree_error) {
  boost::property_tree::ptree offical_env_tree;
  boost::property_tree::ini_parser::read_ini(kStrOfficalEnvFileWithPath,
                                             offical_env_tree); //may throw exception

  std::string env_str("");
  std::string deployenv_str("");
  try {
    env_str.assign(offical_env_tree.get<std::string>(kStrOfficalEnvElement));
    deployenv_str.assign(offical_env_tree.get<std::string>(kStrOfficalBackupEnvElement));
  } catch (boost::property_tree::ptree_error e) {
    TBSYS_LOG(ERROR,"fetch %s from appenv file failed, reason: cannot parse both env or deployenv!!! ", kStrOfficalEnvElement.c_str());
  }

  TBSYS_LOG(DEBUG,"env_str is %s, deploys_env_str is %s ",env_str.c_str(), deployenv_str.c_str());//str_env_ staging
  //环境识别 读取appenv文件
  //优先解析env字段
  //如果没有env字段或者env非法，则解析deployenv字段
  if (CLOG_LIKELY("prod" == env_str
              || "staging" == env_str)) {
        TBSYS_LOG(DEBUG, "env_str is %s env is online", env_str.c_str());
        enum_onoffline_env_ = ONLINE; 
  } else if (CLOG_LIKELY("dev" == env_str
              || "ppe" == env_str
              || "test" == env_str)) {
        TBSYS_LOG(DEBUG, "env_str is %s env is offline", env_str.c_str());
        enum_onoffline_env_ = OFFLINE;
  } else if (CLOG_UNLIKELY("product" == deployenv_str || "prod" == deployenv_str
              || "staging" == deployenv_str)) {
        TBSYS_LOG(DEBUG, "deployenv_str is %s env is online", deployenv_str.c_str());
        enum_onoffline_env_ = ONLINE;
  } else if (CLOG_UNLIKELY("dev" == deployenv_str || "alpha" == deployenv_str
              || "ppe" == deployenv_str || "prelease" == deployenv_str
              || "qa" == deployenv_str || "test" == deployenv_str)) {
        TBSYS_LOG(DEBUG, "deployenv_str is %s env is offline", deployenv_str.c_str());
        enum_onoffline_env_ = OFFLINE;
  } else {
        enum_onoffline_env_ = OFFLINE;
  }

}

bool DevInfo::isOnlineDev(void) {
    try {
      GetEnvFromOfficalEnvFile(); //evenif appenv fetch failed,
	// still can use empty string to compare, so not fetch the result
    } catch (boost::property_tree::ptree_error e) {
       TBSYS_LOG(ERROR,"fetch offical env file failed, reason: %s",e.what());
    }

	return enum_onoffline_env_ == ONLINE;
}

}
