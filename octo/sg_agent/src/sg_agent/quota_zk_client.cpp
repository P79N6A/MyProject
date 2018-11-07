// =====================================================================================
// 
//       Filename:  QuotaZkClient.cpp
// 
//    Description:  
// 
//        Version:  1.0
//        Created:  2014-08-27 16时43分26秒
//       Revision:  none
// 
// =====================================================================================
#include "quota_zk_client.h"
#include "util/json_zk_mgr.h"
#include "comm/log4cplus.h"
#include "util/sg_agent_def.h"
#include "util/SGAgentErr.h"
#include "quota_client.h"

#include "zk_tools.h"
#include "zk_client.h"

QuotaZkClient::QuotaZkClient() {
}

QuotaZkClient::~QuotaZkClient() {
}

/*
 * 功能：从zk中获取配额信息
 *
 * 核心逻辑：先取quota节点静态信息，判断是否有更新，若无更新直接返回；
 *           否则，就去ZK中获取quota下面所有子节点，拼成DegradeActions列表
 *
 *   by xuzhangjian@meituan
 *
 */
int QuotaZkClient::getDegradeActions(std::vector<DegradeAction> &degradeActions,
                                     const std::string &localAppkey,
                                     const std::string &appKey,
                                     std::string &version) {
  LOG_DEBUG("getDegradeActions input , appkey : " << appKey
                                                  << ", localAppkey: " << localAppkey);

  char zkPath[MAX_BUF_SIZE] = {0};
  snprintf(zkPath, sizeof(zkPath), "%s/%s/quota",
           ZkTools::get_root_path().c_str(), appKey.c_str());

  std::string str_json;
  int datalen = kZkContentSize;
  struct Stat stat;
  struct Stat stat_child;

  //先从quota节点获取stat信息，用于对比版本信息
  ZkWGetRequest zk_wget_req;
  ZkWGetInvokeParams zk_wget_params;
  zk_wget_req.path = zkPath;
  zk_wget_req.watch = quotaWatcher;
  zk_wget_req.watcherCtx = NULL;
  zk_wget_params.zk_wget_request = zk_wget_req;
  int ret = ZkClient::getInstance()->ZkWGet(&zk_wget_params);
  if (ZOK != ret) {
    LOG_ERROR("zk_wget fail, ret:  " << ret
                                     << ", remoteAppkey: " << appKey
                                     << " , zkpath : " << zkPath);
    return ret;
  }

  str_json = zk_wget_params.zk_wget_response.buffer;
  datalen = zk_wget_params.zk_wget_response.buffer_len;
  stat = zk_wget_params.zk_wget_response.stat;

  //构造ZK provider新版本信息
  std::string zk_version = getVersion(stat.mtime, stat.cversion, stat.version);
  ret = zk_version_check_.CheckZkVersion(appKey, zk_version, version);
  if (0 != ret) {
    LOG_DEBUG("ZK degradeActions is the same as buf, key : " << appKey
                                                             << ", version : " << version);
    return ret;
  }

  //从ZK中获取新数据
  ZkWGetChildrenRequest zk_wget_children_req;
  ZkWGetChildrenInvokeParams zk_wget_children_params;
  zk_wget_children_req.path = zkPath;
  zk_wget_children_req.watch = quotaWatcher;
  zk_wget_children_req.watcherCtx = NULL;
  zk_wget_children_params.zk_wgetchildren_request = zk_wget_children_req;
  ret = ZkClient::getInstance()->ZkWGetChildren(&zk_wget_children_params);
  if (ZOK != ret) {
    LOG_WARN("getDegradeActions.zoo_wget_children fail, ret: " << ret
                                                               << ", zkPath = " << zkPath
                                                               << ", appKey = " << appKey);
    return ret;
  } else if (zk_wget_children_params.zk_wgetchildren_response.count == 0) {
    LOG_INFO("getDegradeActions strings.count = 0, ret: " << ret
                                                          << " zkPath is :" << zkPath);
    return 0;
  }

  //如果子节点非空，则遍历所有子节点，构造返回服务列表
  for (int i = 0; i < zk_wget_children_params.zk_wgetchildren_response.count; i++) {
    datalen = datalen > kZkContentSize ? datalen : kZkContentSize;
    snprintf(zkPath, sizeof(zkPath), "%s/%s/quota/%s",
             ZkTools::get_root_path().c_str(), appKey.c_str(),
             zk_wget_children_params.zk_wgetchildren_response.data[i].c_str());

    std::string strJson;
    ZkGetInvokeParams zk_get_param;
    ZkGetRequest zk_get_req;
    zk_get_req.path = zkPath;
    zk_get_req.watch = 0;
    zk_get_param.zk_get_request = zk_get_req;
    ret = ZkClient::getInstance()->ZkGet(&zk_get_param);
    if (ZOK != ret) {
      LOG_ERROR("getDegradeActions.zoo_wget children " << i
                                                       << ", fail " << ret
                                                       << ", zkpath is : " << zkPath);
      return ret;
    }

    if (zk_get_param.zk_get_response.buffer.empty()
        || 0 == datalen) {
      continue;
    }
    strJson = zk_get_param.zk_get_response.buffer;

    if (JsonZkMgr::Json2DegradeActions(strJson, degradeActions) < 0) {
      LOG_ERROR("getDegradeActions Json2DegradeActions fail. strJson " << strJson
                                                                       << ", zkpath is : " << zkPath);
    }
  }

  LOG_DEBUG("getDegradeActions END , SIZE : " << degradeActions.size());

  return 0;
}

void QuotaZkClient::quotaWatcher(zhandle_t *zh, int type, int state, const char *path, void *watcherCtx) {

  //ZK watcher触发，获取新的服务列表，并更新消息队列
  if (strlen(path) == 0 || type == -1) {
    LOG_FATAL("get event quotaWatcher,  that ZK server may down! state = " << state
                                                                           << "type = " << type);
    return;
  }
  /// Extract appkey from zkpath
  int prefixlen = ZkTools::get_root_path().size();
  std::string path_tmp = path + prefixlen + 1;
  std::size_t found = path_tmp.find_first_of("/");

  if (std::string::npos == found) {
    LOG_ERROR("can not find the charset,path: "<<path<<", prefixlen"<<prefixlen
    );
    return;
  }
  std::string appkey;
  appkey.assign(path + prefixlen + 1, found);
  if ("" == appkey) {
    LOG_ERROR("watcher getDegradeActions appkey is NULL! path_tmp = " << path_tmp);
    return;
  }

  //TODO:EventLoop SendTask
  std::string localAppkey = "sg_agent_quota_watcher";
  std::string version = "";
  //未找到,交给后端线程去处理
  int ret = sg_agent::QuotaClient::getInstance()->SendTaskToWorker(localAppkey, appkey, version);
  if (0 != ret) {
    LOG_ERROR("failed to Send quota task, remoteAppkey = " << appkey
                                                           << "errorcode = " << ret);
  }

  LOG_DEBUG("quotaWatcher trigger! state = " << state
                                             << ", appKey = " << appkey
                                             << ", zkPath = " << path);
}
