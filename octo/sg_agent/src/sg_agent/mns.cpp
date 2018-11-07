#include <comm/inc_comm.h>
#include <core/plugin_manager.h>
#include <util/sg_agent_env.h>

#include "mns.h"
#include "util/global_def.h"

using namespace std;
using namespace cplugin;

extern GlobalVar *g_global_var;

namespace sg_agent {

//TODO:
const double kGCInterval = 15 * 60.0;

MNS::MNS()
    : protocol_plugin_(NULL),
      service_loop_(NULL),
      route_loop_(NULL),
      servicename_loop_(NULL),
      gc_loop_(NULL) {
}

int MNS::Init() {
  RegisterParams rp;
  if (!PluginManager::Instance()->FindClassCreator("ProtocolServicePlugin",
                                                   &rp)) {
    LOG_ERROR("don't find ProtoclServicePlugin class.");
    return -1;
  }

  plugin_register_params_ = rp;

  int ret;
  protocol_plugin_ = static_cast<IMnsPlugin *>(rp.create_func());
  ret = protocol_plugin_->Init(g_global_var->gIp, g_global_var->gMask);
  if (0 != ret) {
    LOG_ERROR("failed to init ProtocolServicePlugin, errorCode = " << ret);
    return ret;
  }

  service_loop_ = servicename_loop_thread_.startLoop();
  service_loop_->runEvery(DEFAULT_PROTOCOL_SCANTIME,
                          boost::bind(&MNS::UpdateSrvListTimer, this));

  route_loop_ = route_loop_thread_.startLoop();
  route_loop_->runEvery(DEFAULT_PROTOCOL_MAX_UPDATETIME,
                        boost::bind(&MNS::UpdateRouteTimer, this));

  servicename_loop_ = servicename_loop_thread_.startLoop();
  servicename_loop_->runEvery(DEFAULT_PROTOCOL_SCANTIME,
                              boost::bind(&MNS::UpdateSrvNameTimer, this));

  appkeydesc_loop_ = appkeydesc_loop_thread_.startLoop();
  appkeydesc_loop_->runEvery(DEFAULT_PROTOCOL_SCANTIME,
                             boost::bind(&MNS::UpdateAppkeyDescTimer, this));

  gc_loop_ = object_gc_thread_.startLoop();

  return 0;
}

void MNS::UpdateSrvListTimer() {
  if (protocol_plugin_) {
    protocol_plugin_->UpdateSrvListTimer();
  }
}

void MNS::UpdateRouteTimer() {
  if (protocol_plugin_) {
    protocol_plugin_->UpdateRouteTimer();
  }
}

void MNS::UpdateSrvNameTimer() {
  if (protocol_plugin_) {
    protocol_plugin_->UpdateSrvNameTimer();
  }
}

void MNS::UpdateAppkeyDescTimer() {
  if (protocol_plugin_) {
    protocol_plugin_->UpdateAppkeyDescTimer();
  }
}

void MNS::GCTimer() {
  RegisterParams rp = trash_plugin_register_params_.front();
  trash_plugin_register_params_.erase(trash_plugin_register_params_.begin());

  IMnsPlugin *protocol_plugin = trash_protocol_plugins_.front();
  trash_protocol_plugins_.erase(trash_protocol_plugins_.begin());

  rp.destroy_func(protocol_plugin);
  PluginManager::Instance()->UnloadPlugin(rp.library_name);

  LOG_INFO("dclose so!!!");
}

int MNS::ResetMnsPlugin() {
  //update mns objects
  RegisterParams rp;
  if (!PluginManager::Instance()->FindClassCreator("ProtocolServicePlugin",
                                                   &rp)) {
    LOG_ERROR("don't find ProtoclServicePlugin class.");
    return -1;
  }

  trash_plugin_register_params_.push_back(plugin_register_params_);
  plugin_register_params_ = rp;

  trash_protocol_plugins_.push_back(protocol_plugin_);
  protocol_plugin_ = static_cast<IMnsPlugin *>(rp.create_func());

  int ret = protocol_plugin_->Init(g_global_var->gIp, g_global_var->gMask);
  if (0 != ret) {
    LOG_ERROR("failed to init ProtocolServicePlugin, errorCode = " << ret);
    return ret;
  }

  gc_loop_->runAfter(kGCInterval, boost::bind(&MNS::GCTimer, this));
  return 0;
}

} //namespace sg_agent
