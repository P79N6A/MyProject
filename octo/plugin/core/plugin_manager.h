#ifndef PLUGIN_MANAGER_H_
#define PLUGIN_MANAGER_H_

#include <map>
#include <string>
#include <vector>
#include <pthread.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>

#include "hostimpl.h"

namespace cplugin {
class PluginManager {
 public:
  PluginManager();
  ~PluginManager();

  static PluginManager* Instance();
  static bool RegisterFunc(const std::string& type, const RegisterParams& params); 

  void Init();
  void Destory();

  bool Register(const std::string& type, const RegisterParams& params); 
  bool LoadPlugins(const std::map<std::string, HANDLE>& preload_map,
                   const std::vector<PluginInfo>& plugin_infos,
                   const HostServices& host_services);
  bool UpgradePlugins(const std::map<std::string, HANDLE>& preload_map, 
                      const std::vector<PluginInfo>& plugin_infos,
                      const HostServices& host_services,
                      std::vector<std::string>* updated_plugins);
  bool FindClassCreator(const std::string& type, RegisterParams* params);

  void UnloadPlugin(const std::string& plugin);
  void UnloadPlugins();

  int GetPluginInfos(std::vector<PluginInfo>* plugins) {
    *plugins = plugin_infos_;
    return 0;
  }
  
 private:
  void GCTimerHandler();

  static PluginManager* instance_;

  pthread_mutex_t mutex_lock_;
  std::map<std::string, RegisterParams> clsmap_;
  std::map<std::string, HANDLE> plugin_handle_map_;
  std::vector<PluginInfo> plugin_infos_;
  std::vector<std::string> trash_;

  muduo::net::EventLoopThread timer_thread_;
  muduo::net::EventLoop* timer_loop_; 
  muduo::net::TimerCallback garbage_collector_cb_;
};

} // namespace core

#endif // PLUGIN_MANAGER_H_
