#include <algorithm>
#include <sstream>

#include "sgservice_manager.h"
#include "sgservice_misc.h"
#include "comm/inc_comm.h"
#include "comm/log4cplus.h"

SGServiceManager::SGServiceManager() {
}

SGServiceManager::~SGServiceManager() {
  svrList.clear();
}

int SGServiceManager::UpdateSvrList(std::vector <SGService> &vec_sgservice,
                                    std::vector <SGService> &vec_sgservice_add,
                                    std::vector <SGService> &vec_sgservice_del,
                                    std::vector <SGService> &vec_sgservice_chg) {
  std::vector<SGService>::const_iterator it_vec;

  if (unlikely(svrList.empty() && vec_sgservice.empty())) {
    LOG_WARN("Init svr list but empty srvlist");
  } else if (unlikely(0 == svrList.size())) {
    it_vec = vec_sgservice.begin();
    LOG_INFO("Init svr list for appkey " << it_vec->appkey);

    while (it_vec != vec_sgservice.end()) {
      if (unlikely(2 != it_vec->status)) {
        LOG_INFO("svr info: "
                     << SGServiceMisc::SGService2String(*(it_vec++)) << " IGNORED");
        continue;
      }

      svrList.push_back(*it_vec);
      LOG_INFO("appkey: " << it_vec->appkey << " add node: " << it_vec->ip << ":"
                          << it_vec->port);

      vec_sgservice_add.push_back(*it_vec++);
    }

    std::sort(svrList.begin(), svrList.end(), SGServiceMisc::SGServiceCompare);
  } else if (unlikely(0 == vec_sgservice.size())) {
    LOG_WARN("vec_sgservice is empty");

    vec_sgservice_del = svrList;
    std::vector<SGService>::const_iterator it_del = vec_sgservice_del.begin();
    while (it_del != vec_sgservice_del.end()) {
      LOG_INFO("appkey: " << it_del->appkey << " del node: " << it_del->ip <<
                                                                           ":"
                          << it_del->port);
      ++it_del;
    }

    svrList.clear();
  } else {
    std::sort(vec_sgservice.begin(), vec_sgservice.end(),
              SGServiceMisc::SGServiceCompare);
    SGServiceMisc::UpdateSvrList(vec_sgservice, svrList,
                                 vec_sgservice_add, vec_sgservice_del,
                                 vec_sgservice_chg);

  }
  return 0;
}

std::vector <SGService> SGServiceManager::getSvrList() {
  return svrList;
}

int SGServiceManager::getOneSvr(SGService &svr) {
  if (0 == svrList.size()) {
    LOG_ERROR("svrList is empty");
    return -1;
  }

  int index = rand() % svrList.size();
  svr = svrList[index];
  return 0;
}

void SGServiceManager::delOneSvr(const std::string &host, const int port) {
  std::vector<SGService>::iterator iter = svrList.begin();
  while (iter != svrList.end()) {
    if (host == iter->ip && port == iter->port) {
      svrList.erase(iter);
      LOG_INFO("delete from svrList; ip = " << host.c_str()
                                            << " port = " << port);
      break;
    }
    ++iter;
  }
}
