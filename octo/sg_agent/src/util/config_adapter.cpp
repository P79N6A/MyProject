#include "config_adapter.h"
namespace sg_agent {

muduo::MutexLock ConfigAdapter::s_cmutex;
ConfigAdapter *ConfigAdapter::s_instance = NULL;

ConfigAdapter *ConfigAdapter::GetInstance() {
  if (NULL == s_instance) {
    muduo::MutexLockGuard lock(s_cmutex);
    if (NULL == s_instance) {
      s_instance = new ConfigAdapter();
    }
  }
  return s_instance;
}

void ConfigAdapter::Init() {
  LOG_INFO("ConfigAdapter monitor init");
  m_check_loop=m_check_thread.startLoop();
  m_check_loop->runEvery(CHECK_MONITOR_TIME, boost::bind(&ConfigAdapter::CheckAllFile, this));
}

void ConfigAdapter::CheckAllFile() {
  std::set<std::string> appkey;
  std::vector<shared_ptr<IDC> > idc_appkey;
  if (CheckMutableXml(appkey, idc_appkey)) {
	RegistClient::getInstance()->ResetHoteltravelWhiteList(appkey, idc_appkey);
    LOG_INFO("hosttravelappkeylist has changed, reload again. ");
  }
}

bool ConfigAdapter::IsModify(const std::string &config, ConfigType type){
  bool ret = false;
  switch(type) {
    case HOSTELTRAVELAPPKEYS:
      ret = Md5Check(config, m_hoteltravelappkeys_md5);
      break;
    case HOTELTRAVELREGISTERIDC:
      ret = Md5Check(config, m_hoteltravelregisteridc_md5);
      break;
    default: break;
  }
  return ret;
}
/*
 * 对文件内容进行md5校验
 */
bool ConfigAdapter::Md5Check(const std::string &config, std::string &md5) {
  if (config.empty()) {
    LOG_WARN("config is empty.");
    return false;
  }
  std::string tmpMd5 = "";
  MD5 md5String(config);
  tmpMd5 = md5String.md5();
  if(!tmpMd5.empty() &&
      SUCCESS != md5.compare(tmpMd5)) {//compare md5（last）to tmpMd5（next）
    md5 = tmpMd5;
    return true;
  }
  return false;
}
/*
 * 检查单个字段内容是否改变
 */
bool ConfigAdapter::IsModifyMutable(tinyxml2::XMLElement *pListElement,
                                    const std::string &parent,
                                    ConfigType type,
                                    std::set<std::string> & appkey) {
  std::string config = "";
  if (SUCCESS == SGAgentZkPath::ParseWhiteList(pListElement, parent.c_str(), "Item", appkey) ) {
    for (std::set<std::string>::iterator iter = appkey.begin();
         appkey.end() != iter; iter++) {
      config += *iter;
    }
  }
  return IsModify(config, type);
}

/*
 * 检查mutable文件中的两个字段是否改变
 */
bool ConfigAdapter::CheckMutableXml(std::set<std::string> & appkey,
                                    std::vector<boost::shared_ptr<IDC> > &idcs) {

  tinyxml2::XMLDocument conf;
  tinyxml2::XMLError eResult =
      conf.LoadFile(sg_agent::SG_AGENT_MUTABLE_CONF.c_str());
  bool hoteltravel_appkeys_modify = false;
  bool hoteltravel_registeridc_modify= false;
  if (tinyxml2::XML_SUCCESS == eResult) {
    tinyxml2::XMLElement *pListElement =
        conf.FirstChildElement("SGAgentMutableConf");
    if (NULL != pListElement) {
      hoteltravel_appkeys_modify = IsModifyMutable(pListElement,
                                                   "HotelTravelAppkeys", HOSTELTRAVELAPPKEYS, appkey);
      hoteltravel_registeridc_modify = IsModifyHostelIdc(pListElement, idcs);
    } else {
      LOG_ERROR("no element: SGAgentMutableConf in mutable conf");
    }
  } else {
    LOG_ERROR("Failed to load mutablexml, errno = " << eResult);
  }
  return hoteltravel_appkeys_modify || hoteltravel_registeridc_modify;
}

/*
 * Parse and check HotelTravelRegisterIDC
 */
bool ConfigAdapter::IsModifyHostelIdc(tinyxml2::XMLElement *mutable_ptr,
                                     std::vector<boost::shared_ptr<IDC> > &idcs) {
  std::string config = "";
  const tinyxml2::XMLElement *hotel_idcs = mutable_ptr->FirstChildElement("HotelTravelRegisterIDC");
  if (NULL != hotel_idcs) {
    const tinyxml2::XMLElement *idc_item = hotel_idcs->FirstChildElement("Item");
    while (NULL != idc_item) {
      const tinyxml2::XMLElement *ip_ptr = idc_item->FirstChildElement("IP");
      const tinyxml2::XMLElement *mask_ptr = idc_item->FirstChildElement("MASK");
      if (NULL != ip_ptr && NULL != mask_ptr) {
        std::string ip_str(ip_ptr->GetText());
        std::string mask_str(mask_ptr->GetText());
        boost::trim(ip_str);
        boost::trim(mask_str);
        if ((!ip_str.empty()) && (!mask_str.empty())) {
          boost::shared_ptr<IDC> idc(new IDC());
          config += ip_str + mask_str;
          idc->set_ip(ip_str);
          idc->set_mask(mask_str);
          LOG_INFO("\t\tIP = " << ip_str << ", MASK = " << mask_str);
          idcs.push_back(idc);
        } else {
          LOG_WARN("\t\tIP or MASK is empty");
        }
      } else {
        LOG_WARN("\t\tIP or MASK miss.");
      }
      idc_item = idc_item->NextSiblingElement("Item");
    }
    return IsModify(config, HOTELTRAVELREGISTERIDC);
  } else {
    LOG_ERROR("can not find the xml element HotelTravelRegisterIDC.");
  }
  return false;
}

}


