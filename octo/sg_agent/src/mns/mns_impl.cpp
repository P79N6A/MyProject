// =====================================================================================
//
//      Filename:  protocol_service_client.cpp
//
//      Revision:  none
//
// =====================================================================================
#include "mns_impl.h"

#include <dlfcn.h>
#include "util/sgagent_filter.h"
#include "util/sgservice_misc.h"
#include "util/falcon_mgr.h"
#include "util/global_def.h"

extern GlobalVar *g_mns_global_var;

namespace sg_agent {
BufferMgr<getroute_res_param_t> *MnsImpl::m_route_cache = NULL;
BufferMgr<getservice_res_param_t> *MnsImpl::m_origin_srvlist_cache = NULL;
BufferMgr<std::vector<SGService> > *MnsImpl::m_filted_srvlist_cache = NULL;
BufferMgr<getservicename_res_param_t> *MnsImpl::m_srvname_cache = NULL;
BufferMgr<AppkeyDescResponse> *MnsImpl::m_appkeydesc_cache = NULL;


void *MnsImpl::Create() {
  IMnsPlugin *p = new MnsImpl();
  return p;
}

void MnsImpl::Destroy(void *p) {
  if (p) {
    IMnsPlugin *p1 = static_cast<IMnsPlugin *>(p);
    delete p1;
  }
}

MnsImpl::MnsImpl() : m_timeout(DEFAULT_SERVICE_TIMEOUT),
                     m_zk_client(this) {

}

MnsImpl::~MnsImpl() {
  SAFE_DELETE(m_route_cache);

  SAFE_DELETE(m_origin_srvlist_cache);

  SAFE_DELETE(m_filted_srvlist_cache);

  SAFE_DELETE(m_srvname_cache);

  SAFE_DELETE(m_appkeydesc_cache);
}

int MnsImpl::Init(const std::string &local_ip, const std::string &mask, int timeout, int retry) {
  m_timeout = timeout;
  m_local_ip = local_ip;
  m_local_mask = mask;
  m_retry = retry;

  if (NULL == m_origin_srvlist_cache) {
    m_origin_srvlist_cache = new BufferMgr<getservice_res_param_t>();
  }
  if (NULL == m_filted_srvlist_cache) {
    m_filted_srvlist_cache = new BufferMgr<std::vector<SGService> >();
  }
  if (NULL == m_route_cache) {
    m_route_cache = new BufferMgr<getroute_res_param_t>();
  }
  if (NULL == m_srvname_cache) {
    m_srvname_cache = new BufferMgr<getservicename_res_param_t>();
  }
  if (NULL == m_appkeydesc_cache) {
    m_appkeydesc_cache = new BufferMgr<AppkeyDescResponse>();
  }

  // 启动backend处理线程
  m_srv_keys = boost::shared_ptr<std::set<std::string> >(new std::set<std::string>());
  m_srvname_keys = boost::shared_ptr<std::set<std::string> >(new std::set<std::string>());
  m_appkeydesc_keys = boost::shared_ptr<std::set<std::string> >(new std::set<std::string>());

  LOG_DEBUG("init plugin: timeout: " << m_timeout << ", ip: " << m_local_ip);

  return SUCCESS;
}

/**
 * 根据服务分组信息，获取服务列表功能
 */
int MnsImpl::GetSrvList(std::vector<SGService> &srv_list,
                        const ProtocolRequest &req,
                        const bool &is_origin_cache,
                        const bool &is_filte_backbone_swimlane,
                        const bool &is_filte_swimlane) {
  int ret = 0;
  if (!req.remoteAppkey.empty()) {
    // mtthrift
    LOG_INFO("Get servicelist by remoteAppkey, remoteAppkey = " << req.remoteAppkey);
    ret = GetSrvListByAppkey(srv_list, req, is_origin_cache, is_filte_backbone_swimlane, is_filte_swimlane);
  } else if (!req.serviceName.empty()) {
    //pigeon
    LOG_INFO("RemoteAppkey is empty, get servicelist by serviceName, serviceName = " << req.serviceName);
    ret = GetSrvListBySrvName(srv_list, req, is_origin_cache, is_filte_backbone_swimlane, is_filte_swimlane);
  } else {
    LOG_ERROR("both remote appkey and servicename cannot be empty");
    return ERR_PARAMNOTCOMPLETE;
  }

  // hotfix for remoteService is mtthrit || locAppkey = null
  if ("thrift" == req.protocol) {
    if (g_mns_global_var->gOpen == g_mns_global_var->gOpenUnifiedProtoChange4LocalAppkey && req.localAppkey.empty()) {
      LOG_WARN("remoteAppkey = " << req.remoteAppkey
                                 << ", localAppkey is empty, change unifiedProto to false");
      SGServiceMisc::ChangeUnifiedProto2False(&srv_list);
    } else if (g_mns_global_var->gOpen == g_mns_global_var->gOpenUnifiedProtoChange4MTthrift) {
      SGServiceMisc::ChangeUnifiedProto2FalseWithVersionCheck(&srv_list);
    }
  }
//TODO upload statistic data to falcon
  return ret;
}

/**
 * @param is_origin_cache 
 * 				true: use m_origin_srvlist_cache,
 * 				false: use m_filted_srvlist_cache
 * @param is_filte_backbone_swimlane (in the backbone swimlane)
 * 				true: only return the backbone service nodes, 
 * 				false: return all service nodes
 * @param is_filte_swimlane in the special swimlane
 * 				true: run the swimlane logic
 * 				false: skip the swimlane logic
 */
int MnsImpl::DoGetSrvList(std::vector<SGService> &srvlist,
                          const ProtocolRequest &req,
                          const bool &is_origin_cache,
                          const bool &is_filte_backbone_swimlane,
                          const bool &is_filte_swimlane) {

  if (NULL == m_origin_srvlist_cache) {
    LOG_ERROR("origin srvlist cache is NULL, please wait its initialization.");
    return ERR_SERVICE_BUFFER_NULL;
  }
  if (NULL == m_filted_srvlist_cache) {
    LOG_ERROR("filted srvlist cache is NULL, please wait its initialization.");
    return ERR_SERVICE_BUFFER_NULL;
  }

  std::string key = GenCacheKey(req.remoteAppkey, req.protocol);

  int ret = FAILURE;
  ServicePtr resServicePtr(new getservice_res_param_t());

  // the special swimlane logic
  if (is_filte_swimlane && !req.swimlane.empty()) {
    //get data from swimlane
    ret = m_origin_srvlist_cache->get(key, *resServicePtr);

    if (SUCCESS == ret) {
      ret = SGAgent_filter::FilterSwimlane(&srvlist, resServicePtr->serviceList, req.swimlane);
      if (SUCCESS == ret) {
        return SUCCESS;
      } else {
        LOG_WARN("After Filter Swimlane ServiceList is empty, go back to normal way, appkey = "
                     << req.remoteAppkey
                     << ", protocol = " << req.protocol
                     << ", original servicelist size = " << resServicePtr->serviceList.size()
                     << ", swimlane = " << req.swimlane);
      }
    } else {
      LOG_WARN("Cannot find data from origin servicelist cache, ret = " << ret);
    }
  }

  // the backbone swimlane logic

  if (is_origin_cache) {
    boost::shared_ptr<getservice_res_param_t> service_param(new getservice_res_param_t);
    ret = m_origin_srvlist_cache->get(key, *service_param);

    if (SUCCESS == ret) {
      srvlist = service_param->serviceList;

      // delete service nodes while local ip is not in exclusive routes
      boost::shared_ptr<getroute_res_param_t> routes(new getroute_res_param_t());

      if (SUCCESS == m_route_cache->get(key, *routes)) {
        std::vector<CRouteData> exclusive_routes;
        SGAgent_filter::GetExclusiveRoute(routes->routeList, exclusive_routes, false);
        std::string local_ip = "\"" + m_local_ip + "\"";

        if (!SGAgent_filter::IsMatchRoutesConsumer(exclusive_routes, local_ip)) {
          SGAgent_filter::FilterProvidersByExclusiveRoutes(srvlist, exclusive_routes);
        }
      } else {
        LOG_WARN("Cannot find data from route cache.");
      }
    }
  } else {
    ret = m_filted_srvlist_cache->get(key, srvlist);
  }

  if (is_filte_backbone_swimlane) {
    SGAgent_filter::DeleteNodeWithSwimlane(srvlist);
  }

  //缺省
  bool isFiltedCell = true;
  if(req.__isset.enableCell && req.enableCell){
    isFiltedCell = false;
  }

  if(isFiltedCell){

    SGAgent_filter::DeleteNodeWithCell(srvlist);
  }

  return ret;
}
/**
 * 根据服务分组信息，获取服务列表功能
 */
int MnsImpl::GetSrvListByAppkey(std::vector<SGService> &srv_list,
                                const ProtocolRequest &req,
                                const bool &is_origin_cache,
                                const bool &is_filte_backbone_swimlane,
                                const bool &is_filte_swimlane) {

  int ret = DoGetSrvList(srv_list, req, is_origin_cache, is_filte_backbone_swimlane, is_filte_swimlane);

  if (SUCCESS != ret) {
    // cannot find data from cache, now try to get data from zk
    LOG_WARN("cannot find data from cache, now try to get data from zk, ret = " << ret);
    ret = GetRouteList(req.localAppkey, req.remoteAppkey, req.protocol);
    if (SUCCESS != ret) {
      // already log inside.
      LOG_ERROR("Failed to get routelist from zk, ret = " << ret);
      return ret;
    }
    ServicePtr resServicePtr(new getservice_res_param_t());
    resServicePtr->__set_localAppkey(req.localAppkey);
    resServicePtr->__set_remoteAppkey(req.remoteAppkey);
    resServicePtr->__set_version("");
    resServicePtr->__set_protocol(req.protocol);
    ret = GetSrvListFromZk(resServicePtr, false);
    if (SUCCESS != ret) {
      // already log inside.
      return ret;
    }

    /**
     * wait for return, scan buffer
     */
    timeval time_start;
    timeval time_end;
    long deltaTime;

    gettimeofday(&time_start, NULL);
    do {
      ret = DoGetSrvList(srv_list, req, is_origin_cache, is_filte_backbone_swimlane, is_filte_swimlane);
      gettimeofday(&time_end, NULL);
      deltaTime = (time_end.tv_sec - time_start.tv_sec) * 1000000L
          + (time_end.tv_usec - time_start.tv_usec);
      if (SUCCESS != ret) {
        LOG_WARN("Try to find data from cache , ret = " << ret);
        usleep(DEFAULT_SLEEPTIME);
      }
    } while ((long) m_timeout > deltaTime && SUCCESS != ret);
  }

  return ret;
}

int MnsImpl::DoGetSrvListBySrvName(std::vector<SGService> &srvlist,
                                   const ProtocolRequest &req,
                                   const bool &is_origin_cache,
                                   const bool &is_filte_backbone_swimlane,
                                   const bool &is_filte_swimlane) {

  if (NULL == m_srvname_cache) {
    LOG_ERROR("servicename cache is NULL, please wait its initialization.");
    return ERR_SERVICENAME_BUFFER_NULL;
  }
  std::string key = GenCacheKey(req.serviceName, req.protocol);
  ServiceNamePtr resServiceNamePtr(new getservicename_res_param_t());

  int ret = m_srvname_cache->get(key, *resServiceNamePtr);
  if (SUCCESS == ret) {
    ProtocolRequest req_tmp = req;
    for (std::set<std::string>::iterator iter = resServiceNamePtr->appkeys.begin();
         resServiceNamePtr->appkeys.end() != iter; ++iter) {
      std::vector<SGService> list;
      req_tmp.remoteAppkey = *iter;
      ret = GetSrvListByAppkey(list, req_tmp, is_origin_cache, is_filte_backbone_swimlane, is_filte_swimlane);
      if (SUCCESS == ret) {
        srvlist.insert(srvlist.begin(), list.begin(), list.end());
      } else {
        LOG_ERROR("Failed to get servicelist by Appkey, Appkey = " << req_tmp.remoteAppkey
                                                                   << ", ret = " << ret);
      }
    }
    return SGAgent_filter::filterServiceName(srvlist, req.serviceName);
  } else {
    LOG_WARN("Cannot find data from servicename cache, ret = " << ret);
    return ret;
  }
}

/**
 * 根据服务分组信息，获取服务列表功能
 */
int MnsImpl::GetSrvListBySrvName(std::vector<SGService> &serviceList,
                                 const ProtocolRequest &req,
                                 const bool &is_origin_cache,
                                 const bool &is_filte_backbone_swimlane,
                                 const bool &is_filte_swimlane) {

  int ret = DoGetSrvListBySrvName(serviceList, req, is_origin_cache, is_filte_backbone_swimlane, is_filte_swimlane);
  if (SUCCESS != ret) {
    //未找到，需要从worker(zk)里面同步拿取一份serviceName
    LOG_DEBUG("Cannot find data from cache ,and try to get serviceName from zk.");
    ServiceNamePtr resServiceNamePtr(new getservicename_res_param_t());
    resServiceNamePtr->__set_localAppkey(req.localAppkey);
    resServiceNamePtr->__set_servicename(req.serviceName);
    resServiceNamePtr->__set_protocol(req.protocol);
    resServiceNamePtr->__set_version("");
    ret = GetSrvNameFromZk(resServiceNamePtr);
    //如果从ZK获取失败
    if (SUCCESS != ret) {
      LOG_ERROR("getAppkeyByServiceNameFromWorker failed. localAppkey = " << req.localAppkey
                                                                          << ", serviceName = " << req.serviceName
                                                                          << ", protocol = " << req.protocol
                                                                          << ", ret = " << ret);
      return ret;
    }


    /**
	 * wait for return, scan buffer
	 */
    timeval tvalStart;
    timeval tvalEnd;
    long deltaTime;

    gettimeofday(&tvalStart, NULL);
    do {
      ret = DoGetSrvListBySrvName(serviceList, req, is_origin_cache, is_filte_backbone_swimlane, is_filte_swimlane);
      if (SUCCESS != ret) {
        LOG_WARN("Try to find data from cache , ret = " << ret);
      }
      gettimeofday(&tvalEnd, NULL);
      deltaTime = (tvalEnd.tv_sec - tvalStart.tv_sec) * 1000000L
          + (tvalEnd.tv_usec - tvalStart.tv_usec);
      usleep(DEFAULT_SLEEPTIME);
    } while ((long) m_timeout > deltaTime && SUCCESS != ret);
  }
  return ret;
}

/**
 * 根据服务分组信息，获取服务列表功能
 */
int MnsImpl::GetRouteList(const std::string &local_appkey,
                          const std::string &remote_appkey, const std::string &protocol) {
  std::string key = GenCacheKey(remote_appkey, protocol);
  RoutePtr resRoutePtr(new getroute_res_param_t());
  //先取服务列表
  int ret = m_route_cache->get(key, *(resRoutePtr.get()));
  if (SUCCESS != ret) {
    LOG_DEBUG("Cannot find data from route cache , and try to get data from zk, ret = " << ret);
    resRoutePtr->__set_localAppkey(local_appkey);
    resRoutePtr->__set_remoteAppkey(remote_appkey);
    resRoutePtr->__set_version("");
    resRoutePtr->__set_protocol(protocol);
    ret = GetRouteFromZk(resRoutePtr);
    if (0 != ret) {
      LOG_WARN("getRouteList ret = " << ret
                                     << ", protocol = " << protocol
                                     << ", appkey = " << remote_appkey);
      return ret;
    }
  }

  return ret;
}

int MnsImpl::GetSrvNameFromZk(ServiceNamePtr servicename) {

  if (NULL == m_srvname_cache) {
    LOG_ERROR("ProtocolServicePlugin's ServiceNameBufferMgr is NULL, please init it before");
    return FAILURE;
  }

  std::string localAppkey = servicename->localAppkey;
  std::string serviceName = servicename->servicename;
  std::string protocol = servicename->protocol;
  std::string version = servicename->version;
  LOG_INFO("TO get Appkey From Worker, serviceName = " << serviceName
                                                       << ", protocol = " << protocol);

  std::set<std::string> appkeys;
  if (serviceName.empty()) {
    LOG_ERROR(
        "task which is not complete, serviceName is NULL. localAppkey = " << localAppkey << ", protocol = " << protocol
                                                                          << ", version = " << version);
    return -2;
  } else {
    //get From zk
    int ret = m_zk_client.GetAppkeyByServiceName(appkeys, localAppkey, serviceName, version, protocol);
    if (SUCCESS == ret) {
      LOG_DEBUG("succeed to get serviceName from zk"
                    << ", serviceName = " << serviceName
                    << ", protocol = " << protocol
                    << ", appkeys' size = " << appkeys.size());

      servicename->__set_version(version);
      servicename->__set_appkeys(appkeys);
    } else if (ERR_NODE_NOTFIND == ret) {
      LOG_WARN("can not find serviceName node from zk"
                   << ", serviceName = " << serviceName
                   << ", protocol = " << protocol
                   << ", version = " << version);
      return ret;
    } else {
      LOG_ERROR("getServiceName from zk fail, "
                    << ", serviceName = " << serviceName
                    << ", protocol = " << protocol
                    << ", appkeys' size = " << appkeys.size());
      return -1;
    }

    //write Cache
    std::string key = GenCacheKey(serviceName, protocol);
    m_srvname_cache->insert(key, *servicename);
    //write keySet
    {
      muduo::MutexLockGuard lock(m_srvname_mutex_lock);
      if (!m_srvname_keys.unique()) {
        m_srvname_keys.reset(new std::set<std::string>(*m_srvname_keys));
      }
      m_srvname_keys->insert(key);
    }

    LOG_INFO("Receive ServiceName Task in worker thread, key = " << key
                                                                 << ", appkey size:" << servicename->appkeys.size());
    FalconMgr::SetServiceBufferSize(m_origin_srvlist_cache->size());
    return 0;
  }
}

int MnsImpl::GetSrvListFromZk(ServicePtr service, bool is_watcher_callback) {
  std::string local_appkey = service->localAppkey;
  std::string remote_appkey = service->remoteAppkey;
  std::string version = service->version;
  std::string protocol = service->protocol;

  if (remote_appkey.empty()) {
    LOG_ERROR("remote_appkey cannot be empty.");
    return FAILURE;
  }

  if (NULL == m_origin_srvlist_cache) {
    LOG_ERROR("origin service list buffer (mServiceBufferMgr) is NULL, please init it before.");
    return -2;
  }

  std::vector<SGService> serviceList;
  //get From zk
  int ret = m_zk_client.GetSrvListByProtocol(serviceList,
                                             local_appkey,
                                             remote_appkey,
                                             version,
                                             protocol,
                                             is_watcher_callback);

  if (SUCCESS == ret || ERR_NODE_NOTFIND == ret) {
    if (ERR_NODE_NOTFIND == ret) {
      LOG_WARN("can not find service node from zk"
                   << ", appkey = " << remote_appkey
                   << ", protocol = " << protocol
                   << ", current version = " << version);
      // return empty list
      serviceList.clear();
    }
    if (SUCCESS == ret) {
      LOG_DEBUG("succeed to get service list from zk"
                    << ", remoteAppkey = " << remote_appkey
                    << ", protocol = " << protocol
                    << ", serviceList' size = " << serviceList.size());
      service->__set_version(version);
      service->__set_serviceList(serviceList);
    }
  } else if (ret == ERR_ZK_LIST_SAME_BUFFER) {
    // already log inside
    return -3;
  } else {
    LOG_ERROR("getServiceList from zk fail, "
                  << ", remoteAppkey = " << remote_appkey
                  << ", protocol = " << protocol
                  << ", serviceList' size = " << serviceList.size());
    return -4;
  }

  //diff of serviceList and write Cache
  std::string key = GenCacheKey(remote_appkey, protocol);
  ServicePtr tmpServicePtr = boost::make_shared<getservice_res_param_t>();

  m_origin_srvlist_cache->get(key, *tmpServicePtr);

  if (tmpServicePtr->serviceList.size() > 0) {
    PrintUpdateDiff(tmpServicePtr, service);
  }
  m_origin_srvlist_cache->insert(key, *service);

  LOG_INFO("update origin cache, key = " << key << ", appkey size:" << serviceList.size());
  //write keySet
  {
    muduo::MutexLockGuard lock(m_srv_mutex_lock);
    if (!m_srv_keys.unique()) {
      m_srv_keys.reset(new std::set<std::string>(*m_srv_keys));
    }

    m_srv_keys->insert(key);
  }

  UpdateBufferServiceFilted(local_appkey, remote_appkey, protocol);

  return SUCCESS;
}

int MnsImpl::GetRouteFromZk(RoutePtr route) {
  std::string localAppkey = route->localAppkey;
  std::string remoteAppkey = route->remoteAppkey;
  std::string version = route->version;
  std::string protocol = route->protocol;

  if (remoteAppkey.empty()) {
    LOG_ERROR("routeMsg's remoteAppkey is empty! localAppkey = " << localAppkey
                                                                 << ", protocol = " << protocol
                                                                 << ", version = " << version);
    return -1;
  }

  if (NULL == m_route_cache) {
    LOG_ERROR("ProtocolServicePlugin's Route BufferMgr is NULL, please init it before");
    return -2;
  }

  std::vector<CRouteData> routeList;
  //get From zk
  int ret = m_zk_client.getRouteListByProtocol(routeList, localAppkey, remoteAppkey, version, protocol);
  if (SUCCESS == ret) {
    LOG_DEBUG("succeed to get route from zk"
                  << ", remoteAppkey = " << remoteAppkey
                  << ", protocol = " << protocol
                  << ", routelist' size = " << routeList.size());

    route->__set_version(version);
    route->__set_routeList(routeList);
  } else if (ERR_NODE_NOTFIND == ret) {
    LOG_WARN("can not find route node from zk"
                 << ", appkey = " << remoteAppkey
                 << ", protocol = " << protocol
                 << ", version = " << version);
    return ret;
  } else if (ret == ERR_ZK_LIST_SAME_BUFFER) {
    LOG_DEBUG("ZK getRouteList is the same as buf, localAppkey : " << localAppkey
                                                                   << ", remoteAppkey is : " << remoteAppkey
                                                                   << ", version : " << version);
    return ret;
  } else {
    LOG_ERROR("getServiceList from zk fail, "
                  << ", serviceName = " << remoteAppkey
                  << ", protocol = " << protocol
                  << ", routeList' size = " << routeList.size());
    return ret;
  }

  //TODO: 对routeList按优先级进行排序
  SGAgent_filter::SortRouteList(route->routeList);

  //write Cache
  std::string key = GenCacheKey(remoteAppkey, protocol);
  m_route_cache->insert(key, *route);
  LOG_INFO("updating route cache, key = " << key << ", size =" << routeList.size());
  UpdateBufferServiceFilted(localAppkey, remoteAppkey, protocol, false);

  return SUCCESS;
}

std::string MnsImpl::GenCacheKey(const std::string &remoteappkey,
                                 const std::string &protocol) {
  return protocol + "+" + remoteappkey;
}

void MnsImpl::UpdateSrvListTimer() {
  // 延长空节点更新周期
  int static update_count = 0;
  // 60 * 10s = 10min更新一次空的appkey
  const int static CountTimes = 60;
  update_count = (++update_count) % CountTimes;
  LOG_DEBUG("hlb update_count = " << update_count);

  bool null_node_update = (0 == update_count);

  if (NULL == m_origin_srvlist_cache) {
    LOG_ERROR("ProtocolServicePlugin's ServiceBufferMgr is NULL, please init it before");
    return;
  }

  shared_ptr<std::set<std::string> > keySet;
  {
    muduo::MutexLockGuard lock(m_srv_mutex_lock);
    keySet = m_srv_keys;
  }

  for (std::set<std::string>::iterator iter = keySet->begin(); keySet->end() != iter; ++iter) {
    std::string key = *iter;
    ServicePtr resServicePtr(new getservice_res_param_t());
    int ret = m_origin_srvlist_cache->get(key, *(resServicePtr.get()));
    if (SUCCESS == ret) {
      if (resServicePtr->serviceList.empty() && !null_node_update) {
        continue;
      }
      GetSrvListFromZk(resServicePtr, false);
    }
  }
}
void MnsImpl::UpdateRouteTimer() {
  shared_ptr<std::set<std::string> > keySet;
  {
    muduo::MutexLockGuard lock(m_srv_mutex_lock);
    keySet = m_srv_keys;
  }

  for (std::set<std::string>::iterator iter = keySet->begin(); keySet->end() != iter; ++iter) {
    std::string key = *iter;
    RoutePtr resRoutePtr(new getroute_res_param_t());
    int cache_ret = m_route_cache->get(key, *(resRoutePtr.get()));
    if (SUCCESS == cache_ret) {
      GetRouteFromZk(resRoutePtr);
    }
  }
}

void MnsImpl::UpdateSrvNameTimer() {
  if (NULL == m_srvname_cache) {
    LOG_ERROR("ProtocolServicePlugin's ServiceNameBufferMgr is NULL, please init it before");
    return;
  }

  int ret = SUCCESS;
  shared_ptr<std::set<std::string> > keySet;
  {
    muduo::MutexLockGuard lock(m_srvname_mutex_lock);
    keySet = m_srvname_keys;
  }

  std::set<std::string>::iterator iter;
  for (iter = keySet->begin(); iter != keySet->end(); ++iter) {
    //TODO:先从buf中获取老版本信息, 如果没有则直接传空给sg_agent_worker?
    std::string key = *iter;
    ServiceNamePtr resServicePtr(new getservicename_res_param_t());
    ret = m_srvname_cache->get(key, *resServicePtr);
    if (SUCCESS == ret) {
      ret = GetSrvNameFromZk(resServicePtr);
      if (SUCCESS != ret) {
        LOG_ERROR("failed to sendServiceMsg, serviceName = " << resServicePtr->servicename
                                                             << "; version = " << resServicePtr->version
                                                             << "; protocol = " << resServicePtr->protocol
                                                             << "; errorcode = " << ret);
      }
    } else {
      LOG_ERROR("get ServiceNameBufferMgr fail! key = " << key
                                                        << ", ret = " << ret);
    }
  }
}

void MnsImpl::UpdateAppkeyDescTimer() {
  if (NULL == m_appkeydesc_cache) {
    LOG_ERROR("m_appkeydesc_cache is NULL, please init it before.");
    return ;
  }
  int ret = SUCCESS;
  boost::shared_ptr<std::set<std::string> > t_keyset;
  {
    muduo::MutexLockGuard lock(m_appkeydesc_mutex_lock);
    t_keyset = m_appkeydesc_keys;
  }
  std::string key = "";
  for (std::set<std::string>::const_iterator iter = t_keyset->begin();
       t_keyset->end() != iter ; ++iter) {
    key = *iter;
    AppkeyDescResponse desc_res;
    ret = m_appkeydesc_cache->get(key, desc_res);
    if (SUCCESS == ret) {
      ret = GetAppkeyDescFromZk(desc_res, key);
      if (SUCCESS != ret ) {
        LOG_ERROR("failed to send appkeydescMsg, msg = " << desc_res.msg
                                                      << ", errcode" << desc_res.errCode);
      }
    } else {
      LOG_ERROR("Failed to get appekydesc from cache, appkey = " << key
                                                                 << ", ret = " << ret);
    }
  }
}

int MnsImpl::UpdateBufferServiceFilted(const std::string &localAppkey,
                                       const std::string &remoteAppkey,
                                       const std::string &protocol,
                                       const bool &is_update_route) {
  const std::string key = GenCacheKey(remoteAppkey, protocol);

  boost::shared_ptr<getroute_res_param_t> routeParamFilte
      (new getroute_res_param_t());
  boost::shared_ptr<getservice_res_param_t> serviceParamFilte
      (new getservice_res_param_t());
  if (is_update_route) {
    GetRouteList(localAppkey, remoteAppkey, protocol);
  }
  int ret = m_route_cache->get(key, *(routeParamFilte.get()));
  if (SUCCESS != ret) {
    LOG_WARN("get null from mRouteBufferMgr when updateFiltedRouteList, key = " << key << ", ret = " << ret);
    return ret;
  }
  bool do_route = (SUCCESS == ret);
  ret = m_origin_srvlist_cache->get(key, *(serviceParamFilte.get()));

  if (SUCCESS != ret) {
    LOG_WARN("get null from mServiceBufferMgr when updateFiltedServiceList, key = " << key << ", ret = " << ret);
    return ret;
  }

  //去除掉服务列表里面含有泳道的节点, 保证服务打到主干链路上
  //SGAgent_filter::DeleteNodeWithSwimlane(serviceParamFilte->serviceList);

  SGAgent_filter::syncFweight(serviceParamFilte->serviceList);
  if (do_route) {
    SGAgent_filter::FilterRoute(serviceParamFilte->serviceList,
                                routeParamFilte->routeList,
                                m_local_ip,
                                g_mns_global_var->gOpenAutoRoute);
  }

  if ("thrift" == serviceParamFilte->protocol) {
    SGAgent_filter::filterBackup(serviceParamFilte->serviceList);
  }

  // 更新buffer， keyset涉及到线程安全， 通过ServiceBufferMgr ！＝ 0直接
  // 返回来保证数据一致性
  m_filted_srvlist_cache->insert(key, serviceParamFilte->serviceList);

  LOG_INFO("update filted serviceList, key = " << key
                                               << ", size = " << serviceParamFilte->serviceList.size());
  FalconMgr::SetFiltedServiceBufferSize(m_filted_srvlist_cache->size());

  return SUCCESS;
}
/*
 * 获取缓存大小和对应srvlist的大小
 */
int MnsImpl::GetSrvListAndCacheSize(ServListAndCache &list_and_cache,
                                     const std::string &protocol,
                                     const std::string &appkey) {
  if (NULL == m_origin_srvlist_cache || NULL == m_filted_srvlist_cache) {
    LOG_ERROR("m_origin_srvlist_cache or m_filted_srvlist_cache is NULL, please init it before");
    return ERR_SERVICE_BUFFER_NULL;
  }
  std::string key = GenCacheKey(appkey, protocol);
  ServicePtr tmpServicePtr = boost::make_shared<getservice_res_param_t>();
  int ret = m_origin_srvlist_cache->get(key, *tmpServicePtr);
  if (SUCCESS == ret) {
    list_and_cache.origin_servlist_size = tmpServicePtr->serviceList.size();
    list_and_cache.origin_servicelist = tmpServicePtr->serviceList;
  } else {
    LOG_WARN("cannot find key in m_origin_srvlist_cache, ret = " << ret);
    list_and_cache.origin_servlist_size = 0;
    list_and_cache.origin_servicelist = std::vector<SGService>();
    //GetSrvListFromZk(tmpServicePtr, false);
  }
  std::vector<SGService> filte_servlist;
  ret = m_filted_srvlist_cache->get(key, filte_servlist);
  if (SUCCESS == ret) {
    list_and_cache.filte_servlist_size = filte_servlist.size();
    list_and_cache.filte_servicelist = filte_servlist;
  } else {
    LOG_WARN("cannot find key in m_filte_srvlist_cache, ret = " << ret);
    list_and_cache.filte_cache_size = 0;
    list_and_cache.filte_servicelist = std::vector<SGService>();
  }
  list_and_cache.origin_cache_size = m_origin_srvlist_cache->size();
  list_and_cache.filte_cache_size = m_filted_srvlist_cache->size();
  return SUCCESS;
}

int MnsImpl::GetAppkeyDesc(AppkeyDescResponse &desc_res, const std::string &appkey) {
  if (NULL == m_appkeydesc_cache) {
    LOG_ERROR("m_appkeydesc_cache is NULL, please init it before.");
    return ERR_SERVICE_BUFFER_NULL;
  }
  std::string t_appkey = appkey;
  boost::trim(t_appkey);
  if (t_appkey.empty()) {
    LOG_ERROR("param is wrong, appkey is empty.");
    return ERR_PARAM_ERROR;
  }
  int ret = m_appkeydesc_cache->get(t_appkey, desc_res);
  if (SUCCESS != ret) {
    LOG_WARN("Failed to get AppkeyDesc from cache, now try to get from zk!");
    ret = GetAppkeyDescFromZk(desc_res, t_appkey);
  }
  return ret;
}

int MnsImpl::GetAppkeyDescFromZk(AppkeyDescResponse &desc_res, const std::string &appkey){

  int ret = m_zk_client.GetAppkeyDesc(desc_res, appkey);

  if (SUCCESS == ret) {
    LOG_DEBUG("succeed to get appkey desc from zk"
                  << ", appkey = " << appkey
                  << ", category = " << desc_res.desc.category
                  << ", business = " << desc_res.desc.business
                  << ", base = " << desc_res.desc.base
                  << ", owt = " << desc_res.desc.owt
                  << ", pdl = " << desc_res.desc.pdl
                  << ", regLimit = " << desc_res.desc.regLimit
                  << ", cell = " << desc_res.desc.cell
                  << ", ret = " << ret);

  } else if (ERR_NODE_NOTFIND == ret) {
    LOG_ERROR("can not find appkey desc node from zk"
                 << ", appkey = " << appkey
                 << ", category = " << desc_res.desc.category
                 << ", business = " << desc_res.desc.business
                 << ", base = " << desc_res.desc.base
                 << ", owt = " << desc_res.desc.owt
                 << ", pdl = " << desc_res.desc.pdl
                 << ", regLimit = " << desc_res.desc.regLimit
                 << ", cell = " << desc_res.desc.cell
                 << ", ret = " << ret);
    return ret;
  } else {
    LOG_ERROR("get appkey desc from zk fail, "
                  << ", appkey = " << appkey
                  << ", category = " << desc_res.desc.category
                  << ", business = " << desc_res.desc.business
                  << ", base = " << desc_res.desc.base
                  << ", owt = " << desc_res.desc.owt
                  << ", pdl = " << desc_res.desc.pdl
                  << ", regLimit = " << desc_res.desc.regLimit
                  << ", cell = " << desc_res.desc.cell
                  << ", ret = " << ret);
    return FAILURE;
  }
  m_appkeydesc_cache->insert(appkey, desc_res);
  {
    muduo::MutexLockGuard lock(m_appkeydesc_mutex_lock);
    if (!m_appkeydesc_keys.unique()) {
      m_appkeydesc_keys.reset(new std::set<std::string>(*m_appkeydesc_keys));
    }
    m_appkeydesc_keys->insert(appkey);
  }
  return ret;

}
/*
 * 通过key更新缓存中数据
 */
int MnsImpl::UpdateSrvList(const ServicePtr &service) {
  if (NULL == m_origin_srvlist_cache) {
    LOG_ERROR("m_origin_srvlist_cache is NULL, please init it before");
    return FAILURE;
  }
  std::string key = GenCacheKey(service->remoteAppkey, service->protocol);
  ServicePtr tmpServicePtr = boost::make_shared<getservice_res_param_t>();
  std::string version = service->version;
  boost::trim(version);
  if (version.empty()) {//默认为0
    tmpServicePtr->__set_version("0");
  } else {
    tmpServicePtr->__set_version(version);
  }
  int ret = m_origin_srvlist_cache->get(key, *tmpServicePtr);
  LOG_INFO("remoteAppkey : " << service->remoteAppkey
                             << ", protocol = " << service->protocol
                             << ", tmpServicePtr size =" << tmpServicePtr->serviceList.size());
  if (SUCCESS == ret) {
    bool flag = false;
    for (std::vector<SGService>::iterator piter = service->serviceList.begin();
         piter != service->serviceList.end(); ++piter) {
      flag = false;
      for (std::vector<SGService>::iterator iter = tmpServicePtr->serviceList.begin();
           iter != tmpServicePtr->serviceList.end(); ++iter) {
        if ((iter->ip == piter->ip) && (iter->port == piter->port)) {
          if (piter->status >= fb_status::DEAD && piter->status <= fb_status::WARNING) {
            iter->__set_status(piter->status);
          } else {
            LOG_ERROR("status is not in conformity with the specification ,ip : " << piter->ip
                                                                                  <<" , port : " << piter->port);
            flag = true;
            break;
          }
          if (("thrift" == piter->protocol && 0 == piter->serverType) ||
              ("http" == piter->protocol && 1 == piter->serverType)) {
            iter->__set_protocol(piter->protocol);
            iter->__set_serviceInfo(piter->serviceInfo);
          } else {
            LOG_ERROR("thrift and serverType do not match, protocol: " << piter->protocol
                                                                       << " ,serverType: " << piter->serverType);
            flag = true;
            break;
          }
          if (piter->envir >= 1 && piter->envir <= 3) {
            iter->__set_envir(piter->envir);
          } else {
            LOG_ERROR("envir is not in conformity with the specification, envir : " << piter->envir);
            flag = true;
            break;
          }
          if (piter->fweight > 0.0 && piter->fweight <= 100.0) {
            iter->__set_weight(piter->fweight);
            iter->__set_fweight(piter->fweight);
          } else if (piter->weight > 0 && piter->weight <= 100) {
            iter->__set_weight(piter->weight);
            iter->__set_fweight(piter->weight);
          } else {
            iter->__set_weight(10);
            iter->__set_fweight(10.0);
          }
          if (!piter->hostname.empty()) {
            iter->__set_hostname(piter->hostname);
          }
          if (piter->role > 0) {
            iter->__set_role(piter->role);
          }
          if (piter->lastUpdateTime > 0) {
            iter->__set_lastUpdateTime(piter->lastUpdateTime);
          }
          if (!piter->extend.empty()) {
            iter->__set_extend(piter->extend);
          }
          iter->__set_version(piter->version);
          iter->__set_serviceInfo(piter->serviceInfo);
          iter->__set_heartbeatSupport(piter->heartbeatSupport);
          iter->__set_swimlane(piter->swimlane);
          flag = true;
          break;
        }
      }
      if (!flag) {
        LOG_INFO("ip or port is new, need add srvlist to m_origin_srvlist_cache");
        tmpServicePtr->serviceList.push_back(*piter);
      }
    }
    m_origin_srvlist_cache->insert(key, *tmpServicePtr);
  } else {
    LOG_INFO("not found,insert the flush node");
    for (std::vector<SGService>::iterator iter = service->serviceList.begin();
        service->serviceList.end() != iter; ++iter ) {
      tmpServicePtr->serviceList.push_back(*iter);
    }
    m_origin_srvlist_cache->insert(key, *tmpServicePtr);
  }
  ret = UpdateBufferServiceFilted(service->localAppkey,
                                             service->remoteAppkey,
                                             service->protocol);
  if (SUCCESS != ret) {
    LOG_ERROR("Failed to filte service list. localAppkey :" << service->localAppkey
                                                            << ", remoteAppkey :" << service->remoteAppkey
                                                            << ", protocol :" << service->protocol
                                                            << ", ret :" << ret);
  }
  return ret;
}
/*
 * 检查service参数是否符合规范
 */
int MnsImpl::CheckArgs(const SGService &oservice, SGService &iservice) {
  // args check
  std::string appkey = oservice.appkey;
  if (appkey.empty()) {
    LOG_ERROR("fail to register, because the appkey is empty.");
    return ERR_EMPTY_APPKEY;
  } else if (!IsAppkeyLegal(appkey)) {
    LOG_ERROR("Invalid appkey in regist, appkey = " << appkey);
    return ERR_INVALIDAPPKEY;
  }
  if (!IsIpAndPortLegal(oservice.ip, oservice.port)) {
    LOG_ERROR("invalid port: " << oservice.port
                               << ", appkey: " << oservice.appkey
                               << ", ip: " << oservice.ip
                               << ", weight: " << oservice.weight);
    return ERR_INVALID_PORT;
  }
  if (("thrift" == oservice.protocol && 0 == oservice.serverType) ||
      ("http" == oservice.protocol && 1 == oservice.serverType)) {
    iservice.__set_protocol(oservice.protocol);
    iservice.__set_serviceInfo(oservice.serviceInfo);
  } else {
    LOG_ERROR("invalid protocol : " << oservice.protocol
                               << ", serverType : " << oservice.serverType);
    return ERR_INVALID_PROTOCOL;
  }
  if (oservice.fweight > 0.0 && oservice.fweight <= 100.0) {
    iservice.__set_weight(oservice.fweight);
    iservice.__set_fweight(oservice.fweight);
  } else if (oservice.weight > 0 && oservice.weight <= 100) {
    iservice.__set_weight(oservice.weight);
    iservice.__set_fweight(oservice.weight);
  } else {
    iservice.__set_weight(10);
    iservice.__set_fweight(10.0);
  }
  if (!oservice.hostname.empty()) {
    iservice.__set_hostname(oservice.hostname);
  }
  if (oservice.role > 0) {
    iservice.__set_role(oservice.role);
  }
  if (oservice.lastUpdateTime > 0) {
    iservice.__set_lastUpdateTime(oservice.lastUpdateTime);
  }
  if (!oservice.extend.empty()) {
    iservice.__set_extend(oservice.extend);
  }
  iservice.__set_appkey(appkey);
  iservice.__set_ip(oservice.ip);
  iservice.__set_port(oservice.port);
  iservice.__set_version(oservice.version);
  iservice.__set_envir(oservice.envir);
  iservice.__set_serviceInfo(oservice.serviceInfo);
  iservice.__set_cell(oservice.cell);
  iservice.__set_heartbeatSupport(oservice.heartbeatSupport);
  iservice.__set_swimlane(oservice.swimlane);

  return SUCCESS;
}


int MnsImpl::RepalceSrvlist(const ServicePtr &service) {
  if (NULL == m_origin_srvlist_cache ) {
    LOG_ERROR("m_origin_srvlist_cache is NULL, please init.");
    return ERR_SERVICE_BUFFER_NULL;
  }
  if (NULL == m_filted_srvlist_cache) {
    LOG_ERROR("m_filted_srvlist_cache is NULL, please init.");
    return ERR_SERVICE_BUFFER_NULL;
  }
  std::string key = GenCacheKey(service->remoteAppkey, service->protocol);
  LOG_INFO("remoteAppkey : " << service->remoteAppkey
                             << "protocol : " << service->protocol
                             << "service size is " << service->serviceList.size());
  ServicePtr tmp_service = make_shared<getservice_res_param_t>();
  if (!service->localAppkey.empty()) {
    tmp_service->__set_localAppkey(service->localAppkey);
  }
  tmp_service->__set_remoteAppkey((service->remoteAppkey));
  tmp_service->__set_protocol(service->protocol);
  std::string version = service->version;
  boost::trim(version);
  if (version.empty()) {//版本号没有填写，默认为0
    tmp_service->__set_version("0");
  } else {
    tmp_service->__set_version(version);
  }
  int ret = FAILURE;
  for (std::vector<SGService>::const_iterator iter = service->serviceList.begin();
       service->serviceList.end() != iter; ++iter) {
    SGService iservice;
    ret = CheckArgs(*iter, iservice);
    if (SUCCESS != ret) {
      LOG_WARN("the service param is illegal, ret = " << ret);
      continue;
    } else {
      tmp_service->serviceList.push_back(iservice);
    }
  }
  if (tmp_service->serviceList.empty()) {
    LOG_WARN("input servicelist are all illegal.");
  }
  m_origin_srvlist_cache->insert(key, *tmp_service);
  ret = UpdateBufferServiceFilted(service->localAppkey,
                                             service->remoteAppkey,
                                             service->protocol);
  if (SUCCESS != ret) {
    LOG_ERROR("Failed to filte service list. localAppkey :" << service->localAppkey
                                                            << ", remoteAppkey :" << service->remoteAppkey
                                                            << ", protocol :" << service->protocol
                                                            << ", ret :" << ret);
  }
  return ret;
}


void MnsImpl::PrintUpdateDiff(ServicePtr inServiceList, ServicePtr outServiceList) {

  const int LAST_UPDATE_LEN = 32;
  std::vector<SGService> inSvrList = inServiceList->serviceList;
  std::vector<SGService> outSvrList = outServiceList->serviceList;
  std::map<std::string, SGService> tmpMap;
  START_TIME
  for (std::vector<SGService>::iterator inItor = inSvrList.begin(); inItor != inSvrList.end(); inItor++) {
    std::string keyIn = inItor->ip + boost::lexical_cast<std::string>(inItor->port);
    tmpMap.insert(std::pair<std::string, SGService>(keyIn, *inItor));
  }
  for (std::vector<SGService>::iterator outItor = outSvrList.begin(); outItor != outSvrList.end(); outItor++) {
    SGService sgTmp;
    std::map<std::string, SGService>::iterator itor;
    std::string keyOut = "";
    try {
      keyOut = outItor->ip + boost::lexical_cast<std::string>(outItor->port);
    }
    catch (bad_lexical_cast &e) {
      LOG_ERROR("to convert the lexical failed" << e.what());
    }
    itor = tmpMap.find(keyOut);
    if (itor != tmpMap.end()) {
      if ((outItor->status != itor->second.status) || (outItor->fweight != itor->second.fweight)) {
        char strTime[LAST_UPDATE_LEN] = {'0'};
        unixTime2Str(outItor->lastUpdateTime, strTime, LAST_UPDATE_LEN);
        LOG_INFO("the diff srv info,"
                     << "; appkey=" << outItor->appkey << "; origin status: " << itor->second.status
                     << "; update status: " << outItor->status
                     << "; origin fweight: " << itor->second.fweight
                     << "; update fweight: " << outItor->fweight
                     << "; update ip: " << outItor->ip
                     << "; lastUpdateTime:unix time" << outItor->lastUpdateTime
                     << "; CST time = " << strTime);
      }
    }
    END_TIME("the svr list diff time")
  }
}

}
