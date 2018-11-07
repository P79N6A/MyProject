// =====================================================================================
//
//       Filename:  auth_client.cpp
//
//    Description:
//
//        Version:  1.0
//        Created:  2015-10
//       Revision:  none
//
// =====================================================================================
#include "auth_client.h"
#include <dlfcn.h>
#include "util/global_def.h"
#include "sgcommon_invoker.h"
extern GlobalVar *g_global_var;

using namespace muduo::net;

namespace sg_agent {

static const size_t kMaxPendingTasks = 100;

BufferMgr<getauth_res_param_t> *AuthClient::mBufferMgr = NULL;
AuthClient *AuthClient::mAuthClient = NULL;

AuthClient::AuthClient()
    : loop_(NULL),
      loop_thread_(NULL),
      timeout_(DEFAULT_AUTH_TIMEOUT) {

}

AuthClient::~AuthClient() {
  EventLoopThreadProxyDestroyer(loop_thread_);
  SAFE_DELETE(mBufferMgr);
}

AuthClient *AuthClient::getInstance() {
  if (NULL == mAuthClient) {
    mAuthClient = new AuthClient();
  }

  return mAuthClient;
}

void AuthClient::Destroy() {
  SAFE_DELETE(mAuthClient);
}

int AuthClient::init(int timeout) {
  timeout_ = timeout;

  if (NULL == mBufferMgr) {
    mBufferMgr = new BufferMgr<getauth_res_param_t>();
  }

  keySet_ = boost::shared_ptr<std::set<std::string> >(new std::set<std::string>());

  loop_thread_ = EventLoopThreadProxyMaker();
  if (NULL == loop_thread_) {
    LOG_ERROR("fail to init loop_thread_");
    return FAILURE;
  }
  // 启动backend处理线程
  loop_ = loop_thread_->startLoop();

  // 启动Timer
  boost::shared_ptr<AuthTimer> task = boost::shared_ptr<AuthTimer>(new AuthTimer(this));
  loop_->runEvery(DEFAULT_SCANTIME, task);

  return 0;
}

int AuthClient::getAuthorized(int role, std::string &_return, const std::string &targetAppkey) {
  if (NULL == mBufferMgr) {
    LOG_ERROR("auth's BufferMgr is NULL, please init it before");
    return ERR_AUTH_BUFFER_NULL;
  }

  std::string key = _genKey(role, targetAppkey);
  AuthPtr resAuthPtr(new getauth_res_param_t());
  int ret = mBufferMgr->get(key, *(resAuthPtr));
  if (0 == ret) {
    _return = resAuthPtr->content;
    return ret;
  } else {
    //未找到，需要从zk里面同步拿取一份
    std::string version = "";
    ret = SendTaskToWorker(role, targetAppkey, version);
    if (0 != ret) {
      LOG_ERROR("failed to sendAuthMsg, targetAppkey = " << targetAppkey
                                                         << ", role = " << role
                                                         << ", errorcode = " << ret);
      return ERR_FAILEDSENDMSG;
    }
  }

  /**
   * wait for return, scan buffer
   */
  timeval tvalStart;
  timeval tvalEnd;
  long deltaTime;

  gettimeofday(&tvalStart, NULL);
  do {
    ret = mBufferMgr->get(key, *(resAuthPtr));
    if (0 == ret) {
      _return = resAuthPtr->content;
      LOG_DEBUG("getAuthorized input param, appKey: " << resAuthPtr->targetAppkey << ", key = " << key);
      break;
    }
    gettimeofday(&tvalEnd, NULL);
    deltaTime = (tvalEnd.tv_sec - tvalStart.tv_sec) * 1000000L
        + (tvalEnd.tv_usec - tvalStart.tv_usec);
    usleep(DEFAULT_SLEEPTIME);
  } while (timeout_ > deltaTime);

  return ret;
}

void AuthClient::UpdateTimer() {
  //由于大量appkey不存在quota节点, 改进成10min(60 * 10)更新一次空的appkey
  int static update_count = 0;
  const int static CountTimes = 60;
  update_count = (++update_count) % CountTimes;
  LOG_DEBUG("auth update_count = " << update_count);
  bool null_node_update = (0 == update_count);

  boost::shared_ptr<std::set<std::string> > keySet;
  {
    muduo::MutexLockGuard lock(authMutexLock_);
    keySet = keySet_;
  }

  std::set<std::string>::iterator iter;
  int ret = 0;
  for (iter = keySet->begin(); iter != keySet->end(); ++iter) {
    std::string key = *iter;
    AuthPtr resAuthPtr(new getauth_res_param_t());
    ret = mBufferMgr->get(key, *resAuthPtr);
    if (0 == ret) {
      if (resAuthPtr->content.empty() && !null_node_update) {
        continue;
      }
      BackendZkHandler(resAuthPtr);
    } else {
      LOG_ERROR("Auth KeySet's key is not in the bufferMgr. key = " << key
                                                                    << ", ret = " << ret);
    }
  }
}

int AuthClient::SendTaskToWorker(int role, const std::string &targetAppkey, const std::string &version) {
  AuthPtr pRes(new getauth_res_param_t());
  pRes->__set_role(role);
  pRes->__set_targetAppkey(targetAppkey);
  pRes->__set_version(version);

  size_t pending_tasks_size = loop_->queueSize();

  if (pending_tasks_size < kMaxPendingTasks) {
    boost::shared_ptr<AuthTask> task = boost::shared_ptr<AuthTask>(new AuthTask(this, pRes));
    loop_->runInLoop(task);
  } else {
    LOG_WARN("quota backend thread overload, task queue size: "
                 << pending_tasks_size);
    return -1;
  }

  return 0;
}

void AuthClient::BackendZkHandler(AuthPtr auth) {
  if (NULL == mBufferMgr) {
    LOG_ERROR("auth's bufferMgr is NULL, please init before");
    return;
  }

  std::string key = _genKey(auth->role, auth->targetAppkey);
  //get From zk
  int ret = GetFromZk(auth);
  if (0 == ret || ERR_NODE_NOTFIND == ret) {
    if (ERR_NODE_NOTFIND == ret) {
      LOG_WARN("can not find auth node from zk"
                   << ", Appkey = " << auth->targetAppkey);
    }
  } else {
    LOG_ERROR("getAuth fail , role: " << auth->role
                                      << ", targetAppkey: " << auth->targetAppkey
                                      << ", ret: " << ret);
    return;
  }

  //write cache
  mBufferMgr->insert(key, *auth);
  {
    muduo::MutexLockGuard lock(authMutexLock_);
    if (!keySet_.unique()) {
      keySet_.reset(new std::set<std::string>(*keySet_));
    }

    keySet_->insert(key);
  }

  LOG_INFO("Receive Auth task in worker thread! key = " << key);

}

int AuthClient::GetFromZk(AuthPtr auth) {
  std::string name = "";
  if (AUTH_PROVIDER == auth->role) {
    name = "provider";
  } else if (AUTH_CONSUMER == auth->role) {
    name = "consumer";
  } else {
    LOG_ERROR("Auth role is not correct; role = " << auth->role);
    return ERR_AUTH_ROLE_WRONG;
  }

  char zkPath[MAX_BUF_SIZE] = {0};
  snprintf(zkPath, sizeof(zkPath), "/mns/sankuai/%s/%s/auth/%s",
           g_global_var->gEnvStr.c_str(), auth->targetAppkey.c_str(), name.c_str());
  struct Stat stat;
  int datalen = kZkContentSize;
  ZkGetInvokeParams zk_get_param;
  ZkGetRequest zk_get_req;
  zk_get_req.path = zkPath;
  zk_get_req.watch = 0;
  zk_get_param.zk_get_request = zk_get_req;
  int ret = ZkClient::getInstance()->ZkGet(&zk_get_param);
  if (ZOK != ret) {
    LOG_WARN("zoo_get auth fail, zkpath is : " << zkPath
                                               << ", targetAppkey: " << auth->targetAppkey
                                               << ", role: " << name
                                               << ", ret : " << ret);
    auth->content = "";
    return ret;
  }
  auth->content = zk_get_param.zk_get_response.buffer;

  return 0;
}
} //namespace
