#include "whitelist_mgr.h"
#include "comm/log4cplus.h"
#include "comm/tinyxml2.h"


int WhiteListMgr::Init() {
  tinyxml2::XMLDocument conf;
  tinyxml2::XMLError eResult =
      conf.LoadFile(sg_agent::SG_AGENT_MUTABLE_CONF.c_str());
  if (tinyxml2::XML_SUCCESS == eResult) {
    tinyxml2::XMLElement *pListElement =
        conf.FirstChildElement("SGAgentMutableConf");
    if (NULL != pListElement) {
      tinyxml2::XMLElement *noWatcherList =
          pListElement->FirstChildElement("NoWatcherWhiteLists");
      if (NULL != noWatcherList) {
        tinyxml2::XMLElement *item =
            noWatcherList->FirstChildElement("Item");
        while (NULL != item) {
          const char *appkey = item->GetText();
          LOG_INFO("add appkey: " << appkey
                                  << " to noWatcher whiteList");
          whiteList.insert(std::string(appkey));
          item = item->NextSiblingElement("Item");
        }
      } else {
        LOG_ERROR("no element: NoWatcherWhiteLists in mutable conf");
      }

      tinyxml2::XMLElement *registe_unlimit_list =
          pListElement->FirstChildElement("RegisteUnlimitWhiteList");
      if (NULL != registe_unlimit_list) {
        tinyxml2::XMLElement *item =
            registe_unlimit_list->FirstChildElement("Item");
        while (NULL != item) {
          const char *appkey = item->GetText();
          LOG_INFO("add appkey: " << appkey
                                  << " to regist unlimit list whiteList");
          registe_unlimit_whitelist_.insert(std::string(appkey));
          item = item->NextSiblingElement("Item");
        }
      } else {
        LOG_ERROR("no element: RegisteUnlimitWhiteList in mutable conf");
      }
    } else {
      LOG_ERROR("no element: SGAgentMutableConf in mutable conf");
    }
  } else {
    LOG_ERROR("Failed to load WhiteList, errno = " << eResult);
    return -1;
  }

  return 0;
}

bool WhiteListMgr::IsAppkeyInWhitList(
    const std::string &appkey) {
  std::set<std::string>::iterator it =
      whiteList.find(appkey);
  if (whiteList.end() == it) {
    return false;
  }
  return true;
}

bool WhiteListMgr::IsAppkeyInRegistUnlimitWhitList(
    const std::string &appkey) {
  std::set<std::string>::iterator it =
      registe_unlimit_whitelist_.find(appkey);
  if (registe_unlimit_whitelist_.end() == it) {
    return false;
  }
  return true;
}
