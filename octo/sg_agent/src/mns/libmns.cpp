#include "mns_impl.h"
#include "mns_zk_tools.h"

#include <unistd.h>
#include <signal.h>
#include <pthread.h>
#include <core/pluginimpl.h>
#include <iostream>
#include <uuid/uuid.h>
#include "util/global_def.h"
#include "util/idc_util.h"
using namespace sg_agent;
using namespace std;

HostServices *g_host_services = NULL;
GlobalVar *g_mns_global_var = NULL;

int InitializePlugin(
    const std::map<std::string, HANDLE> &preload_map,
    const HostServices &host_services,
    PluginInfo info) {
  LOG_INFO("start to init libmns.so");
  g_host_services = new HostServices;
  *g_host_services = host_services;

  g_mns_global_var = static_cast<GlobalVar *>(g_host_services->context);
  LOG_INFO("InitializePlugin cp_agent env is: " << g_mns_global_var->gEnvStr);

  int ret = MnsZkTools::Init();
  if (0 != ret) {
    LOG_ERROR("Init MnsZkTools fail! ret = " << ret);
    return ret;
  }
	LOG_ERROR("this is the start the InitializePlugin");
  IdcUtil::StartCheck(); 	
	RegisterParams service_params;
  service_params.create_func = MnsImpl::Create;
  service_params.destroy_func = MnsImpl::Destroy;
  service_params.library_name = info.library_name;
  host_services.register_func("ProtocolServicePlugin", service_params);



  return ret;

}

int UninitializePlugin() {
  LOG_INFO("UninitializePlugin cp_agent");
  if (g_host_services) {
    delete g_host_services;
  }
  return SUCCESS;
}

