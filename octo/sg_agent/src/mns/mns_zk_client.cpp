// =====================================================================================
// 
//       Filename:  ServiceZkClient.cpp
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-08-27 16时43分26秒
//       Revision:  none
// 
// =====================================================================================
#include "mns_zk_client.h"
#include "util/json_zk_mgr.h"
#include "util/sg_agent_def.h"

#include "mns_zk_tools.h"
#include "util/appkey_path_operation.h"

#include "mns_impl.h"
#include "sg_agent/service_def.h"
#include <core/plugindef.h>
#include "util/global_def.h"

using namespace sg_agent;

extern HostServices *g_host_services;
extern GlobalVar *g_mns_global_var;
//zk provider节点个数，大于NUM_PROVIDER, 则访问MNSCache服务
static const int kAccessMnscNodeNums = 46;
//zk provider节点数大于NUM_CHILD且watcher触发，则需要sleep，等待MNSC同步数据
static const unsigned int kMaxSleepNodeNums = 2000;
static const unsigned int kMaxSleepTime = 8000000;
muduo::net::EventLoop* ServiceZkClient::s_watcher_loop = NULL;
muduo::net::EventLoopThread ServiceZkClient::s_watcher_thread;

ServiceZkClient::ServiceZkClient(MnsImpl *p) {
  plugin = p;
	s_watcher_loop = s_watcher_thread.startLoop();
}


ServiceZkClient::~ServiceZkClient() {

}

/*
 * 功能：从zk中去获取servicename对应的appkey list
 *
 */
int ServiceZkClient::GetAppkeyByServiceName(std::set<std::string> &appkeys,
                                            const std::string &localAppkey,
                                            const std::string &serviceName,
                                            std::string &version,
                                            const std::string &protocol) {
  char zkPath[MAX_BUF_SIZE] = {0};
  int ret = MnsZkTools::operation_.genServiceNameZkPathNode(zkPath, serviceName, protocol);
  if (0 != ret) {
    LOG_ERROR("_gen genServiceNameZkPathNode fail! protocol is empty! zkPath: "
                  << zkPath);
    return ret;
  }
  LOG_DEBUG("getAppkeyByServiceName _gen zkPath: "
                << zkPath);

  std::string strJson;

  ZkGetRequest req;
  ZkGetInvokeParams param;
  req.path = zkPath;
  req.watch = 0;
  param.zk_get_request = req;
  //先从provider节点获取stat信息，用于版本更新比较标示
  ret = g_host_services->invoke_service_func(sg_agent::ZK_GET, &param);
  if (SUCCESS != ret) {
    // the zkPath contains necessary info
    LOG_ERROR("fail to getAppkeyByServiceName, path = " << zkPath << ", ret = " << ret);
    return ret;
  } else if (ZOK != param.zk_get_response.err_code) {
    LOG_ERROR("fail to getAppkeyByServiceName, path = " << zkPath << ", err_code = " << param.zk_get_response.err_code);
    return param.zk_get_response.err_code;
  }

  strJson = param.zk_get_response.buffer;
  ServiceNode oserviceNode;
  ret = JsonZkMgr::Json2ServiceNode(strJson, oserviceNode);
  if (0 != ret) {
    LOG_ERROR("getAppkeyByServiceName _Json2ServiceNode fail, Json:  "
                  << strJson
                  << ", ret = " << ret);
    return ret;
  }
  appkeys = oserviceNode.appkeys;
  return 0;
}
/*
 * 功能：从zk中去获取appkey的desc信息
 */
int ServiceZkClient::GetAppkeyDesc(AppkeyDescResponse &desc_res,const std::string &appkey){

  char zkPath[MAX_BUF_SIZE] = {0};

  int ret = MnsZkTools::operation_.genDescZkPath(zkPath,appkey);
  if (SUCCESS != ret) {
    LOG_ERROR("_gen genDescZkPath fail! desc is empty! zkPath: "
                  << zkPath);
    return ret;
  }
  LOG_DEBUG("GetAppkeyDesc _gen zkPath: "
                << zkPath);

  std::string strJson;

  ZkGetInvokeParams param;
  param.zk_get_request.path = zkPath;
  param.zk_get_request.watch = 0;

  ret = g_host_services->invoke_service_func(sg_agent::ZK_GET, &param);
  if (SUCCESS != ret) {
    LOG_ERROR("fail to GetAppkeyDesc, path = " << zkPath << ", ret = " << ret);
    return ret;
  } else if (ZOK != param.zk_get_response.err_code) {
    LOG_ERROR("fail to GetAppkeyDesc, path = " << zkPath << ", err_code = " << param.zk_get_response.err_code);
    desc_res.msg = "failed to get desc from zk";
    return param.zk_get_response.err_code;
  }

  strJson = param.zk_get_response.buffer;
  ret = JsonZkMgr::Json2AppkeyDescNode(strJson, desc_res.desc);
  if (SUCCESS != ret) {
    LOG_ERROR("get appkey desc _Json2ServiceNode fail, Json:  "
                  << strJson
                  << ", ret = " << ret);
    desc_res.msg = "_Json2ServiceNode failed";
    return ret;
  }
  if(desc_res.desc.appkey != appkey ){

    LOG_WARN("the request appkey is not same zk exit node appkey ");
    desc_res.msg = "the request appkey is not same zk exit node appkey";
    return ret;
  }
  return SUCCESS;

}

/**
 *
 * @param srvlist - if SUCCESS, srv_list will be updated
 * @param localAppkey
 * @param appKey
 * @param cur_cache_version - if SUCCESS, cur_cache_version will be updated.
 * @param protocol
 * @param is_watcher_callback
 * @return
 */
int ServiceZkClient::GetSrvListByProtocol(std::vector<SGService> &srvlist,
                                          const std::string &local_appkey,
                                          const std::string &remote_appkey,
                                          std::string &cur_cache_version,
                                          const std::string &protocol,
                                          bool is_watcher_callback) {
  // clear the srv_list
  srvlist.clear();

  // generate the zk path of provider
  char provider_path[MAX_BUF_SIZE] = {0};
  std::string node_type = "provider";
  int ret = MnsZkTools::operation_.GenProtocolZkPath(provider_path, remote_appkey, protocol, node_type);
  if (SUCCESS != ret) {
    // already log genProtocolZkPath
    return ret;
  }

  std::string provider_path_str = provider_path;
  // wget the data of provider path
  ZkWGetInvokeParams zk_wget_params;

  zk_wget_params.zk_wget_request.path = provider_path_str;
  if (MnsZkTools::whiteListMgr.IsAppkeyInWhitList(remote_appkey)) {
    zk_wget_params.zk_wget_request.watch = NULL;
  } else {
    zk_wget_params.zk_wget_request.watch = ServiceByProtocolWatcher;
    zk_wget_params.zk_wget_request.watcherCtx = plugin;
  }

  ret = g_host_services->invoke_service_func(ZK_WGET, &zk_wget_params);
  if (ZOK != ret) {
    LOG_ERROR("fail to zk wget. ret = " << ret << ", path = " << provider_path);
    return ret;
  }

  struct Stat stat = zk_wget_params.zk_wget_response.stat;
  std::string zk_version = getVersion(stat.mtime, stat.cversion, stat.version);
  ret = zk_version_check_.CheckZkVersion(remote_appkey + protocol, zk_version, cur_cache_version);
  if (SUCCESS != ret) {
    LOG_DEBUG("there is nothing to do, because the zk data is not changed. appkey = "
                  << remote_appkey
                  << ", protocol = " << protocol
                  << ", version = "
                  << cur_cache_version);
    return ret;
  }

  // get data from mnsc, while the node nums >= kAccessMnscNodeNums,kAccessMnscNodeNums
  int node_nums = stat.numChildren;
  if (g_mns_global_var->gMnscAppkey != remote_appkey && node_nums >= kAccessMnscNodeNums
      && ("http" == protocol || "thrift" == protocol) && g_mns_global_var->isOpenMNSCache) {

    MNSCacheRequest req;
    req.serviceList = &srvlist;
    req.providerSize = node_nums;
    req.appkey = &remote_appkey;
    req.version = &cur_cache_version;
    req.env = &g_mns_global_var->gEnvStr;
    req.protocol = &protocol;
    if (is_watcher_callback) {
      // node_nums is always larger than kAccessMnscNodeNums(46)
      unsigned int
          sleep_time = node_nums >= kMaxSleepNodeNums ?
                       kMaxSleepTime : node_nums * (kMaxSleepTime / kMaxSleepNodeNums);
      usleep(sleep_time);
    }
    int ret_code = g_host_services->invoke_service_func(GET_MNS_CACHE, &req);

    if ((sg_agent::MNSC_OK == ret_code) && (srvlist.size() == node_nums)) {
      LOG_DEBUG("succeed to get serviceList from mnsc, ret = " << ret_code);
      return SUCCESS;
    } else {
      LOG_WARN("fail to get data from mnsc, now try to get from zk. ret: " << ret_code);
    }
  }

  // get data from zk.
  ZkWGetChildrenInvokeParams wg_child_params;
  wg_child_params.zk_wgetchildren_request.path = provider_path_str;
  if (MnsZkTools::whiteListMgr.IsAppkeyInWhitList(remote_appkey)) {
    wg_child_params.zk_wgetchildren_request.watch = NULL;
  } else {
    wg_child_params.zk_wgetchildren_request.watch = ServiceByProtocolWatcher;
    wg_child_params.zk_wgetchildren_request.watcherCtx = plugin;
  }

  ret = g_host_services->invoke_service_func(ZK_WGET_CHILDREN, &wg_child_params);
  if (ZOK != ret) {
    LOG_ERROR("fail to get children nodes from zk. ret = " << ret << ", path = " << provider_path);
    return ret;
  }

  // get data from zk
  START_TIME
  for (int i = 0; i < wg_child_params.zk_wgetchildren_response.count; ++i) {
    std::string zk_node_path = provider_path_str + "/" + wg_child_params.zk_wgetchildren_response.data[i];

    //子节点不加watcher, 减少watcher数量
    ZkGetInvokeParams param;
    param.zk_get_request.path = zk_node_path;
    param.zk_get_request.watch = 0;
    ret = g_host_services->invoke_service_func(sg_agent::ZK_GET, &param);
    if (ERR_NODE_NOTFIND == ret) {
      LOG_WARN("the service node has been deleted, ignore it and continue updating service list. path = " << zk_node_path);
      continue;
    } else if (ZOK != ret) {
      LOG_ERROR("fail to get zk data. ret = " << ret << ", path = : " << zk_node_path);
      return ret;
    }

    std::string node_json_str = param.zk_get_response.buffer;
    LOG_DEBUG("succeed to zoo_get, json: " << node_json_str);
    SGService oservice;
    ret = JsonZkMgr::Json2SGService(node_json_str, oservice);
    if (ret != 0) {
      LOG_WARN("fail to parse node json str. "
                   << ", ret = " << ret
                   << ", path = " << zk_node_path
                   << ", json = " << node_json_str);
      continue;
    }

    /// double check
    if (oservice.appkey != remote_appkey) {
      LOG_WARN("expected appkey: " << remote_appkey << ", but node.appky = " << oservice.appkey << ", path = "
                                   << zk_node_path);
      continue;
    }
    //update srvlist
    srvlist.push_back(oservice);
  }
  END_TIME("get service list from zk")

  // 服务节点下线或者反序列化失败
  if (wg_child_params.zk_wgetchildren_response.count != srvlist.size()) {
    LOG_WARN("srvlist size is " << srvlist.size()
                                << ", childnode num is " << wg_child_params.zk_wgetchildren_response.count
                                << ". Json failed or nodes have been deleted.");
    //todo return error-code
  }
  return SUCCESS;
}

/* *
 * 从ZK中获取分组列表信息
 * */
int ServiceZkClient::getRouteListByProtocol(std::vector<CRouteData> &routeList,
                                            const std::string &localAppkey,
                                            const std::string &appKey,
                                            std::string &version,
                                            const std::string &protocol) {
  char zkPath[MAX_BUF_SIZE] = {0};
  std::string nodeType = "route";
  int ret = MnsZkTools::operation_.GenProtocolZkPath(zkPath, appKey, protocol, nodeType);
  if (0 != ret) {
    return ret;
  }
  LOG_DEBUG("_gen getRoute zkPath: " << zkPath << ", nodeType: " << nodeType);

  int datalen = kZkContentSize;
  std::string strJson;
  struct Stat stat;
  struct Stat stat_child;

  //TODO:
  ZkGetRequest req;
  ZkGetInvokeParams param;
  req.path = zkPath;
  req.watch = 0;
  param.zk_get_request = req;
  ret = g_host_services->invoke_service_func(sg_agent::ZK_GET, &param);
  if (ZOK != ret) {
    LOG_WARN("route zoo_get znode fail ret: " << ret
                                              << ", appKey: " << appKey
                                              << ", protocol: " << protocol
                                              << " zkpath : " << zkPath);
    return ret;
  } else if (ERR_NODE_NOTFIND == param.zk_get_response.err_code) {
    LOG_WARN("fail to get route data, because the zk node don't exist. ret = " << param.zk_get_response.err_code
                                                                               << ", path = "
                                                                               << param.zk_get_request.path);
    return ERR_NODE_NOTFIND;
  }

  strJson = param.zk_get_response.buffer;
  datalen = param.zk_get_response.buffer_len;
  stat = param.zk_get_response.stat;
  if (strJson.empty()) {
    LOG_ERROR("strJson is empty , cannot Json2RouteNode.");
    return ERR_DATA_TO_JSON_FAIL;
  }
  CRouteNode oroute;
  ret = JsonZkMgr::Json2RouteNode(strJson, stat.mtime, stat.version, stat.cversion, oroute);
  if (ret != 0) {
    LOG_ERROR("_Json2RouteNode fail ret = " << ret
                                            << ", json is: " << strJson.c_str());
    return ERR_DATA_TO_JSON_FAIL;
  }
  //构造ZK route节点新版本信息
  std::string zk_version = getVersion(oroute.mtime, oroute.cversion, oroute.version);
  ret = zk_version_check_route_.CheckZkVersion(appKey + protocol, zk_version, version);
  if (0 != ret) {
    LOG_DEBUG("ZK serviceList is the same as buf, key : " << appKey
                                                          << ", version : " << version);
    return ret;
  }

  //从ZK中获取新服务列表数据
  ZkWGetChildrenRequest w_child_req;
  ZkWGetChildrenInvokeParams w_child_params;
  w_child_req.path = zkPath;
  w_child_req.watch = 0;
  w_child_params.zk_wgetchildren_request = w_child_req;
  ret = g_host_services->invoke_service_func(ZK_WGET_CHILDREN, &w_child_params);
  if (ZOK != ret) {
    LOG_WARN("getRoute.zoo_wget_children fail, zkPath: " << zkPath
                                                         << ", ret = " << ret);
    return ret;
  } else if (w_child_params.zk_wgetchildren_response.count == 0) {
    LOG_WARN("getRouteList strings.count == 0 !");
    return 0;
  }

  //如果子节点非空，则遍历所有子节点，构造返回服务列表
  char zkRouteNodePath[MAX_BUF_SIZE];

  for (int i = 0; i < w_child_params.zk_wgetchildren_response.count; i++) {
    snprintf(zkRouteNodePath,
             sizeof(zkRouteNodePath),
             "%s/%s",
             zkPath,
             w_child_params.zk_wgetchildren_response.data[i].c_str());

    //获取子节点信息时，去掉watcher
    ZkGetInvokeParams param_tmp;
    param_tmp.zk_get_request.path = zkRouteNodePath;
    param_tmp.zk_get_request.watch = 0;
    ret = g_host_services->invoke_service_func(sg_agent::ZK_GET, &param_tmp);
    if (ZOK != ret) {
      LOG_ERROR("getRouteList.zoo_wget children " << i
                                                  << ", fail ret: " << ret
                                                  << " zkpath is : " << zkRouteNodePath);
      return ret;
    }

    strJson.assign(param_tmp.zk_get_response.buffer);
    datalen = param_tmp.zk_get_response.buffer_len;
    stat_child = param_tmp.zk_get_response.stat;

    if (strJson.empty() || 0 == datalen) {
      continue;
    }

    CRouteData routeData;
    ret = JsonZkMgr::Json2RouteData(strJson, routeData);
    if (SUCCESS != ret) {
      LOG_ERROR("json2routeData fail, strJson is:" << strJson.c_str()
                                                   << ", ret = " << ret);
      continue;
    }

    /// double check
    if (routeData.appkey != appKey) {
      LOG_ERROR("unexpected exception! appkey " << appKey.c_str()
                                                << "routeData.appKey " << routeData.appkey.c_str()
                                                << "datalen " << datalen);
      continue;
    }
    //将数据插入到返回列表中
    routeList.push_back(routeData);
  }

  return SUCCESS;
}

void ServiceZkClient::ServiceByProtocolWatcher(zhandle_t *zh, int type, int state, const char *path, void *watcherCtx) {
  //ZK watcher触发，获取新的服务列表，并更新消息队列
  LOG_INFO("rcv the watcher from the ZK server by protocol,path"<<path<<"type"<<type);

	if (strlen(path) == 0 || type == -1) {
    LOG_FATAL("get event serviceByProtocolWatcher,  that ZK server may down! state = " << state
                                                                                       << ", type = " << type
                                                                                       << ", path = " << path);
    return;
  } else {
    std::string path_str(path);
    LOG_INFO("zk watch trigger: path = " << path_str);
  }
  ///Extract appkey from zkpath
  std::string appkey = "";
  std::string protocol = "";
  int ret = SGAgentAppkeyPath::deGenZkPath(path, appkey, protocol);
  if (SUCCESS != ret) {
    LOG_ERROR("deGenZkPath is serviceByProtocolWatcher is wrong! path:" << path
                                                                        << ", appkey:" << appkey
                                                                        << ", protocol:" << protocol);
    return;
  }
  ServicePtr resServicePtr(new getservice_res_param_t());
  resServicePtr->__set_localAppkey("sg_agent_protocol_watcher");
  resServicePtr->__set_remoteAppkey(appkey);
  resServicePtr->__set_version("");
  resServicePtr->__set_protocol(protocol);
  MnsImpl *p = static_cast<MnsImpl *>(watcherCtx);

	LOG_INFO("zk watch trigger: appkey = " <<resServicePtr->remoteAppkey
			     << " protocol = " <<resServicePtr->protocol
					 << " queue size = " <<s_watcher_loop->queueSize());
	s_watcher_loop->runInLoop(boost::bind(&MnsImpl::GetSrvListFromZk,p,resServicePtr,true));
}



