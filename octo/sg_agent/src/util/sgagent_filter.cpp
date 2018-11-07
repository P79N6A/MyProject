// =====================================================================================
//
//      Filename:  sgagent_filter.h
//
//      Description:  对serviceList进行route过滤和backup过滤
//
//      Version:  1.0
//      Created:  2015-05-25
//      Revision:  none
//
//
// =====================================================================================

#include <sys/time.h>
#include <map>
#include "boost/lambda/lambda.hpp"
#include "sgagent_filter.h"
#include "sgagent_stat.h"
#include "idc_util.h"

#include "global_def.h"
namespace sg_agent {

extern int check_flag;
int SGAgent_filter::FilterWeight(std::vector<SGService> &serviceList, double thresholdWeight) {
  std::vector<SGService> serviceList_tmp;
  serviceList_tmp = serviceList;

  //删除weight = 0 的节点
  for (std::vector<SGService>::iterator iter = serviceList_tmp.begin();
       iter != serviceList_tmp.end();) {
    if (thresholdWeight >= iter->fweight) {
      LOG_DEBUG("filterWeight is 0, appkey = " << iter->appkey
                                               << " status : " << iter->status
                                               << " role : " << iter->role
                                               << " ip : " << iter->ip.c_str());
      iter = serviceList_tmp.erase(iter);
      continue;
    }
    ++iter;
  }

  if (serviceList_tmp.size() != serviceList.size()
      && serviceList_tmp.size() != 0) {
    serviceList = serviceList_tmp;
    return 0;
  }

  // 表示过滤后的结果为空， 保留原值
  return -1;
}

int SGAgent_filter::filterBackup(std::vector<SGService> &serviceList) {
  std::vector<SGService> serviceList_tmp;
  serviceList_tmp = serviceList;

  //正常情况，返回list删除backup节点
  for (std::vector<SGService>::iterator iter = serviceList_tmp.begin();
       iter != serviceList_tmp.end();) {
    if (iter->role != 0) {
      LOG_DEBUG("filterBackup, appkey = " << iter->appkey
                                          << " status : " << iter->status
                                          << " role : " << iter->role
                                          << " ip : " << iter->ip.c_str());
      iter = serviceList_tmp.erase(iter);
      continue;
    }
    ++iter;
  }

  //如果tmp中节点信息与原来节点数不同，且至少存在一个alive节点
  //则进行copy更新serviceList;
  //如果tmp节点数为0了， 或者没有alive节点
  //则返回原来所有节点，包括备份节点
  if (serviceList_tmp.size() != serviceList.size()
      && serviceList_tmp.size() != 0
      && hasAliveNode(serviceList_tmp)) {
    serviceList = serviceList_tmp;
  }

  return 0;
}

int SGAgent_filter::filterUnAlive(std::vector<SGService> &serviceList) {
  for (std::vector<SGService>::iterator iter = serviceList.begin();
       iter != serviceList.end();) {
    if (iter->status != fb_status::ALIVE) {
      LOG_INFO("filterUnAlive servicee is not alive, appkey = " << iter->appkey
                                                                << " status : " << iter->status
                                                                << " role : " << iter->role
                                                                << " ip : " << iter->ip.c_str());
      iter = serviceList.erase(iter);
      continue;
    }
    iter++;
  }
  return 0;
}

bool SGAgent_filter::hasAliveNode(std::vector<SGService> &serviceList) {
  for (std::vector<SGService>::iterator iter = serviceList.begin();
       iter != serviceList.end(); ++iter) {
    if (fb_status::ALIVE == iter->status) {
      return true;
    }
  }
  return false;
}

/*
 * 对于192.168.0.196:*这类host，过滤带port的host
 */
bool SGAgent_filter::RegexFilterPort(const std::string &regex_ip, const std::string &local_ip) {
  LOG_DEBUG("RegexFilterPort input param: regex_ip: " << regex_ip << " localIp: " << local_ip);
  //对于ip地址第4位以及端口进行切分,例如： 196:8890
  std::vector<std::string> regex_ip_vec;
  SplitStringIntoVector(regex_ip.c_str(), ":", regex_ip_vec);

  if (2 != regex_ip_vec.size()) {
    LOG_WARN("regex_ip =" << regex_ip << "is error");
    return false;
  }

  std::vector<std::string> local_ip_vec;
  SplitStringIntoVector(local_ip.c_str(), ":", local_ip_vec);

  if (RegexFilterIp(regex_ip_vec[0], local_ip_vec[0])) {
    if ("*" == regex_ip_vec[1] || "*\"" == regex_ip_vec[1] || regex_ip_vec[1] == local_ip_vec[1]) {
      LOG_INFO("filter ip OK, filter port OK!");
      return true;
    }
  }

  return false;
}

/**
 * 本地机房过滤， 使用localIp的前两位与Provider（reIp）的前两位比较
 * 相等， 表示匹配； 否则丢弃
 */
int SGAgent_filter::FilterIdcOrCenter(std::vector<SGService> &serviceList,
                                      const std::string &localIp,
                                      bool routeLimit,
                                      bool is_idc) {
  // 本地机房， 并且处于可用状态

  std::vector<SGService> serviceList_tmp = serviceList;
  int locAliveSize = FilterRange(serviceList_tmp, localIp,
                                 routeLimit, true, is_idc);

  // 如果没有同机房， 并且非强制， 则返回全部结果
  if (0 == locAliveSize && !routeLimit) {
    LOG_DEBUG("locAliveSize = 0, return all res");
    serviceList_tmp = serviceList;
    FilterRange(serviceList_tmp, localIp,
                routeLimit, false, is_idc);
  }

  serviceList = serviceList_tmp;
  return 0;
}

/**
 * Region&IDC区分
 * routeLimit: 是否强制；
 * needWeightChange: 为兼容老版本， 当本IDC无服务时， 将其他节点weight值保持原值
 */
int SGAgent_filter::FilterRange(std::vector<SGService> &serviceList,
                                const std::string &localIp,
                                bool routeLimit, bool needWeightChange, bool is_idc) {
  // 本地机房， 并且处于可用状态
  int aliveSize = 0;
  LOG_INFO("the idc config flag is ="<<IdcUtil::GetModifiedFlag());
	if(IdcUtil::GetModifiedFlag()){
			LOG_INFO("reload the idc xml");
			IdcUtil::ReloadIdcCfg();
	}

	boost::shared_ptr<IDC> local_idc = IdcUtil::GetIdc(localIp);
  if (!local_idc) {
    LOG_ERROR("fail to the idc info of local ip = " << localIp);
    return -1;
  }
  std::vector<SGService>::iterator iter = serviceList.begin();
  while (serviceList.end() != iter) {

    boost::shared_ptr<IDC> idc_item = IdcUtil::GetIdc(iter->ip);
    if (!idc_item) {
      ++iter;
      continue;
    }

    if (is_idc && IdcUtil::IsSameIdc(local_idc, idc_item)) {
      // same idc
      if (fb_status::ALIVE == iter->status) {
        ++aliveSize;
      }
    } else if (!is_idc && IdcUtil::IsSameCenter(local_idc, idc_item)) {
      // same center
      if (fb_status::ALIVE == iter->status) {
        ++aliveSize;
      }
    } else if (local_idc->get_region() == idc_item->get_region()) {
      // same region
      if (!routeLimit) {
        if (needWeightChange) {
          iter->weight = 0;
        }
        iter->fweight *= SameRegionMod;
        LOG_DEBUG("fileterRange IP: " << iter->ip
                                      << ", weight: " << iter->weight
                                      << ", fweight: " << iter->fweight);
      } else {
        iter = serviceList.erase(iter);
        continue;
      }
    } else {
      // cross region
      if (!routeLimit) {
        if (needWeightChange) {
          iter->weight = 0;
        }
        iter->fweight *= DiffRegionMod;
        LOG_DEBUG("fileterRange IP: " << iter->ip
                                      << ", weight: " << iter->weight
                                      << ", fweight: " << iter->fweight);
      } else {
        iter = serviceList.erase(iter);
        continue;
      }
    }
    ++iter;
  }
  return aliveSize;
}

bool SGAgent_filter::FilterHost(const std::string &regex_ip, const std::string &local_ip) {
  LOG_DEBUG("FilterHost input param: re_ip: " << regex_ip << " localIp: " << local_ip);
  //首先判断host格式是否为，192.168.1.196:8890
  std::vector<std::string> re_ip_vec;
  SplitStringIntoVector(regex_ip.c_str(), ":", re_ip_vec);

  //对于格式为, ip:port的host，单独过滤下port
  if (2 == re_ip_vec.size()) {
    if (RegexFilterPort(regex_ip, local_ip)) {
      LOG_INFO("RegexFilterPort OK! filter host is: " << regex_ip << " local host is: " << local_ip);
      return true;
    }
  } else {
    if (RegexFilterIp(regex_ip, local_ip)) {
      LOG_INFO("RegexFilterIp OK! filter IP is: " << regex_ip << " local IP is: " << local_ip);
      return true;
    }
  }

  return false;
}

bool SGAgent_filter::RegexFilterIp(const std::string &reIp, const std::string &localIp) {
  //对于格式仅为IP的，分组ip中带*号的ip进行过滤
  std::vector<std::string> vecLocal;
  SplitStringIntoVector(localIp.c_str(), ".", vecLocal);

  std::vector<std::string> vecRe;
  SplitStringIntoVector(reIp.c_str(), ".", vecRe);

  if (vecRe.size() == 1) {
    if (vecRe[0] == "\"*\"" or vecRe[0] == "*" or vecRe[0] == "\"*")
      return true;
  } else if (vecRe.size() == 2) {
    if ((((vecRe[0] == "\"*") or (vecRe[0] == "*"))
        and (vecRe[1] == "*" or vecRe[1] == "*\""))
        or ((vecRe[0] == vecLocal[0])
            and ((vecRe[1] == "*\"") or (vecRe[1] == "*"))))
      return true;
  } else if (vecRe.size() == 3) {
    if (((vecRe[2] == "*\"" or vecRe[2] == "*")
        and (vecRe[0] == vecLocal[0])
        and (vecRe[1] == vecLocal[1]))
        or ((vecRe[2] == "*\"" or vecRe[2] == "*")
            and (vecRe[0] == vecLocal[0])
            and (vecRe[1] == "*"))
        or ((vecRe[2] == "*\"" or vecRe[2] == "*")
            and (vecRe[0] == "*" or vecRe[0] == "\"*")
            and (vecRe[1] == "*")))
      return true;
  } else if (vecRe.size() == 4) {
    if (((vecRe[3] == "*\"" or vecRe[3] == "*"
        or vecRe[3] == vecLocal[3])
        and (vecRe[0] == vecLocal[0])
        and (vecRe[1] == vecLocal[1])
        and (vecRe[2] == vecLocal[2]))
        or ((vecRe[3] == "*\"" or vecRe[3] == "*"
            or vecRe[3] == vecLocal[3])
            and (vecRe[0] == vecLocal[0])
            and (vecRe[1] == vecLocal[1])
            and (vecRe[2] == "*"))
        or ((vecRe[3] == "*\"" or vecRe[3] == "*"
            or vecRe[3] == vecLocal[3])
            and (vecRe[0] == vecLocal[0])
            and (vecRe[1] == "*")
            and (vecRe[2] == "*"))
        or ((vecRe[3] == "*\"" or vecRe[3] == "*"
            or vecRe[3] == vecLocal[3])
            and ((vecRe[0] == "*" or vecRe[0] == "\"*"))
            and (vecRe[1] == "*")
            and (vecRe[2] == "*")))
      return true;
  } else {
    LOG_WARN("route consumer ip is: " << reIp.c_str()
                                      << ", localIp is: " << localIp.c_str());
    return false;
  }

  return false;
}

//consume列表过滤服务分组
bool SGAgent_filter::IsMatchConsumer(const CRouteData &route, const std::string &local_ip) {

  //一期实现基于localIp过滤consume,appkey过滤暂时不考虑
  std::vector<std::string> ips = route.consumer.ips;
  for (std::vector<std::string>::const_iterator iter = ips.begin(); ips.end() != iter; ++iter) {
    if ((*iter == local_ip) || FilterHost(*iter, local_ip)) {
      LOG_DEBUG("filterConsumer use ip = " << *iter);
      return true;
    }
  }

  return false;
}

//provide列表过滤服务分组
bool SGAgent_filter::FilterProvider(const CRouteData &route, const SGService &oservice) {

  //自定义分组，通过ip:port格式比对
  std::string ip_port_str = "\"" + oservice.ip
      + ":" + boost::lexical_cast<std::string>(oservice.port) + "\"";

  for (std::vector<std::string>::const_iterator iter = route.provider.begin(); route.provider.end() != iter; ++iter) {
    if ((*iter == ip_port_str) || FilterHost(*iter, ip_port_str)) {
      return true;
    }
  }
  return false;
}

/*
 * 根据服务分组列表，过滤服务列表
 * */
int SGAgent_filter::FilterRoute(std::vector<SGService> &services,
                                const std::vector<CRouteData> &sorted_routes, 
																const std::string &ip,
																const bool open_auto_route) {
  //获取sg_agent的IP，用于过滤consumer, 初始化时已经获取到IP
  std::string local_ip = "\"" + ip + "\"";

  LOG_DEBUG("active original routes'size = " << sorted_routes.size());

  std::vector<CRouteData> not_exclusive_routes = sorted_routes;
  std::vector<CRouteData> exclusive_routes;

  GetExclusiveRoute(not_exclusive_routes, exclusive_routes, true);

  bool is_local_ip_in_exclusive_routes = IsMatchRoutesConsumer(exclusive_routes, local_ip);
  if (!is_local_ip_in_exclusive_routes && !exclusive_routes.empty()) {
    // if local_ip is not in exclusive_routes, delete service node based on exclusive_routes
    FilterProvidersByExclusiveRoutes(services, exclusive_routes);
  }

  std::vector<CRouteData>::const_iterator
      iter = is_local_ip_in_exclusive_routes ? sorted_routes.begin() : not_exclusive_routes.begin();
  std::vector<CRouteData>::const_iterator
      end_iter = is_local_ip_in_exclusive_routes ? sorted_routes.end() : not_exclusive_routes.end();

  //遍历服务分组列表，按照优先级从高到低依次遍历
  for (; iter != end_iter; ++iter) {
    bool routeLimit = _isRouteLimit(*iter);
    bool is_hit = false;
    // 如果符合本机房过滤的条件， 进行本机房过滤
    switch (iter->category) {
      case 1:
        if (open_auto_route) {
          FilterIdcOrCenter(services, ip, routeLimit, true);
        }
        is_hit = true;
        break;
      case 3:
        if (open_auto_route) {
          FilterIdcOrCenter(services, ip, routeLimit, false);
        }
        is_hit = true;
        break;
      case 4:
      default:std::vector<SGService> serviceList_tmp;

        // 先匹配comsumer:如果consumer都没匹配到，则应用下一条route规则
        // 否则应用该规则， 不论结果是否为空， 都跳出循环
        if (!IsMatchConsumer(*iter, local_ip)) {
          is_hit = false;
          break;
        }
        LOG_DEBUG("filter route OK! using route name is : " << iter->name
                                                            << ", priority is : " << iter->priority);

        //再过滤provide,如果为空则返回所有service
        for (std::vector<SGService>::iterator vec = services.begin();
             vec != services.end(); ++vec) {
          //如果route中匹配到，则添加此provider到tmp
          if (FilterProvider(*iter, *vec)) {
            SGService service_tmp = *vec;
            serviceList_tmp.push_back(service_tmp);
          } else {
            // 对于未匹配的provider， 根据reserved字段
            // 如果强制过滤， 则不保留该vec值
            // 否则修改vec对应权重位0，保留
            if (!routeLimit) {
              SGService service_tmp = *vec;
              service_tmp.weight = 0;
              service_tmp.fweight *= SameRegionMod;
              serviceList_tmp.push_back(service_tmp);
            }
          }
        }

        services = serviceList_tmp;
        is_hit = true;
        break;
    }
    if (is_hit) {
      break;
    }
  }

  return SUCCESS;
}

/*
 * 根据服务分组优先级排序，并且过滤掉下线状态的服务分组
 * */
int SGAgent_filter::SortRouteList(std::vector<CRouteData> &routeList) {
  if (routeList.empty()) {
    LOG_WARN("routeList is empty");
    return -1;
  }
  //首先过滤掉status!=1的分组
  for (std::vector<CRouteData>::iterator iter = routeList.begin();
       iter != routeList.end();) {

    if (1 != iter->status) {
      iter = routeList.erase(iter);
      continue;
    }
    iter++;
  }

  if (routeList.size() == 0) {
    return -1;
  }

  //对routList按照优先级逆序排列
  std::sort(routeList.begin(), routeList.end(), compRouterData);

  return 0;
}

std::string SGAgent_filter::getValue(std::string src, std::string key,
                                     std::string sp, std::string sp2) {
  if (src.empty() || key.empty() || sp.empty() || sp2.empty()) {
    return "";
  }

  std::vector<std::string> kvs;
  //split(kvs, src, boost::is_any_of<std::string>(sp));
  int ret = SplitStringIntoVector(src.c_str(), sp.c_str(), kvs);
  if (0 == ret) {
    return "";
  }

  std::vector<std::string> kv;
  for (std::vector<std::string>::iterator iter = kvs.begin(); iter != kvs.end(); ++iter) {
    kv.clear();
    //split(kv, *iter, boost::is_any_of<std::string>(sp2));
    ret = SplitStringIntoVector(iter->c_str(), sp2.c_str(), kv);
    if (kv.size() == 2 && key == kv[0]) {
      return kv[1];
    }
  }

  return "";
}

/**
 * 检测是否需要保留过滤掉的结果
 */
bool SGAgent_filter::_isRouteLimit(const CRouteData &routeData) {
  //当配置route_limit=1, 返回true; 否则返回alse
  if (!(routeData.reserved).empty()) {
    //解析reserved内容
    std::string value = getValue(routeData.reserved, LIMIT_KEY, "|", ":");
    if (LIMIT_VALUE == value) {
      return true;
    }
  }
  return false;
}

int SGAgent_filter::syncFweight(std::vector<SGService> &services) {
  for (std::vector<SGService>::iterator iter = services.begin();
       iter != services.end(); ++iter) {
    if (iter->fweight != iter->weight
        && 0 == iter->fweight) {
      iter->fweight = iter->weight;
    }
  }
  return 0;
}

int SGAgent_filter::filterServiceName(std::vector<SGService> &serviceList,
                                      std::string serviceName) {
  for (std::vector<SGService>::iterator iter = serviceList.begin();
       iter != serviceList.end();) {
    std::map<std::string, ServiceDetail>::iterator it
        = iter->serviceInfo.find(serviceName);
    if (iter->serviceInfo.end() == it) {
      iter = serviceList.erase(iter);
      continue;
    }
    ++iter;
  }
  return 0;
}

int SGAgent_filter::FilterSwimlane(std::vector<SGService> *serviceList,
                                   const std::vector<SGService> &original_serviceList,
                                   const std::string &swimlane) {
  std::vector<SGService>::const_iterator iter = original_serviceList.begin();
  while (iter != original_serviceList.end()) {
    if (swimlane == iter->swimlane) {
      serviceList->push_back(*iter);
    }
    ++iter;
  }

  // 表示过滤后的结果为空， 返回负值
  return serviceList->empty() ? FAILURE : SUCCESS;
}

void SGAgent_filter::DeleteNodeWithSwimlane(std::vector<SGService> &serviceList) {
  std::vector<SGService>::iterator iter = serviceList.begin();
  while (iter != serviceList.end()) {
    if (!iter->swimlane.empty()) {
      LOG_DEBUG("Erase Swimlane, appkey = " << iter->appkey
                                            << " swimlane: " << iter->swimlane
                                            << " ip : " << iter->ip.c_str());
      iter = serviceList.erase(iter);
      continue;
    }
    ++iter;
  }
}

void SGAgent_filter::DeleteNodeWithCell(std::vector<SGService> &serviceList){
  std::vector<SGService>::iterator iter = serviceList.begin();
  while (iter != serviceList.end()) {
    if (!iter->cell.empty()) {
      LOG_DEBUG("Erase Cell, appkey = " << iter->appkey
                                            << " cell: " << iter->cell
                                            << " ip : " << iter->ip);
      iter = serviceList.erase(iter);
      continue;
    }
    ++iter;
  }
}

void SGAgent_filter::GetExclusiveRoute(std::vector<CRouteData> &routes,
                                       std::vector<CRouteData> &exclusive_routes,
                                       bool is_update_routes) {
  for (std::vector<CRouteData>::iterator iter = routes.begin(); routes.end() != iter;) {
    if (4 == iter->category) {
      exclusive_routes.push_back(*iter);
      if (is_update_routes) {
        iter = routes.erase(iter);
        continue;
      }
    }
    ++iter;
  }
}
void SGAgent_filter::FilterProvidersByExclusiveRoutes(std::vector<SGService> &serivces,
                                                      const std::vector<CRouteData> &exclusive_routes) {
  // delete service node while it is match the exclusive_routes
  for (std::vector<CRouteData>::const_iterator route_iter = exclusive_routes.begin();
       exclusive_routes.end() != route_iter; ++route_iter) {
    for (std::vector<SGService>::iterator service_iter = serivces.begin(); serivces.end() != service_iter;) {
      if (FilterProvider(*route_iter, *service_iter)) {
        LOG_DEBUG("delete ip = " << service_iter->ip << " port = " << service_iter->port << "from the service list");
        service_iter = serivces.erase(service_iter);
      } else {
        ++service_iter;
      }
    }
  }
}
bool SGAgent_filter::IsMatchRoutesConsumer(const std::vector<CRouteData> &routes, const std::string &ip) {
  for (std::vector<CRouteData>::const_iterator iter = routes.begin(); routes.end() != iter; ++iter) {
    if (IsMatchConsumer(*iter, ip)) {
      return true;
    }
  }
  return false;
}

} //namespace
