#ifndef PLUGINIMPL_H_
#define PLUGINIMPL_H_

#include <dlfcn.h>
#include <map>
#include <string>

#include "define/port.h"
#include "plugindef.h"

static long g_refcount = 0;

// Plugin need to implement this local api for plugin initialization, called
// by internal function InitPlugin.
LOCALAPI int InitializePlugin(
    const std::map<std::string, HANDLE>& preload_map, 
    const HostServices& host_services,
    PluginInfo info);

// Plugin need to implement this local api for plugin uninitialization, called
// by internal function FreePlugin.
LOCALAPI int UninitializePlugin();

// Called by core.
OUTAPI int InitPlugin(const std::map<std::string, HANDLE>& preload_map, 
                      const HostServices& host_services,
                      PluginInfo info) {
  if (++g_refcount != 1) {
    return -1999;
  }

  return InitializePlugin(preload_map, host_services, info);
}

// Called by core.
OUTAPI int FreePlugin() {
  if (--g_refcount != 0) {
    return -1999;
  }
  
  return UninitializePlugin();
}

#endif // PLUGINIMPL_H_
