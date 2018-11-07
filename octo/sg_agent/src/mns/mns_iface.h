#ifndef MNS_IFACE_H_
#define MNS_IFACE_H_

#include <string>
#include <vector>
#include <set>

#include "sgagent_service_types.h"
#include "sgagent_worker_service_types.h"
#include "util/sg_agent_def.h"
#include "mns_comm.h"

namespace sg_agent {

class IMnsPlugin {
 public:
  virtual ~IMnsPlugin() {};
  virtual int Init(const std::string &local_ip,
                   const std::string &mask,
                   int timeout = DEFAULT_SERVICE_TIMEOUT,
                   int retry = DEFAULT_SERVICE_RETRY) = 0;

  /**
   *
   * @param serviceList
   * @param req
   * @param is_origin_cache use origin cache or not
   * @param is_filte_swimlane filte the service list by swimlane or not
   * @return
   */
  virtual int GetSrvList(std::vector<SGService> &srvlist,
                         const ProtocolRequest &req,
                         const bool &is_origin_cache,
                         const bool &is_filte_backbone_swimlane,
                         const bool &is_filte_swimlane) = 0;

  /**
   *
   * @param service : request
   * @return
   */
  virtual int UpdateSrvList(const boost::shared_ptr<getservice_res_param_t>  &service) = 0;

  /**
   *
   * @param list_and_cache : return-data
   * @param protocol :
   * @param appkey :
   * @return
   */
  virtual int GetSrvListAndCacheSize(ServListAndCache &list_and_cache,
                                      const std::string &protocol,
                                      const std::string &appkey) = 0;

  virtual int GetAppkeyDesc(AppkeyDescResponse &desc_res,
                            const std::string &appkey)=0;


  /**
  *
  * @param service : request
  * @return
  */
  virtual int RepalceSrvlist(const boost::shared_ptr<getservice_res_param_t> &service) = 0;

  virtual void UpdateSrvListTimer() = 0;

  virtual void UpdateRouteTimer() = 0;

  virtual void UpdateSrvNameTimer() = 0;

  virtual void UpdateAppkeyDescTimer() = 0;

};

} // namespace
#endif
