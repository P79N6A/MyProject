// =====================================================================================
// 
//       Filename:  zk_manager.h
// 
//    Description:  zk连接管理，通过单例实现，负责ZK相关连接各类操作接口封装
// 
// =====================================================================================

#ifndef __zk_client__H__
#define __zk_client__H__

extern "C" {
#include <zookeeper/zookeeper.h>
#include "comm/cJSON.h"
}
#include <pthread.h>
#include <boost/shared_ptr.hpp>
#include <boost/shared_array.hpp>
#include <boost/bind.hpp>
#include <muduo/base/Mutex.h>
#include <muduo/base/ThreadPool.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>

#include "task_context.h"
#include "service_def.h"
using namespace muduo::net;
using namespace sg_agent;

class ZkClient {
 public:
  typedef boost::shared_ptr<TaskContext<ZkGetRequest, ZkGetResponse> > ZkGetContextPtr;
  typedef boost::shared_ptr<TaskContext<ZkWGetRequest, ZkWGetResponse> > ZkWGetContextPtr;
  typedef boost::shared_ptr<TaskContext<ZkWGetChildrenRequest, ZkWGetChildrenResponse> > ZkWGetChildrenContextPtr;
  typedef boost::shared_ptr<TaskContext<std::string, int> > ZkConnContextPtr;
  typedef boost::shared_ptr<TaskContext<ZkCreateRequest, int> > ZkCreateContextPtr;
  typedef boost::shared_ptr<TaskContext<ZkSetRequest, int> > ZkSetContextPtr;
  typedef boost::shared_ptr<TaskContext<ZkExistsRequest, int> > ZkExistsContextPtr;
  typedef boost::function<void(ZkGetContextPtr)> ZkGetCallBack;
  typedef boost::function<void(ZkWGetContextPtr)> ZkWGetCallBack;
  typedef boost::function<void(ZkWGetChildrenContextPtr)> ZkWGetChildrenCallBack;
  typedef boost::function<void(ZkCreateContextPtr)> ZkCreateCallBack;
  typedef boost::function<void(ZkSetContextPtr)> ZkSetCallBack;
  typedef boost::function<void(ZkExistsContextPtr)> ZkExistsCallBack;

  ZkClient();
  ~ZkClient();

  static ZkClient *getInstance();
  static void Destroy();

  int init(const std::string &server, int timeout = 10000, int retry = 3);

  /// connection watcher function
  static void connWatcher(zhandle_t *zh, int type, int state,
                          const char *path, void *watcher_ctx);

  void setTimeout(int timeout) { m_timeout = timeout; }

  void setRetry(int retry) { m_retry = retry; }

  void on_reconnect();

  int connectToZk();

  int checkZk();

  int randSleep();

  //ZK接口封装
  int ZkGet(sg_agent::ZkGetInvokeParams *req);
  int ZkWGet(sg_agent::ZkWGetInvokeParams *req);
  int ZkWGetChildren(sg_agent::ZkWGetChildrenInvokeParams *req);
  int ZkCreate(sg_agent::ZkCreateInvokeParams *req);
  int ZkSet(sg_agent::ZkSetRequest *req);
  int ZkExists(sg_agent::ZkExistsRequest *req);

 private:
  zhandle_t *m_zh;        //zk handler
  std::string m_server;   //server ip list and port
  int m_timeout;          //timeout
  int m_retry;            //retry time

  unsigned int rand_try_times;

  static ZkClient *mZkClient;
  int timeout_;   //eventloop timeout

  int zk_init();
  int zk_close();
  int zk_get(const char *path, int watch, std::string *buffer, int *buffer_len, struct Stat *stat);
  int zk_wget_children(const char *path, watcher_fn watch, void *watcherCtx, struct String_vector *strings);
  int zk_wget(const char *path,
              watcher_fn watch,
              void *watcherCtx,
              std::string *buffer,
              int *buffer_len,
              struct Stat *stat);

  ZkGetCallBack zk_get_cb_;
  ZkWGetCallBack zk_wget_cb_;
  ZkWGetChildrenCallBack zk_wget_children_cb_;
  ZkCreateCallBack zk_create_cb_;
  ZkSetCallBack zk_set_cb_;
  ZkExistsCallBack zk_exists_cb_;

  int HandleZkGetReq(ZkGetContextPtr context);
  int HandleZkWGetReq(ZkWGetContextPtr context);
  int HandleZkWGetChildrenReq(ZkWGetChildrenContextPtr context);
  int HandleZkCreateReq(ZkCreateContextPtr context);
  int HandleZkSetReq(ZkSetContextPtr context);
  int HandleZkExistsReq(ZkExistsContextPtr context);
  muduo::net::EventLoopThread zk_loop_thread_;
  muduo::net::EventLoop *zk_loop_;
};

#endif

