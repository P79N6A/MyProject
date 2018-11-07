#ifndef CONFIG_UTIL_H_
#define CONFIG_UTIL_H_

#include <vector>

#include "tinyxml2.h"
#include "../plugindef.h"

namespace cplugin {

bool ParsePreload(tinyxml2::XMLElement* root, 
                  std::vector<PreloadInfo>* preload_infos);

bool ParseHost(tinyxml2::XMLElement* root, HostInfo* host_info);

bool ParsePlugin(tinyxml2::XMLElement* root, 
                 std::vector<PluginInfo>* plugin_infos);

bool ParseConfig(tinyxml2::XMLElement* root,
                 std::vector<ConfigInfo>* config_infos);

} // namespace cplugin

#endif // CONFIG_UTIL_H_
