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

#ifndef __sgagent_filter_H__
#define __sgagent_filter_H__

#include <string>
#include <vector>
#include <set>
#include "sgagent_service_types.h"
#include "msgparam.h"
#include <pthread.h>
using namespace __gnu_cxx;

namespace sg_agent {

const static std::string LIMIT_KEY = "route_limit";
const static std::string LIMIT_VALUE = "1";
const static std::string UNLIMIT_VALUE = "0";

// 启用本机房过滤的权重
//const static int LOCER_PRIORITY = 0;
const static double SameRegionMod = 0.001;
const static double DiffRegionMod = 0.000001;

// routeData排序比较函数，基于priority大小
static bool compRouterData(const CRouteData &r1, const CRouteData &r2) {
  return r1.priority > r2.priority;
}

class SGAgent_filter {
 public:
  // 过滤掉权重为零的服务节点
  static int FilterWeight(std::vector<SGService> &serviceList, double thresholdWeight = 0);

  // 根据服务分组优先级排序，并且过滤掉下线状态的服务分组
  static int SortRouteList(std::vector<CRouteData> &routeList);

  // 过滤服务分组,根据服务分组信息过滤返回的服务列表
  static int FilterRoute(std::vector<SGService> &serviceList,
                         const std::vector<CRouteData> &sorted_routes,
                         const std::string &ip,
                         const bool open_auto_route = true);

  /**
   * 过滤backup节点,正常情况只返回nomal角色的服务列表，
   * 只有所有服务不可用时，才返回backup节点
   */
  static int filterBackup(std::vector<SGService> &serviceList);

  /**
   * 过滤非ALIVE节点
   */
  static int filterUnAlive(std::vector<SGService> &serviceList);

  /**
   * 检测是否有alive状态服务
   */
  static bool hasAliveNode(std::vector<SGService> &serviceList);

  /**
   * 同步fweight，将空fweight置为weight值
   */
  static int syncFweight(std::vector<SGService> &service);

  /**
   * 过滤非ALIVE节点
   */
  static int filterServiceName(std::vector<SGService> &serviceList,
                               std::string serviceName);

  /**
   * 获取CRoute的reserved扩展字段route_enforce值
   * reserved格式k1:v1|k2:v2..., 此处K为“route_enforce", 0:空返回全部， 1:空返回空
   */
  static std::string getValue(std::string src, std::string key,
                              std::string sp, std::string sp2);

  /**
   * 过滤swimlane内的节点
   */
  static int FilterSwimlane(std::vector<SGService> *serviceList,
                            const std::vector<SGService> &original_serviceList,
                            const std::string &swimlane);
  //删除含有泳道标识的节点
  static void DeleteNodeWithSwimlane(std::vector<SGService> &serviceList);

  //删除含有SET标识的节点
  static void DeleteNodeWithCell(std::vector<SGService> &serviceList);

  static bool IsMatchRoutesConsumer(const std::vector<CRouteData> &routes, const std::string &ip);
  /**
   * 从routes中过滤排他性路由分组，category=4
   * @param routes
   * @param exclusive_routes
   * @param is_update_routes true: 将会更新routes
   */
  static void GetExclusiveRoute(std::vector<CRouteData> &routes,
                                std::vector<CRouteData> &exclusive_routes,
                                bool is_update_routes);

  // delete the service node while it is match the exclusive_routes
  static void FilterProvidersByExclusiveRoutes(std::vector<SGService> &serivces,
                                               const std::vector<CRouteData> &exclusive_routes);

 private:
  // 通过IP信息过滤服务消费者
  static bool IsMatchConsumer(const CRouteData &route, const std::string &local_ip);

  // 通过服务分组过滤服务提供列表
  static bool FilterProvider(const CRouteData &route, const SGService &oservice);
  // 对ip:port主机进行过滤,判断是否匹配上服务分组列表中的ip:port信息
  static bool FilterHost(const std::string &regex_ip, const std::string &local_ip);
  static bool RegexFilterIp(const std::string &regex_ip, const std::string &local_ip);
  static bool RegexFilterPort(const std::string &regex_ip, const std::string &local_ip);

  /**
   *
   * @param route_limit
   * @param is_idc 标志位 true:按照同机房优先过滤, false:按照同中心过滤
   * @return
   */
  static int FilterIdcOrCenter(std::vector<SGService> &, const std::string &,
                               bool route_limit, bool is_idc);

  /**
   *
   * @param is_idc 标志位 true:按照同机房优先过滤, false:按照同中心过滤
   * @return
   */
  static int FilterRange(std::vector<SGService> &, const std::string &,
                         bool, bool, bool is_idc);

  /**
   * 检测是否需要保留过滤掉的结果
   * true: 强制过滤， 不保留； false： 保留
   */
  static bool _isRouteLimit(const CRouteData &routeData);

};

} // namespace


#endif

