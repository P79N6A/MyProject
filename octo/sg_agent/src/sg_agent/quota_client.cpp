// =====================================================================================
//
//       Filename:  quota_client.cpp
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-06-16
//       Revision:  none
//
// =====================================================================================

#include "quota_client.h"
#include <dlfcn.h>
#include "sgcommon_invoker.h"

using namespace muduo::net;

namespace sg_agent {

static const size_t kMaxPendingTasks = 100;

BufferMgr<quota_res_param_t> *QuotaClient::mBufferMgr = NULL;
QuotaClient *QuotaClient::mQuotaClient = NULL;

QuotaClient::QuotaClient()
    : loop_(NULL),
      loop_thread_(NULL),
      timeout_(DEFAULT_QUOTA_TIMEOUT) {
}

QuotaClient::~QuotaClient() {
  EventLoopThreadProxyDestroyer(loop_thread_);
  SAFE_DELETE(mBufferMgr);
}

QuotaClient *QuotaClient::getInstance() {
  if (NULL == mQuotaClient) {
    mQuotaClient = new QuotaClient();
  }

  return mQuotaClient;
}

void QuotaClient::Destroy() {
  SAFE_DELETE(mQuotaClient);
}

int QuotaClient::init(int timeout) {
  timeout_ = timeout;

  if (NULL == mBufferMgr) {
    mBufferMgr = new BufferMgr<quota_res_param_t>();
  }

  keySet_ = boost::shared_ptr<std::set<std::string> >(new std::set<std::string>());

  loop_thread_ = EventLoopThreadProxyMaker();
  if (NULL == loop_thread_) {
    LOG_ERROR("fail to init loop_thread_");
    return FAILURE;
  }
  // 启动backend处理线程
  loop_ = loop_thread_->startLoop();

  // TODO:启动Timer
  boost::shared_ptr<UpdateHandler> task(new UpdateHandler(this));
  loop_->runEvery(DEFAULT_SCANTIME, task);

  return 0;
}

int QuotaClient::getActions(std::vector<DegradeAction> &actions,
                            const std::string &localAppKey,
                            const std::string &appKey) {
  if (NULL == mBufferMgr) {
    LOG_ERROR("quota's BufferMgr is NULL, please init it before");
    return ERR_QUOTA_BUFFER_NULL;
  }

  std::string key = _genKey(localAppKey, appKey);
  QuotaPtr resQuotaPtr(new quota_res_param_t());
  int ret = mBufferMgr->get(key, *(resQuotaPtr));
  if (0 == ret) {
    actions = resQuotaPtr->actions;
    return ret;
  } else {
    std::string version = "";
    ret = SendTaskToWorker(localAppKey, appKey, version);
    if (0 != ret) {
      LOG_ERROR("failed to Send quota task, remoteAppkey = " << appKey
                                                             << ", localAppkey = " << localAppKey
                                                             << ", errorcode = " << ret);
      return ERR_FAILEDSENDMSG;
    }
  }

  /**
   * 在超时时间内，轮询buffer
   */
  timeval tvalStart;
  timeval tvalEnd;
  long deltaTime;

  gettimeofday(&tvalStart, NULL);
  do {
    ret = mBufferMgr->get(key, *(resQuotaPtr));
    if (0 == ret) {
      actions = resQuotaPtr->actions;
      LOG_DEBUG("getActions input param, appKey: " << localAppKey.c_str()
                                                   << ", remoteAppkey: " << appKey.c_str()
                                                   << ", getActionsFromWorker , list size : " << actions.size());
      break;
    }
    gettimeofday(&tvalEnd, NULL);
    deltaTime = (tvalEnd.tv_sec - tvalStart.tv_sec) * 1000000L
        + (tvalEnd.tv_usec - tvalStart.tv_usec);
    usleep(DEFAULT_SLEEPTIME);
  } while ((long) timeout_ > deltaTime);

  if (0 != ret) {
    LOG_WARN("failed to get quota. maybe worker thread getServiceList  key = " << key
                                                                               << ",ret = " << ret);
  }

  return ret;
}

void QuotaClient::UpdateTimer() {
  //由于大量appkey不存在quota节点, 改进成10min(60 * 10)更新一次空的appkey
  int static update_count = 0;
  const int static CountTimes = 60;
  update_count = (++update_count) % CountTimes;
  LOG_DEBUG("quota update_count = " << update_count);
  bool null_node_update = (0 == update_count);

  int ret = 0;
  boost::shared_ptr<std::set<std::string> > keySet;
  {
    muduo::MutexLockGuard lock(quotaMutexLock_);
    keySet = keySet_;
  }
  std::set<std::string>::iterator iter;
  for (iter = keySet->begin(); iter != keySet->end(); ++iter) {
    std::string remoteAppkey = *iter;
    QuotaPtr resQuotaPtr(new quota_res_param_t());
    ret = mBufferMgr->get(remoteAppkey, *(resQuotaPtr));
    if (0 == ret) {
      if (resQuotaPtr->actions.empty() && !null_node_update) {
        continue;
      }
      //在buffer里面存在，直接更新
      BackendZkHandler(resQuotaPtr);
    } else {
      LOG_ERROR("KeySet's key is not in the bufferMgr. key = " << remoteAppkey
                                                               << ", ret = " << ret);
    }
  }
}

int QuotaClient::SendTaskToWorker(const std::string &localAppkey,
                                  const std::string &remoteAppkey,
                                  const std::string &version) {
  QuotaPtr pRes(new quota_res_param_t());
  pRes->__set_localAppkey(localAppkey);
  pRes->__set_remoteAppkey(remoteAppkey);
  pRes->__set_version(version);

  size_t pending_tasks_size = loop_->queueSize();

  boost::shared_ptr<ZkHandler> task(new ZkHandler(this, pRes));
  if (pending_tasks_size < kMaxPendingTasks) {
    loop_->runInLoop(task);
  } else {
    LOG_WARN("quota backend thread overload, task queue size: "
                 << pending_tasks_size);
    return -1;
  }

  return 0;
}

void QuotaClient::BackendZkHandler(QuotaPtr quota) {
  if (NULL == mBufferMgr) {
    LOG_ERROR("quota's bufferMgr is NULL, please init before");
    return;
  }

  std::vector<DegradeAction> actions;
  //get From zk
  int ret = zkClient_.getDegradeActions(actions, quota->localAppkey, quota->remoteAppkey, quota->version);
  if (0 == ret || ERR_NODE_NOTFIND == ret) {
    if (ERR_NODE_NOTFIND == ret) {
      LOG_WARN("can not find degradeAction node from zk"
                   << ", remoteAppkey = " << quota->remoteAppkey
                   << ", localAppkey = " << quota->localAppkey);
      // return empty list
      actions.clear();
    } else {
      LOG_DEBUG("succeed to get degradeAction from zk"
                    << ", appkey = " << quota->remoteAppkey
                    << ", localAppkey = " << quota->localAppkey
                    << ", actions' size = " << actions.size());
    }
    //只有在这两种情况下才对quota的actions赋值
    quota->__set_actions(actions);
  } else if (ret == ERR_ZK_LIST_SAME_BUFFER) {
    LOG_DEBUG("ZK getDegradeActions is the same as buf, localAppkey : " << quota->localAppkey
                                                                        << ", remoteAppkey is : " << quota->remoteAppkey
                                                                        << ", version : " << quota->version);
    return;
  } else {
    LOG_ERROR("getDegradeActions fail , appkey : " << quota->remoteAppkey.c_str()
                                                   << ", localAppkey: " << quota->localAppkey
                                                   << ", ret: " << ret);
    return;
  }

  //write cache
  std::string key = _genKey(quota->localAppkey, quota->remoteAppkey);
  mBufferMgr->insert(key, *quota);
  //write keySet
  {
    muduo::MutexLockGuard lock(quotaMutexLock_);
    if (!keySet_.unique()) {
      keySet_.reset(new std::set<std::string>(*keySet_));
    }

    keySet_->insert(key);
  }
  LOG_DEBUG("Recevie Quota Task in Worker thread, key = " << key
                                                          << " , size : " << quota->actions.size());
}

} //namespace
