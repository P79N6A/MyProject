// =====================================================================================
//
//      Filename:  protocol_service_client.h
//
//
//      Version:  1.0
//      Revision:  none
//
//
// =====================================================================================

#ifndef MNS_IMPL_H_
#define MNS_IMPL_H_

#include <pthread.h>
#include <muduo/base/Mutex.h>
#include <muduo/base/Atomic.h>

#include <boost/bind.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/algorithm/string/trim.hpp>
#include <comm/buffer_mgr.h>
#include <boost/make_shared.hpp>
#include "mns_zk_client.h"
#include "mns_iface.h"
#include "comm/log4cplus.h"
#include "util/SGAgentErr.h"
#include "mns_comm.h"
namespace sg_agent {

typedef boost::shared_ptr<getservicename_res_param_t> ServiceNamePtr;
typedef boost::shared_ptr<getservice_res_param_t> ServicePtr;
typedef boost::shared_ptr<getroute_res_param_t> RoutePtr;

class MnsImpl : public IMnsPlugin {
 public:

  static void *Create();
  static void Destroy(void *p);

  MnsImpl();
  virtual ~MnsImpl();

  int Init(const std::string &local_ip,
           const std::string &mask,
           int timeout = DEFAULT_SERVICE_TIMEOUT,
           int retry = DEFAULT_SERVICE_RETRY);

  int GetSrvList(std::vector<SGService> &srv_list,
                 const ProtocolRequest &req,
                 const bool &is_origin_cache,
                 const bool &is_filte_backbone_swimlane,
                 const bool &is_filte_swimlane);
  /**
   * @param AppkeyDescResponse
   * @param appkey
   * @return
   * */
  int GetAppkeyDesc(AppkeyDescResponse &desc_res,
                    const std::string &appkey);

  int GetSrvListFromZk(ServicePtr service, bool is_watcher_callback = false);

  //current support by http client
  int UpdateSrvList(const ServicePtr &service);
  int GetSrvListAndCacheSize(ServListAndCache& list_and_cache,
                                     const std::string &protocol,
                                     const std::string &appkey);
  int RepalceSrvlist(const ServicePtr &service);

	// timers
  void UpdateSrvListTimer();
  void UpdateRouteTimer();
  void UpdateSrvNameTimer();
  void UpdateAppkeyDescTimer();

  ServiceZkClient *GetZkClient() {
    return &m_zk_client;
  }
 private:


  // 获取优先级最高的分组
  int GetRouteList(const std::string &localAppkey,
                   const std::string &remote_appkey, const std::string &protocol);

  /**
   *
   * @param serviceList
   * @param req
   * @param is_filter 标志位，是否安装服务分组过滤服务列表
   * @return
   */
  int GetSrvListByAppkey(std::vector<SGService> &srv_list,
                         const ProtocolRequest &req,
                         const bool &is_origin_cache,
                         const bool &is_filte_backbone_swimlane,
                         const bool &is_filte_swimlane);

  int DoGetSrvList(std::vector<SGService> &srv_list,
                   const ProtocolRequest &req,
                   const bool &is_origin_cache,
                   const bool &is_filte_backbone_swimlane,
                   const bool &is_filte_swimlane);

  int GetSrvListBySrvName(std::vector<SGService> &serviceList,
                          const ProtocolRequest &req,
                          const bool &is_origin_cache,
                          const bool &is_filte_backbone_swimlane,
                          const bool &is_filte_swimlane);

  int DoGetSrvListBySrvName(std::vector<SGService> &srvlist,
                            const ProtocolRequest &req,
                            const bool &is_origin_cache,
                            const bool &is_filte_backbone_swimlane,
                            const bool &is_filte_swimlane);

  int GetRouteFromZk(RoutePtr route);
  int GetSrvNameFromZk(ServiceNamePtr servicename);
  int GetAppkeyDescFromZk(AppkeyDescResponse &desc_res,const std::string &appkey);


  // 生成buffer key
  std::string GenCacheKey(const std::string &, const std::string &protocol);

  // 更新过虑后的ServiceList
  int UpdateBufferServiceFilted(const std::string &localAppkey,
                                const std::string &remoteAppkey,
                                const std::string &protocol,
                                const bool &is_update_route = true);
  // check param
  int CheckArgs(const SGService &oservice, SGService &iservice);

  void PrintUpdateDiff(ServicePtr inServiceList,ServicePtr outServiceList);


  // sg_agent本机ip
  std::string m_local_ip;
  // sg_agent本机mask
  std::string m_local_mask;
  int m_retry;

  int m_timeout;
  ServiceZkClient m_zk_client;


  static BufferMgr<getroute_res_param_t> *m_route_cache;

  static BufferMgr<getservice_res_param_t> *m_origin_srvlist_cache;


  // 用于存储分组后的serviceList
  static BufferMgr<std::vector<SGService> > *m_filted_srvlist_cache;

  static BufferMgr<getservicename_res_param_t> *m_srvname_cache;

  static BufferMgr<AppkeyDescResponse> *m_appkeydesc_cache;

  boost::shared_ptr<std::set<std::string> > m_srv_keys;
  muduo::MutexLock m_srv_mutex_lock;

  boost::shared_ptr<std::set<std::string> > m_srvname_keys;
  muduo::MutexLock m_srvname_mutex_lock;

  boost::shared_ptr<std::set<std::string> > m_appkeydesc_keys;
  muduo::MutexLock m_appkeydesc_mutex_lock;
};

} // namespace


#endif
