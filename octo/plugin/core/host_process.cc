#include "host_process.h"

#include <iostream>
#include <fstream>
#include <dlfcn.h>
#include <sys/prctl.h>
#include <boost/make_shared.hpp>
#include <glog/logging.h>
#include <rapidjson/document.h>
#include <rapidjson/writer.h>
#include <rapidjson/stringbuffer.h>
#include <transport/TSocket.h>    
#include <transport/TBufferTransports.h>    
#include <protocol/TBinaryProtocol.h>

#include "util/net_util.h"
#include "util/config_util.h"
#include "util/process.h"

using namespace std;
using namespace tinyxml2;
using namespace muduo;
using namespace rapidjson;
using namespace ::apache::thrift;    
using namespace ::apache::thrift::protocol;    
using namespace ::apache::thrift::transport;    

namespace cplugin {

HostProcess::HostProcess(int fd, const std::string& name, const std::string& version)
    : pop_fd_(fd), 
      downloader_(NULL),
      name_(name),
      version_(version)

{
  config_file_.assign(name_);
  config_file_.append("_");
  config_file_.append("config.xml");
  pid_file_.assign(name_);
  pid_file_.append(".pid");
}

HostProcess::~HostProcess() {
  if (downloader_) {
    delete downloader_;
    downloader_ = NULL;
  }
}

void HostProcess::SavePid()
{
   SavePid_X(pid_file_.c_str());
}

void HostProcess::Run(int argc, char** argv) {
  InitProcTitle(argc, argv);
  SetProcTitle(name_.c_str());
  SavePid();

  while (true) {
    int command = -1;
    int n = ReadNBytes(pop_fd_, &command, sizeof(command));
    if (n <= 0) {
      LOG_X_ERROR( "read error.");
      return;
    }

    LOG_X_INFO("Pid: " << getpid() << ", command: " << command) ;
    int32_t result = -1;
    // Handle command
    switch (command) {
      case START:
        result = Start();
        n = WriteNBytes(pop_fd_, &result, sizeof(result));
        break;
    case STOP:
        result = Stop();
        n = WriteNBytes(pop_fd_, &result, sizeof(result));
        usleep(1000*10);
        _exit(0);
        break;
      case UPGRADE:
        result = Upgrade();
        n = WriteNBytes(pop_fd_, &result, sizeof(result));
        break;
      case MONITOR:
      {
        std::string ret  = Monitor();
        n = WriteNBytes(pop_fd_, ret.c_str(), ret.length());
        break;
      }
      case GET_RUNNING_INFO:
        {
        TInfos infos = GetRunningInfo();
        uint8_t* buf_ptr;  
        uint32_t sz;  
        boost::shared_ptr<TMemoryBuffer> mem_buf(new TMemoryBuffer);  
        boost::shared_ptr<TBinaryProtocol> bin_proto(new TBinaryProtocol(mem_buf));  
        infos.write(bin_proto.get());  
        mem_buf->getBuffer(&buf_ptr, &sz);
        n = WriteNBytes(pop_fd_, buf_ptr, sz);
        break;
        }
      default:
        result = -1;
    }

    if (n < 0) {
      LOG_X_ERROR( "write error.");
      return;
    }
  }
}

void HostProcess::SetDownloader(std::string host, std::string port) {
  downloader_ = new Downloader();
  downloader_->Init(host, port);
}

TInfos HostProcess::GetRunningInfo() {
  TInfos tinfos;
  typedef int (*CF)(vector<PluginInfo>* plugins);
  CF cf = reinterpret_cast<CF>(dlsym(host_handle_, 
                                     "GetPluginInfos"));
  if (!cf) {
    LOG_X_ERROR( "Plugin " << host_info_.library_name
                           << " haven't implement UpgradePlugins method." );
  }

  vector<PluginInfo> plugins;

  if (0 != cf(&plugins)) {
    LOG_X_ERROR("GetRunningInfo failed.");
  }
  
  vector<PluginInfo>::iterator it;
  for (it = plugins.begin(); it != plugins.end(); ++it) {
    const PluginInfo& pi = *it;
    TPluginInfo tpi;
    tpi.__set_name(pi.name) ;
    tpi.__set_library_name(pi.library_name);
    tpi.__set_hash(pi.hash) ;
    tpi.__set_version(version_);
    tinfos.plugin_infos.push_back(tpi);
  }

  tinfos.host_info.__set_name(host_info_.name);
  tinfos.host_info.__set_version(version_) ;
  tinfos.host_info.__set_library_name(host_info_.library_name) ;
  tinfos.host_info.__set_hash(host_info_.hash) ;

  return tinfos;
}

int32_t HostProcess::Start() {
  // Load config
  if (!LoadConfig()) {
    LOG_X_ERROR("Unable to load config.");
    return -1;
  }

  // Load preload modules
  if (!LoadPreloadModule()) {
    LOG_X_ERROR("Unable to preload modules.");
    return -1;
  }

  // Load plugin base on the plugin tree
  if (!LoadHost()) {
    LOG_X_ERROR("Unable to load host.");
    return -1; 
  }

  return 0;
}

int32_t HostProcess::Stop() {

    LOG_X_INFO ( "HostProcess::Stop begin  " << name_ << "  " << version_);

    if (!host_handle_) {
        LOG_X_ERROR ( "host_handle_  empty" );
        return -1;
    }

    typedef bool (*CF)();
    CF cf = reinterpret_cast<CF>(dlsym(host_handle_, "UnInitHost"));
    if (!cf) {
        LOG_X_ERROR( "Host  haven't implement reset method.");
        return -1;
    }


    if (0 != cf()) {
        LOG_X_ERROR( "Error happens when UnInitHost.");
        return -1;
    }

    LOG_X_INFO ( "HostProcess::Stop end");
    return 0;
}

int32_t HostProcess::Upgrade() {
  LOG_X_INFO("HostProcess::Upgrade.");
  XMLDocument doc;
  XMLError ret = doc.LoadFile(config_file_.c_str());
  if (XML_SUCCESS != ret) {
    LOG_X_ERROR( "Load xml error: " << ret);
    return -1;
  }

  XMLElement* root = doc.RootElement();
  if (!root) {
    LOG_X_ERROR("Parse config.xml error.");
    return -1;
  }

  XMLElement* plugin_root = root->FirstChildElement("Plugin");
  vector<PluginInfo> plugin_infos;
  if (plugin_root && !ParsePlugin(plugin_root, &plugin_infos)) {
    return -1;
  }

  typedef bool (*CF)(vector<PluginInfo>);
  CF cf = reinterpret_cast<CF>(dlsym(host_handle_, 
                                     "UpgradePlugins"));
  if (!cf) {
    LOG_X_ERROR( "Plugin " << host_info_.library_name
        << " haven't implement UpgradePlugins method.");
    return -1;
  }

  if (0 != cf(plugin_infos)) {
    LOG_X_ERROR( "Upgrade failed.");
    return -1;
  }

  return 0;  
}

string HostProcess::Monitor(){
  LOG_X_INFO ( "HostProcess::Monitor begin  " << name_ << "  " << version_);

  if (!host_handle_) {
    LOG_X_ERROR ( "host_handle_  empty" );
    return "error";
  }

  typedef int (*CF)(std::string&);
  CF cf = reinterpret_cast<CF>(dlsym(host_handle_, "Monitor"));
  if (!cf) {
    LOG_X_ERROR( "Host  haven't implement Monitor method.");
    return "error";
  }

  std::string info;

  if (0 != cf(info)) {
    LOG_X_ERROR( "Error happens when Moniter.");
    return "error";
  }

  LOG_X_INFO ( "HostProcess::Monitor end");
  return info;
}

bool HostProcess::LoadConfig() {
  XMLDocument doc;
  LOG_X_INFO (  "Load xml : " << config_file_);
  XMLError ret = doc.LoadFile(config_file_.c_str());
  if (XML_SUCCESS != ret) {
    LOG_X_INFO (  "Load xml error: " << ret);
    return false;
  }

  XMLElement* root = doc.RootElement();
  if (!root) {
    return false;
  }

  XMLElement* preload_host = root->FirstChildElement("Preload");
  if (preload_host && !ParsePreload(preload_host, &preload_infos_)) {
    return false;
  }

  XMLElement* host_root = root->FirstChildElement("Host");
  if (host_root && !ParseHost(host_root, &host_info_)) {
    return false;
  }

  XMLElement* plugin_root = root->FirstChildElement("Plugin");
  if (plugin_root && !ParsePlugin(plugin_root, &plugin_infos_)) {
    return false;
  }

  XMLElement* config_root = root->FirstChildElement("Config");
  if (config_root && !ParseConfig(config_root, &config_infos_)) {
    return false;
  }
 
  vector<ConfigInfo>::iterator config_it;
  for (config_it = config_infos_.begin(); config_it != config_infos_.end(); ++config_it) {
    const string& link_name = (*config_it).link_name;
    const string& real_name = (*config_it).config_name;
    if (remove(link_name.c_str()) != 0) {
      LOG_X_ERROR (  "Cannot remove file: " << link_name
        << ", errno is " << errno);
    }

    if (symlink(real_name.c_str(), link_name.c_str()) != 0) {
      LOG_X_ERROR ( "Create symbolic link error, errno is " << errno);
      return false;
    }  
  } 

  return true;
}

bool HostProcess::LoadPreloadModule() {
  vector<PreloadInfo>::iterator it;
  HANDLE handle = 0;
  for (it = preload_infos_.begin(); it != preload_infos_.end(); ++it) {
    PreloadInfo& info = *it;
    const string& lib_name = info.library_name;

    handle = dlopen(lib_name.c_str(), RTLD_NOW);
    if (!handle) {
      LOG_X_ERROR( "open library " << lib_name << " error: " << dlerror());
      return false;
    }
    info.handle = handle;
  }
  return true;
}

bool HostProcess::LoadHost() {
  LOG_X_INFO ( "dlopen library " << host_info_.library_name << "begin" );
  host_handle_ = dlopen(host_info_.library_name.c_str(), RTLD_NOW);
  if (!host_handle_) {
    LOG_X_ERROR ( "open library " << host_info_.library_name
        << " error: " << dlerror());
    return false;
  }

  LOG_X_INFO ( "dlopen library " << host_info_.library_name << "end" );

  typedef bool (*CF)(const map<string, HANDLE>& preload_map, 
                     const vector<PluginInfo>& plugins);

  LOG_X_INFO ( "dlsym  InitHost begin" );

  CF cf = reinterpret_cast<CF>(dlsym(host_handle_, "InitHost"));
  if (!cf) {
    LOG_X_ERROR( "Host" << host_info_.library_name
        << " haven't implement InitHost method.");
    return false;
  }

  LOG_X_INFO ( "dlsym  InitHost end" );

  map<string, HANDLE> preload_map;
  vector<PreloadInfo>::iterator it;
  for (it = preload_infos_.begin(); it != preload_infos_.end(); ++it) {
    const PreloadInfo& info = *it;
    preload_map[info.name] = info.handle;
  }

  LOG_X_INFO ( "call  InitHost begin" );

  if (0 != cf(preload_map, plugin_infos_)) {
    LOG_X_ERROR( "Error happens when init host.");
    return false;
  }

  LOG_X_INFO ( "call  InitHost end" );
  return true;
}

} // namespace cplugin 
