#ifndef HOST_PROCESS_H_
#define HOST_PROCESS_H_

#include <map>
#include <vector>
#include <string>
#include <boost/shared_ptr.hpp>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>

#include "define/port.h"
#include "util/tinyxml2.h"
#include "util/downloader.h"
#include "plugindef.h"
#include "Core.h"
#include "util/log4cplus.h"

namespace cplugin {
enum Command {
  START = 1,
  STOP,
  UPGRADE,
  DOWNLOAD,
  MONITOR,
  GET_RUNNING_INFO
};

class HostProcess {
 public:
  explicit HostProcess(int fd, const std::string& name, const std::string& version);
  ~HostProcess();
  void Run(int argc, char** argv);
  void SetDownloader(std::string host, std::string port);
 private:
  int32_t Start();
  int32_t Stop();
  int32_t Upgrade();
  TInfos GetRunningInfo();
  std::string Monitor();
  bool LoadConfig();
  bool LoadPreloadModule();
  bool LoadHost();


  void SavePid();

  /*bool ParsePreload(tinyxml2::XMLElement* root, 
                    std::vector<PreloadInfo>* preload_infos);
  bool ParseHost(tinyxml2::XMLElement* root, HostInfo* host_info);
  bool ParsePlugin(tinyxml2::XMLElement* root, 
                   std::vector<PluginInfo>* plugin_infos);*/

  int pop_fd_;
  std::vector<PreloadInfo> preload_infos_;
  std::vector<PluginInfo> plugin_infos_;
  std::vector<ConfigInfo> config_infos_;
  HostInfo host_info_;
  HANDLE host_handle_;
  std::string name_;
  std::string version_;
  std::string config_file_;
  std::string pid_file_;

  Downloader *downloader_;
};

} // namespace cplugin 

#endif // HOST_PROCESS_H_
