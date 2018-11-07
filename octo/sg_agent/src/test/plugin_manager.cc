#include <core/plugin_manager.h>

#include <dlfcn.h>
#include <iostream>
#include <boost/bind.hpp>

#include <core/define/auto_lock.h>

using namespace std;

int InitializePlugin(const std::map<std::string, HANDLE>& preload_map, 
										const HostServices& host_services,
										PluginInfo info);
namespace cplugin {

static const double kGCInterval = 30 * 60.0; // 30mins

PluginManager* PluginManager::instance_ = NULL;

PluginManager::PluginManager() 
  : garbage_collector_cb_(boost::bind(&PluginManager::GCTimerHandler, this)) {
  pthread_mutex_init(&mutex_lock_, NULL); 
}

PluginManager::~PluginManager() {
  pthread_mutex_destroy(&mutex_lock_);
}

PluginManager* PluginManager::Instance() {
  if (!instance_) {
    instance_ = new PluginManager();
  }

  return instance_;
}

void PluginManager::Init() {
  timer_loop_ = timer_thread_.startLoop();
}

void PluginManager::Destory() {
  if (instance_) {
    delete instance_;
    instance_ = NULL;
  }
}

bool PluginManager::Register(const string& type, const RegisterParams& params) {
  {
  AutoLock auto_lock(&mutex_lock_);
  clsmap_[type] = params;
  }
  return true;
}

bool PluginManager::RegisterFunc(const string& type, const RegisterParams& params) {
  return Instance()->Register(type, params);
}

bool PluginManager::LoadPlugins(const map<string, HANDLE>& preload_map, 
                                const vector<PluginInfo>& plugin_infos, 
                                const HostServices& host_services) {
  vector<PluginInfo>::const_iterator it;
  for (it = plugin_infos.begin(); it != plugin_infos.end(); ++it) {
    HANDLE handle = 0;
    const string& lib_name = (*it).library_name;
		/*
    handle = dlopen(lib_name.c_str(), RTLD_NOW);
    if (!handle) {
      cout << "error: " << dlerror() << endl;
      //LOG(ERROR) << "open library " << lib_name << " error: " << dlerror();
      return false;
    }

    typedef int (*CF)(const map<string, HANDLE>& preload_map,
                      const HostServices& host_services,
                      PluginInfo info);
    CF cf = reinterpret_cast<CF>(dlsym(handle, "InitPlugin"));
    if (!cf) {
      //LOG(ERROR) << "Plugin " << lib_name 
      //    << " haven't implement InitPlugin method.";
      return false;
    }
   */ 
    if (0 != InitializePlugin(preload_map, host_services, *it)) {
      //LOG(ERROR) << "Plugin " << lib_name << " init failed.";
      return false;
    }
    {
    AutoLock auto_lock(&mutex_lock_);
    plugin_handle_map_[lib_name] = handle;
    }
  }

  {
  AutoLock auto_lock(&mutex_lock_);
  plugin_infos_ = plugin_infos;
  }

  return true;
}

bool PluginManager::UpgradePlugins(const map<string, HANDLE>& preload_map, 
                                   const vector<PluginInfo>& plugin_infos,
                                   const HostServices& host_services,
                                   vector<string>* updated_plugins) {
  vector<PluginInfo> origin_plugin_infos; 
  {
  AutoLock auto_lock(&mutex_lock_);
  origin_plugin_infos.assign(plugin_infos_.begin(), plugin_infos_.end()); 
  }

  bool is_updated = false;
  vector<PluginInfo>::const_iterator it;
  for (it = plugin_infos.begin(); it != plugin_infos.end(); ++it) {
    const string& name = (*it).name;
    const string& lib_name = (*it).library_name;

    vector<PluginInfo>::iterator tmp_it;
    for (tmp_it = origin_plugin_infos.begin(); tmp_it != origin_plugin_infos.end(); ++tmp_it) {
      if ((*tmp_it).name.compare(name) == 0) {
        if ((*tmp_it).library_name.compare(lib_name) != 0) {
          cout << "open: " << lib_name << endl;
          HANDLE handle = dlopen(lib_name.c_str(), RTLD_NOW);
          if (!handle) {
            //LOG(ERROR) << "open library " << lib_name << " error: " << dlerror();
            return false;
          }

          typedef int (*CF)(const map<string, HANDLE>& preload_map,
                            const HostServices& host_services,
                            PluginInfo info);
          CF cf = reinterpret_cast<CF>(dlsym(handle, "InitPlugin"));
          if (!cf) {
            //LOG(ERROR) << "Plugin " << lib_name 
            //    << " haven't implement InitPlugin method.";
            return false;
          }
          
          if (0 != cf(preload_map, host_services, *it)) {
            //LOG(ERROR) << "Plugin " << lib_name << " init failed.";
            return false;
          }
          
          is_updated = true; 
          updated_plugins->push_back(name);

          {
          AutoLock auto_lock(&mutex_lock_);
          plugin_handle_map_[lib_name] = handle;
          plugin_infos_.push_back((*it));
          trash_.push_back((*tmp_it).library_name); 
          }
        }
      }
    }
  }
  
  //if (is_updated) {
  //  timer_loop_->runAfter(kGCInterval, garbage_collector_cb_);
  //}

  return is_updated;
}

bool PluginManager::FindClassCreator(const string& type, RegisterParams* params) {
  {
  AutoLock auto_lock(&mutex_lock_);

  if (clsmap_.find(type) != clsmap_.end()) {
    RegisterParams& rp = clsmap_[type];
    params->create_func = rp.create_func;
    params->destroy_func = rp.destroy_func;
    params->library_name = rp.library_name;
    return true;
  }
  }

  return false;
}

void PluginManager::UnloadPlugin(const string& plugin) {
  vector<string>::const_iterator it;
  for (it = trash_.begin(); it != trash_.end(); ++it) {
    const string& trash_lib_name = *it;

    if (trash_lib_name != plugin) {
      continue;
    }

    HANDLE handle = plugin_handle_map_[trash_lib_name];

    typedef int (*CF)();
    CF cf = reinterpret_cast<CF>(dlsym(handle, "FreePlugin"));
    if (!cf) {
      //LOG(ERROR) << "Plugin " << lib_name 
      //    << " haven't implement InitPlugin method.";
    }
    
    if (0 != cf()) {
      //LOG(ERROR) << "Plugin " << lib_name << " init failed.";
    }

    if (dlclose(handle) < 0) {
      //LOG(ERROR) << "dlclose " << trash_lib_name << " error: " << dlerror();
    }
   
    // Remove handle map  
    map<string, HANDLE>::iterator map_it;
    if ((map_it = plugin_handle_map_.find(trash_lib_name)) != plugin_handle_map_.end()) {
      plugin_handle_map_.erase(map_it);
    }
    
    // Remove plugin info
    vector<PluginInfo>::iterator info_it;
    for (info_it = plugin_infos_.begin(); info_it != plugin_infos_.end(); ++info_it) {
      const string& lib_name = (*info_it).library_name;
      if (lib_name.compare(trash_lib_name) == 0) {
        plugin_infos_.erase(info_it);
        break;
      }
    }
  }
}

void PluginManager::UnloadPlugins() {
  GCTimerHandler();
}

void PluginManager::GCTimerHandler() {
  vector<string>::const_iterator it;
  for (it = trash_.begin(); it != trash_.end(); ++it) {
    const string& trash_lib_name = *it;
    HANDLE handle = plugin_handle_map_[trash_lib_name];

    typedef int (*CF)();
    CF cf = reinterpret_cast<CF>(dlsym(handle, "FreePlugin"));
    if (!cf) {
      //LOG(ERROR) << "Plugin " << lib_name 
      //    << " haven't implement InitPlugin method.";
    }
    
    if (0 != cf()) {
      //LOG(ERROR) << "Plugin " << lib_name << " init failed.";
    }

    if (dlclose(handle) < 0) {
      //LOG(ERROR) << "dlclose " << trash_lib_name << " error: " << dlerror();
    }
   
    // Remove handle map  
    map<string, HANDLE>::iterator map_it;
    if ((map_it = plugin_handle_map_.find(trash_lib_name)) != plugin_handle_map_.end()) {
      plugin_handle_map_.erase(map_it);
    }
    
    // Remove plugin info
    vector<PluginInfo>::iterator info_it;
    for (info_it = plugin_infos_.begin(); info_it != plugin_infos_.end(); ++info_it) {
      const string& lib_name = (*info_it).library_name;
      if (lib_name.compare(trash_lib_name) == 0) {
        plugin_infos_.erase(info_it);
        break;
      }
    }

  }
}

} // namespace core
