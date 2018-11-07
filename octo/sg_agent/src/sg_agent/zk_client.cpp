// =====================================================================================
// 
//       Filename:  zk_manager.cpp
// 
//    Description: ZK连接相关管理接口实现 
// 
// =====================================================================================

#include <unistd.h>
#include <string.h>
#include <sstream>
#include "zk_client.h"
#include "comm/inc_comm.h"
#include "comm/log4cplus.h"
#include "comm/auto_lock.h"
#include "util/SGAgentErr.h"
#include "util/sg_agent_def.h"

const int kMaxPendingTasks = 1000;
const int kZkGetRetry = 3;
ZkClient *ZkClient::mZkClient = NULL;

ZkClient::ZkClient()
    : timeout_(sg_agent::DEFAULT_SERVICE_TIMEOUT),
      zk_get_cb_(boost::bind(&ZkClient::HandleZkGetReq, this, _1)),
      zk_wget_cb_(boost::bind(&ZkClient::HandleZkWGetReq, this, _1)),
      zk_wget_children_cb_(boost::bind(&ZkClient::HandleZkWGetChildrenReq, this, _1)),
      zk_create_cb_(boost::bind(&ZkClient::HandleZkCreateReq, this, _1)),
      zk_set_cb_(boost::bind(&ZkClient::HandleZkSetReq, this, _1)),
      zk_exists_cb_(boost::bind(&ZkClient::HandleZkExistsReq, this, _1)),
      m_server(""),
      m_timeout(sg_agent::ZK_CLIENT_TIMEOUT),
      rand_try_times(0),
      m_retry(sg_agent::ZK_RETRY),
      m_zh(NULL) {
}

ZkClient::~ZkClient() {
  zk_close();
}

void ZkClient::Destroy() {
  SAFE_DELETE(mZkClient);
}

int ZkClient::checkZk() {
  int count = 0;
  int flag = -1;
  int state = zoo_state(m_zh);
  do {
    if (ZOO_CONNECTED_STATE == state) {
      flag = 0;
      break;
    } else if (ZOO_CONNECTING_STATE == state) {
      //如果连接已经创建，正在建立连接时，sleep 50ms,再check
      LOG_WARN("WARN zk connection: ZOO_CONNECTING_STATE!");
      usleep(50000);
    } else {
      LOG_FATAL("ERR zk connection lost! zk state = " << state);
      flag = -1;
    }
    state = zoo_state(m_zh);
    count++;
  } while (state != ZOO_CONNECTED_STATE && count < m_retry);

  return flag;
}

int ZkClient::init(const std::string &server, int timeout, int retry) {
  rand_try_times = 0;
  m_server = server;
  m_timeout = timeout;
  m_retry = retry;

  //set log level.//
  zoo_set_debug_level(ZOO_LOG_LEVEL_WARN);

  zk_loop_ = zk_loop_thread_.startLoop();

  int ret = connectToZk();
  return ret;
}

int ZkClient::connectToZk() {
  //先关闭连接
  zk_close();

  int count = 0;
  //避免跟zookeeper的自定义的状态重合
  int state = 888;
  //默认初始值
  int delay_time = 1;
  do {
    LOG_WARN("start to connect ZK, count : " << count);
    ++count;
    if (m_zh == NULL) {
      int ret = zk_init();
      if (0 != ret || m_zh == NULL || errno == EINVAL) {
        LOG_ERROR("zookeeper_init failed. retrying in 1 second. serverList: " << m_server
                                                                              << ", errno = " << errno
                                                                              << ", timeout = " << m_timeout
                                                                              << ", count = " << count);
      } else {
        LOG_DEBUG("zookeeper_init success. serverList: " << m_server);
      }
    }

    //连接建立后，sleep 1s
    sleep(delay_time);
    delay_time = delay_time * 2;

    //check状态
    state = zoo_state(m_zh);
    if (state == ZOO_CONNECTING_STATE) {
      //sleep 5ms再check 状态
      usleep(5000);
      state = zoo_state(m_zh);
    }
  } while (state != ZOO_CONNECTED_STATE && count < m_retry + 1);

  if (state != ZOO_CONNECTED_STATE) {
    LOG_ERROR("zookeeper_init failed, please check zk_server. state = " << state);
  }

  return state == ZOO_CONNECTED_STATE ? 0 : -2;
}

/*
 * ZK连接watcher, 当连接发送变化时，进行日志报警
 *
 */
void ZkClient::connWatcher(zhandle_t *zh, int type, int state,
                           const char *path, void *watcher_ctx) {
  if (ZOO_CONNECTED_STATE == state) {
    LOG_INFO("connWatcher() ZOO_CONNECTED_STATE = " << state
                                                    << ", type " << type);
  } else if (ZOO_AUTH_FAILED_STATE == state) {
    LOG_ERROR("connWatcher() ZOO_AUTH_FAILED_STATE = " << state
                                                       << ", type " << type);
  } else if (ZOO_EXPIRED_SESSION_STATE == state) {
    LOG_ERROR("connWatcher() ZOO_EXPIRED_SESSION_STATE = " << state
                                                           << ", type " << type);
    //TODO:
    mZkClient->on_reconnect();
  } else if (ZOO_CONNECTING_STATE == state) {
    LOG_ERROR("connWatcher() ZOO_CONNECTING_STATE = " << state
                                                      << ", type " << type);
  } else if (ZOO_ASSOCIATING_STATE == state) {
    LOG_ERROR("connWatcher() ZOO_ASSOCIATING_STATE = " << state
                                                       << ", type " << type);
  }
}

int ZkClient::randSleep() {
  srand(time(0));
  int rand_time = 0;
  ++rand_try_times;
  rand_try_times = rand_try_times > 3 ? 3 : rand_try_times;

  //根据次数随机一个时间，防止所有机器同一时间去重连ZK
  switch (rand_try_times) {
    case 1: {
      rand_time = rand() % 10;
      break;
    }
    case 2: {
      rand_time = rand() % 30;
      break;
    }
    default: {
      rand_time = rand() % 50;
    }
  }

  usleep(rand_time * 1000);
  LOG_ERROR("zk connection lost, randtimes " << rand_try_times << ", wait " << rand_time << "ms, start to reconnect!");

  return SUCCESS;
}

/* 
 *  获取ZK handle， 如果连接不可用, 启动重连 
*  */
void ZkClient::on_reconnect() {
  while (checkZk() < 0) {
    randSleep();
    if (connectToZk() < 0) {
      LOG_ERROR("reconnect to zk fail!");
    } else {
      //连接建立后，rand的次数恢复到0
      rand_try_times = 0;
    }
  }
}

/* 
 *  获取ZK单例对象 
 *  */
ZkClient *ZkClient::getInstance() {
  if (NULL == mZkClient) {
    mZkClient = new ZkClient();
  }
  return mZkClient;
}

int ZkClient::zk_get(const char *path, int watch, std::string *buffer, int *buffer_len, struct Stat *stat) {
  if (checkZk() < 0) {
    return ERR_ZK_CONNECTION_LOSS;
  }
  int retry = 0;
  int ret = 0;
  boost::shared_array<char> buff_ptr(new char[*buffer_len]);
  while (kZkGetRetry > retry++) {
    ret = zoo_get(m_zh, path, watch, buff_ptr.get(), buffer_len, stat);
    if (ZOK != ret) {
      LOG_ERROR("failed to zoo get, path: " << path
                                            << ", buffer_len: " << *buffer_len
                                            << ", ret = " << ret);
      return ret;
    }
    if (*buffer_len != stat->dataLength && stat->dataLength != 0) {
      LOG_INFO("new larger buffer's size to get zk_get node")
      buff_ptr = boost::shared_array<char>(new char[stat->dataLength]);
      *buffer_len = stat->dataLength;
      continue;
    }
    break;
  }
  buffer->assign(buff_ptr.get(), *buffer_len);
  return ret;
}

int ZkClient::zk_wget_children(const char *path, watcher_fn watch, void *watcherCtx, struct String_vector *strings) {
  if (checkZk() < 0) {
    return ERR_ZK_CONNECTION_LOSS;
  }

  int ret = zoo_wget_children(m_zh, path, watch, watcherCtx, strings);

  return ret;
}

int ZkClient::zk_wget(const char *path, watcher_fn watch, void *watcherCtx,
                      std::string *buffer, int *buffer_len, struct Stat *stat) {
  if (checkZk() < 0) {
    return ERR_ZK_CONNECTION_LOSS;
  }

  int retry = 0;
  int ret = 0;
  boost::shared_array<char> buff_ptr(new char[*buffer_len]);
  while (kZkGetRetry > retry++) {
    ret = zoo_wget(m_zh, path, watch, watcherCtx, buff_ptr.get(), buffer_len, stat);
    if (ZOK != ret) {
      return ret;
    }

    if (*buffer_len != stat->dataLength && stat->dataLength != 0) {
      LOG_INFO("buffer's size is not enough to put zk_wget node")
      buff_ptr = boost::shared_array<char>(new char[stat->dataLength]);
      *buffer_len = stat->dataLength;
      continue;
    }
    break;
  }
  buffer->assign(buff_ptr.get(), *buffer_len);

  return ret;
}

int ZkClient::ZkGet(sg_agent::ZkGetInvokeParams *req) {
  int ret = 0;

  ZkGetContextPtr context(
      new TaskContext<ZkGetRequest, ZkGetResponse>(req->zk_get_request));

  size_t pending_tasks_size = zk_loop_->queueSize();

  if (pending_tasks_size < kMaxPendingTasks) {
    zk_loop_->runInLoop(boost::bind(zk_get_cb_, context));
  } else {
    LOG_ERROR("zk backend thread overload, task queue size: "
                  << pending_tasks_size);
    return ERR_FAILEDSENDMSG;
  }

  //wait for return
  context->WaitResult(timeout_);
  if (NULL == context->get_response()) {
    LOG_WARN("(ZkGet) don't get response in time=" << timeout_ << "us, path = " << req->zk_get_request.path);
    ret = ERR_ZK_EVENTLOOP_TIMEOUT;
  } else {
    req->zk_get_response = *(context->get_response());
    // use zk errorcode as the return.
    ret = req->zk_get_response.err_code;
  }

  return ret;
}

int ZkClient::ZkWGet(sg_agent::ZkWGetInvokeParams *req) {
  int ret = 0;

  ZkWGetContextPtr context(
      new TaskContext<ZkWGetRequest, ZkWGetResponse>(req->zk_wget_request));

  size_t pending_tasks_size = zk_loop_->queueSize();

  if (pending_tasks_size < kMaxPendingTasks) {
    zk_loop_->runInLoop(boost::bind(zk_wget_cb_, context));
  } else {
    LOG_ERROR("zk backend thread overload, task queue size: "
                  << pending_tasks_size);
    return ERR_FAILEDSENDMSG;
  }

  //wait for return
  context->WaitResult(timeout_);
  if (NULL == context->get_response()) {
    LOG_WARN("(ZkWGet) don't get response in time =" << timeout_ << "us, path = " << req->zk_wget_request.path);
    ret = ERR_ZK_EVENTLOOP_TIMEOUT;
  } else {
    req->zk_wget_response = *(context->get_response());
    ret = req->zk_wget_response.err_code;
  }

  return ret;
}

int ZkClient::ZkWGetChildren(sg_agent::ZkWGetChildrenInvokeParams *req) {
  int ret = 0;
  req->zk_wgetchildren_response.count = 0;

  ZkWGetChildrenContextPtr context(
      new TaskContext<ZkWGetChildrenRequest, ZkWGetChildrenResponse>(
          req->zk_wgetchildren_request));

  size_t pending_tasks_size = zk_loop_->queueSize();

  if (pending_tasks_size < kMaxPendingTasks) {
    zk_loop_->runInLoop(boost::bind(zk_wget_children_cb_, context));
  } else {
    LOG_ERROR("zk backend thread overload, task queue size: "
                  << pending_tasks_size);
    return ERR_FAILEDSENDMSG;
  }

  //wait for return
  context->WaitResult(timeout_);
  if (NULL == context->get_response()) {
    LOG_WARN("(ZkWGetChildren) don't get response in time=" << timeout_ << "us, path = "
                                                            << req->zk_wgetchildren_request.path);
    ret = ERR_ZK_EVENTLOOP_TIMEOUT;
  } else {
    req->zk_wgetchildren_response = *(context->get_response());
    ret = req->zk_wgetchildren_response.err_code;
  }

  return ret;
}

int ZkClient::ZkCreate(sg_agent::ZkCreateInvokeParams *req) {
  int ret = 0;

  ZkCreateContextPtr context(
      new TaskContext<ZkCreateRequest, int>(req->zk_create_request));

  size_t pending_tasks_size = zk_loop_->queueSize();

  if (pending_tasks_size < kMaxPendingTasks) {
    zk_loop_->runInLoop(boost::bind(zk_create_cb_, context));
  } else {
    LOG_ERROR("zk backend thread overload, task queue size: "
                  << pending_tasks_size);
    return ERR_FAILEDSENDMSG;
  }

  ret = ERR_ZK_EVENTLOOP_TIMEOUT;
  //wait for return
  context->WaitResult(timeout_);
  if (NULL == context->get_response()) {
    LOG_WARN("(ZkCreate) don't get response in time=" << timeout_
                                                      << "us, path = " << req->zk_create_request.path);
    ret = ERR_ZK_EVENTLOOP_TIMEOUT;
  } else if (ZOK != *(context->get_response())) {
    LOG_WARN("failed to create zk node, "
                 << "path = "
                 << req->zk_create_request.path
                 << ", ret = "
                 << context->get_response());
    ret = *context->get_response();
  } else {
    LOG_INFO("succeed to get node exists info, "
                 << "path = " << req->zk_create_request.path);
    ret = 0;
  }

  return ret;
}

int ZkClient::ZkSet(sg_agent::ZkSetRequest *req) {
  int ret = 0;

  ZkSetContextPtr context(
      new TaskContext<ZkSetRequest, int>(*req));

  size_t pending_tasks_size = zk_loop_->queueSize();

  if (pending_tasks_size < kMaxPendingTasks) {
    zk_loop_->runInLoop(boost::bind(zk_set_cb_, context));
  } else {
    LOG_ERROR("zk backend thread overload, task queue size: " << pending_tasks_size);
    return ERR_FAILEDSENDMSG;
  }

  ret = ERR_ZK_EVENTLOOP_TIMEOUT;
  //wait for return
  context->WaitResult(timeout_);
  if (NULL == context->get_response()) {
    LOG_WARN("(ZkSet) don't get response in time=" << timeout_ << "us, path = " << req->path);
    ret = ERR_ZK_EVENTLOOP_TIMEOUT;
  } else if (ZOK != *(context->get_response())) {
    LOG_WARN("failed to set zk node, "
                 << "path = "
                 << req->path
                 << ", ret = "
                 << context->get_response());
    ret = *context->get_response();
  } else {
    LOG_INFO("succeed to set zk note, path = " << req->path);
    ret = 0;
  }

  return ret;
}

int ZkClient::ZkExists(sg_agent::ZkExistsRequest *req) {
  int ret = 0;

  ZkExistsContextPtr context(
      new TaskContext<ZkExistsRequest, int>(*req));

  size_t pending_tasks_size = zk_loop_->queueSize();

  if (pending_tasks_size < kMaxPendingTasks) {
    zk_loop_->runInLoop(boost::bind(zk_exists_cb_, context));
  } else {
    LOG_ERROR("zk backend thread overload, task queue size: "
                  << pending_tasks_size);
    return ERR_FAILEDSENDMSG;
  }

  ret = ERR_ZK_EVENTLOOP_TIMEOUT;
  //wait for return
  context->WaitResult(timeout_);
  if (NULL == context->get_response()) {
    LOG_WARN("(ZkExists) don't get response in time=" << timeout_ << "us, path = " << req->path);
    ret = ERR_ZK_EVENTLOOP_TIMEOUT;
  } else if (ZOK != *(context->get_response())) {
    LOG_WARN("failed to check zk node, "
                 << "path = "
                 << req->path
                 << ", ret = "
                 << context->get_response());
    ret = *context->get_response();
  } else {
    LOG_INFO("succeed to call zkexist, path = " << req->path);
    ret = 0;
  }

  return ret;
}

int ZkClient::zk_init() {
  m_zh = zookeeper_init(m_server.c_str(), connWatcher, m_timeout, 0, NULL, 0);
  int ret = (NULL == m_zh) ? -1 : 0;
  return ret;
}

int ZkClient::zk_close() {
  int ret = 0;
  if (m_zh) {
    ret = zookeeper_close(m_zh);
    if (0 != ret) {
      LOG_ERROR("close ZK connection failed! ret = " << ret);
    }
    m_zh = NULL;
  }
  return ret;
}

int ZkClient::HandleZkGetReq(ZkGetContextPtr context) {
  sg_agent::ZkGetResponse zk_get_response;
  zk_get_response.buffer_len = kZkContentSize;
  int ret = zk_get(context->get_request()->path.c_str(), 0,
                   &(zk_get_response.buffer),
                   &(zk_get_response.buffer_len),
                   &(zk_get_response.stat));
  // recorde the zk errorcode
  zk_get_response.err_code = ret;
  context->set_response(zk_get_response);
  return ret;
}

int ZkClient::HandleZkWGetReq(ZkWGetContextPtr context) {
  sg_agent::ZkWGetResponse zk_wget_response;
  zk_wget_response.buffer_len = kZkContentSize;
  int ret = zk_wget(context->get_request()->path.c_str(),
                    context->get_request()->watch,
                    context->get_request()->watcherCtx,
                    &(zk_wget_response.buffer),
                    &(zk_wget_response.buffer_len),
                    &(zk_wget_response.stat));

  // recorde the zk errorcode
  zk_wget_response.err_code = ret;
  context->set_response(zk_wget_response);
  return ret;
}

int ZkClient::HandleZkWGetChildrenReq(ZkWGetChildrenContextPtr context) {
  sg_agent::ZkWGetChildrenResponse zk_wgetchildren_response;
  struct String_vector stat;
  stat.count = 0;
  stat.data = 0;
  int ret = zk_wget_children(context->get_request()->path.c_str(),
                             context->get_request()->watch,
                             context->get_request()->watcherCtx,
                             &stat);
  // record the zk errorcode
  zk_wgetchildren_response.err_code = ret;
  if(ZOK == ret){
    zk_wgetchildren_response.count = stat.count;
    for (int i = 0; i < stat.count; i++) {
      std::string data = stat.data[i];
      zk_wgetchildren_response.data.push_back(data);
    }
  }
  context->set_response(zk_wgetchildren_response);
  deallocate_String_vector(&stat);
  return ret;
}

int ZkClient::HandleZkCreateReq(ZkCreateContextPtr context) {
  int ret = 0;
  if (checkZk() < 0) {
    ret = ERR_ZK_CONNECTION_LOSS;
    context->set_response(ret);
    return ret;
  }

  char path_buffer[MAX_BUF_SIZE] = {0};
  int path_buffer_len = MAX_BUF_SIZE;

  ret = zoo_create(m_zh,
                   context->get_request()->path.c_str(),
                   context->get_request()->value.c_str(),
                   context->get_request()->value_len,
                   &ZOO_OPEN_ACL_UNSAFE /* use ACL of parent */,
                   0 /* persistent node*/,
                   path_buffer,
                   path_buffer_len);

  LOG_INFO("zoo_create ret = "
               << ret
               << ", path = "
               << context->get_request()->path);
  context->set_response(ret);
  return ret;
}

int ZkClient::HandleZkSetReq(ZkSetContextPtr context) {
  int ret = 0;
  if (checkZk() < 0) {
    ret = ERR_ZK_CONNECTION_LOSS;
    context->set_response(ret);
    return ret;
  }

  ret = zoo_set(m_zh,
                context->get_request()->path.c_str(),
                context->get_request()->buffer.c_str(),
                context->get_request()->buffer.size(),
                context->get_request()->version);
  context->set_response(ret);
  LOG_INFO("zoo_set ret = "
               << ret
               << ", path = "
               << context->get_request()->path);
  return ret;
}

int ZkClient::HandleZkExistsReq(ZkExistsContextPtr context) {
  int ret = 0;
  if (checkZk() < 0) {
    ret = ERR_ZK_CONNECTION_LOSS;
    context->set_response(ret);
    return ret;
  }

  struct Stat stat;
  ret = zoo_exists(m_zh,
                   context->get_request()->path.c_str(),
                   context->get_request()->watch,
                   &stat);
  LOG_INFO("zoo_exists ret = "
               << ret
               << ", path = "
               << context->get_request()->path);
  context->set_response(ret);
  return ret;
}
