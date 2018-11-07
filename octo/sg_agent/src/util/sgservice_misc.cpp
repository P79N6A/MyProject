#include <sstream>
#include <iostream>

#include <comm/inc_comm.h>
#include "sgservice_misc.h"

std::string SGServiceMisc::SGService2String(const SGService &sgservice) {
  std::string str_ret
      ("appkey:" + sgservice.appkey + " version:" + sgservice.version + " ip:"
           + sgservice.ip);

  std::stringstream ss;
  ss << sgservice.port;
  std::string str_tmp;
  ss >> str_tmp;
  ss.clear();

  str_ret.append(" port:" + str_tmp);

  ss << sgservice.weight;
  ss >> str_tmp;
  ss.clear();

  str_ret.append(" weight:" + str_tmp);

  ss << sgservice.status;
  ss >> str_tmp;
  ss.clear();

  str_ret.append(" status:" + str_tmp);

  ss << sgservice.role;
  ss >> str_tmp;
  ss.clear();

  str_ret.append(" role:" + str_tmp);

  ss << sgservice.envir;
  ss >> str_tmp;
  ss.clear();

  str_ret.append(" envir:" + str_tmp);

  ss << sgservice.lastUpdateTime;
  ss >> str_tmp;
  ss.clear();

  str_ret.append(" lastUpdateTime:" + str_tmp + " extend:" + sgservice.extend);

  ss << sgservice.fweight;
  ss >> str_tmp;
  ss.clear();

  str_ret.append(" fweight:" + str_tmp);

  ss << sgservice.serverType;
  ss >> str_tmp;

  return str_ret.append(" serverType:" + str_tmp);
}

bool SGServiceMisc::SGServiceCompare(const SGService &sgservice1,
                                     const SGService &sgservice2) {
  return (0 > SGServiceIpPortCompare(sgservice1, sgservice2));
}

int SGServiceMisc::SGServiceIpPortCompare(const SGService &sgservice1,
                                          const SGService &sgservice2) {
  std::stringstream ss;
  ss << sgservice1.port;
  std::string port1;
  ss >> port1;
  ss.clear();
  ss << sgservice2.port;
  std::string port2;
  ss >> port2;
  ss.clear();

  std::string ipport1 = sgservice1.ip;
  ipport1.append(port1);
  std::string ipport2 = sgservice2.ip;
  ipport2.append(port2);

  return ipport1.compare(ipport2);
}

bool SGServiceMisc::IsSGServiceEqual(const SGService &sgservice1,
                                     const SGService &sgservice2) {
  if (0 != sgservice1.ip.compare(sgservice2.ip)) {
    return false;
  }
  if (sgservice1.port != sgservice2.port) {
    return false;
  }
  if (sgservice1.fweight != sgservice2.fweight) {
    return false;
  }
  return !(sgservice1.status != sgservice2.status);
}

int SGServiceMisc::UpdateSvrList(const std::vector <SGService> &slist,
                                 std::vector <SGService> &dlist,
                                 std::vector <SGService> &addlist,
                                 std::vector <SGService> &dellist,
                                 std::vector <SGService> &chglist) {
  std::string str_appkey;
  std::vector<SGService>::const_iterator tmp_siter = slist.begin();
  while (tmp_siter != slist.end()) {
    LOG_DEBUG("appkey: " << tmp_siter->appkey
                         << " ip : " << tmp_siter->ip
                         << " port: " << tmp_siter->port);

    if (unlikely(str_appkey.empty())) {
      str_appkey.assign(tmp_siter->appkey); //store for end print
    }

    ++tmp_siter;
  }

  std::vector<SGService>::iterator tmp_diter = dlist.begin();
  while (tmp_diter != dlist.end()) {
    LOG_DEBUG("appkey: " << tmp_diter->appkey
                         << " ip : " << tmp_diter->ip
                         << " port: " << tmp_diter->port);
    ++tmp_diter;
  }

  std::vector<SGService>::const_iterator siter = slist.begin();
  std::vector<SGService>::iterator diter = dlist.begin();
  std::vector <SGService> reslist;
  while (siter != slist.end() && diter != dlist.end()) {
    int comp = SGServiceIpPortCompare(*siter, *diter);
    if (fb_status::ALIVE != siter->status) {
      LOG_DEBUG("appkey: " << siter->appkey
                           << " ip : " << siter->ip
                           << " port: " << siter->port
                           << " is not alive. It will be ignored");
      ++siter;
      continue;
    }
    if (0 > comp) {
      LOG_DEBUG("appkey: " << siter->appkey
                           << " ip : " << siter->ip
                           << " port: " << siter->port
                           << " is alive. It will be added");
      addlist.push_back(*siter);
      reslist.push_back(*siter);
      ++siter;
    } else if (0 < comp) {
      LOG_DEBUG("appkey: " << diter->appkey
                           << " ip : " << diter->ip
                           << " port: " << diter->port
                           << " is alive. It will be deleted");
      dellist.push_back(*diter);
      ++diter;
    } else {
      if (!IsSGServiceEqual(*siter, *diter)) {
        LOG_DEBUG("appkey: " << siter->appkey
                             << " ip : " << siter->ip
                             << " port: " << siter->port
                             << " is alive. It will be changed");
        chglist.push_back(*siter);
      }
      reslist.push_back(*siter);
      ++siter;
      ++diter;
    }
  }
  while (siter != slist.end()) {
    if (fb_status::ALIVE != siter->status) {
      LOG_DEBUG("appkey: " << siter->appkey
                           << " ip : " << siter->ip
                           << " port: " << siter->port
                           << " is not alive. It will be ignored");
      ++siter;
      continue;
    }
    LOG_DEBUG("appkey: " << siter->appkey
                         << " ip : " << siter->ip
                         << " port: " << siter->port
                         << " is alive. It will be added");
    addlist.push_back(*siter);
    reslist.push_back(*siter);
    ++siter;
  }
  while (diter != dlist.end()) {
    LOG_DEBUG("appkey: " << diter->appkey
                         << " ip : " << diter->ip
                         << " port: " << diter->port
                         << " is alive. It will be deleted");
    dellist.push_back(*diter);
    ++diter;
  }
  dlist = reslist;

  //print modify node
  std::vector<SGService>::const_iterator it = addlist.begin();
  while (it != addlist.end()) {
    LOG_INFO("appkey: " << it->appkey << " add node: " << it->ip << ":"
                        << it->port);
    ++it;
  }

  it = dellist.begin();
  while (it != dellist.end()) {
    LOG_INFO("appkey: " << it->appkey << " del node: " << it->ip << ":"
                        << it->port);

    ++it;
  }

  it = chglist.begin();
  while (it != chglist.end()) {
    LOG_INFO("appkey: " << it->appkey << " chg node: " << it->ip << ":"
                        << it->port);  //TODO chg detail

    ++it;
  }

  LOG_INFO("update appkey " << str_appkey << " svr list done, add node number: "
                            << addlist.size
                                () << " del node number: " << dellist
                                .size() <<
                            " chg node number: "
                            << chglist
                                .size());

  return 0;
}

bool SGServiceMisc::CheckVerifyCode(const std::string &verifyCode) {
  if ("agent.octo.sankuai.com" != verifyCode) {
    LOG_ERROR("The verifyCode is invalid.");
    return false;
  }
  return true;
}

void
SGServiceMisc::ChangeUnifiedProto2False(std::vector <SGService> *service_list_ptr) {
  for (std::vector<SGService>::iterator iter = service_list_ptr->begin();
       iter != service_list_ptr->end(); ++iter) {
    for (std::map<std::string, ServiceDetail>::iterator
             svr_info_iter = iter->serviceInfo.begin();
         svr_info_iter != iter->serviceInfo.end(); ++svr_info_iter) {
      svr_info_iter->second.unifiedProto = false;
    }

  }
}

void SGServiceMisc::ChangeUnifiedProto2FalseWithVersionCheck(
    std::vector <SGService> *service_list_ptr) {
  for (std::vector<SGService>::iterator iter = service_list_ptr->begin();
       iter != service_list_ptr->end(); ++iter) {
    if (IsMTthriftVersion(iter->version)) {
      LOG_DEBUG("remoteAppkey = " << iter->appkey
                                  << ", tansport is mtthrift, change unifiedProto to false. "
                                  << "IP: " << iter->ip
                                  << ", port: " << iter->port);
      for (std::map<std::string, ServiceDetail>::iterator
               svr_info_iter = iter->serviceInfo.begin();
           svr_info_iter != iter->serviceInfo.end(); ++svr_info_iter) {
        svr_info_iter->second.unifiedProto = false;
      }
    }
  }
}

bool SGServiceMisc::IsMTthriftVersion(const std::string &version) {
  if (version.empty()) {
    return false;
  }
  std::string strMtthrift("mtthrift");
  return (std::string::npos != version.find(strMtthrift));
}

