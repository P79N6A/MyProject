#include "config_util.h"
#include "../plugindef.h"
#include <glog/logging.h>

using namespace std;
using namespace tinyxml2;

namespace cplugin {

bool ParsePreload(XMLElement* root, vector<PreloadInfo>* preload_infos) {
  XMLElement* module = root->FirstChildElement("Module");
  if (!module) 
    return true;

  do {
    const char* attr_name = module->Attribute("Name");
    const char* attr_lib_name = module->Attribute("LibraryName");
    const char* attr_hash = module->Attribute("Hash");
    if (!attr_name || !attr_lib_name || !attr_hash) {
      LOG(ERROR) << "Parse config failed.";
      return false;
    }
    PreloadInfo info;
    info.name = attr_name;
    info.library_name = attr_lib_name; 
    info.hash = attr_hash;

    preload_infos->push_back(info);
    module = module->NextSiblingElement();
  } while (module);

  return true;
}

bool ParseHost(XMLElement* root, HostInfo* host_info) {
  XMLElement* module = root->FirstChildElement("Module");
  if (!module) 
    return true;

  const char* attr_name = module->Attribute("Name");
  const char* attr_lib_name = module->Attribute("LibraryName");
  const char* attr_hash = module->Attribute("Hash");
  if (!attr_name || !attr_lib_name || !attr_hash) {
    LOG(ERROR) << "Parse config failed.";
    return false;
  }

  HostInfo info;
  info.name = attr_name;
  info.library_name = attr_lib_name; 
  info.hash = attr_hash;
  
  *host_info = info;
  module = module->NextSiblingElement();

  return true;
}

bool ParsePlugin(XMLElement* root, vector<PluginInfo>* plugin_infos) {
  XMLElement* module = root->FirstChildElement("Module");
  if (!module) 
    return true;

  do {
    const char* attr_name = module->Attribute("Name");
    const char* attr_lib_name = module->Attribute("LibraryName");
    const char* attr_hash = module->Attribute("Hash");
    const char* host_update = module->Attribute("HotUpdate");
    if (!attr_name || !attr_lib_name || !attr_hash ) {
      LOG(ERROR) << "Parse config failed.";
      return false;
    }
    PluginInfo info;
    info.name = attr_name;
    info.library_name = attr_lib_name; 
    info.hash = attr_hash;

    if(!host_update){
      info.hot_update = UPDATE_TYPE_ALL;
    }else{
      if(strcmp(host_update,"1")){
        info.hot_update = UPDATE_TYPE_PLUGIN;
      }else{
        info.hot_update = UPDATE_TYPE_ALL;
      }
    }

    plugin_infos->push_back(info);
    module = module->NextSiblingElement();
  } while (module);

  return true;
}

bool ParseConfig(tinyxml2::XMLElement* root,
                 std::vector<ConfigInfo>* config_infos) {
  XMLElement* file = root->FirstChildElement("File");
  if (!file) 
    return true;

  do {
    const char* attr_name = file->Attribute("ConfigName");
    const char* attr_link_name = file->Attribute("LinkName");
    const char* attr_hash = file->Attribute("Hash");
    const char* host_update = file->Attribute("HotUpdate");
    if (!attr_name || !attr_link_name || !attr_hash ) {
      LOG(ERROR) << "Parse config failed.";
      return false;
    }

    ConfigInfo info;
    if(!host_update){
      info.hot_update = UPDATE_TYPE_ALL;
    }else{
      if(strcmp(host_update,"1")){
        info.hot_update = UPDATE_TYPE_CONFIG;
      }else{
        info.hot_update = UPDATE_TYPE_ALL;
      }
    }

    info.config_name = attr_name;
    info.link_name = attr_link_name;
    info.hash      =  attr_hash;


    config_infos->push_back(info);
    file = file->NextSiblingElement();
  } while (file);

  return true;

}

} // namespace cplugin
  
