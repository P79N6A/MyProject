#ifndef HOSTIMPL_H_
#define HOSTIMPL_H_

#include <string>
#include <vector>
#include <map>

#include "define/port.h"
#include "plugindef.h"

// Called by core.
OUTAPI int InitHost(const std::map<std::string, HANDLE>& preload_map, 
                     const std::vector<PluginInfo>& plugins);

OUTAPI int Monitor(std::string&);

OUTAPI int UpgradePlugins(const std::vector<PluginInfo>& plugins);

OUTAPI int GetPluginInfos(std::vector<PluginInfo>* plugins);

OUTAPI int Monitor(std::string &monitor_info);
#endif // HOSTIMPL_H_
