#ifndef SGAGENT_MNS_H_
#define SGAGENT_MNS_H_

#include <pthread.h>
#include <core/hostimpl.h>
#include <boost/bind.hpp>
#include <boost/function.hpp>
#include <boost/shared_ptr.hpp>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/net/EventLoopThreadPool.h>

//suggestion by zhangcan
#include "mns/mns_iface.h"

namespace sg_agent {
class MNS {
 public:
  MNS();

  int Init();
  void UpdateSrvListTimer();
  void UpdateRouteTimer();
  void UpdateSrvNameTimer();
  void UpdateAppkeyDescTimer();
  void GCTimer();

  int ResetMnsPlugin();

  IMnsPlugin *GetMnsPlugin() const {
    return protocol_plugin_;
  }

 private:
  muduo::net::EventLoopThread service_loop_thread_;
  muduo::net::EventLoop *service_loop_;

  muduo::net::EventLoopThread route_loop_thread_;
  muduo::net::EventLoop *route_loop_;

  muduo::net::EventLoopThread servicename_loop_thread_;
  muduo::net::EventLoop *servicename_loop_;

  muduo::net::EventLoopThread object_gc_thread_;
  muduo::net::EventLoop *gc_loop_;

  muduo::net::EventLoopThread appkeydesc_loop_thread_;
  muduo::net::EventLoop *appkeydesc_loop_;

  IMnsPlugin *protocol_plugin_;
  std::vector<IMnsPlugin *> trash_protocol_plugins_;
  RegisterParams plugin_register_params_;
  std::vector<RegisterParams> trash_plugin_register_params_;
};

}//namespace sg_agent
#endif

