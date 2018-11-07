#include <unistd.h>
#include <pthread.h>
#include <boost/bind.hpp>
#include <core/pluginimpl.h>
#include <glog/logging.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/net/EventLoopThreadPool.h>

#include "SGAgentHandler.h"

using namespace std;
using namespace ::apache::thrift;

static TProcessor* volatile g_processor = NULL; 

bool InitializePlugin(const std::map<std::string, HANDLE>& preload_map, 
                      const map<string, HANDLE>& handle_map) {
  LOG(INFO) << "Sgagent_v1 initialization.";
  return true;  
}

bool UninitializePlugin() {
  LOG(INFO) << "Sgagent_v1 uninitialization.";
  return true;
}

bool UpdatePluginNotify(
    const map<string, HANDLE>& update_handle_map) {
  LOG(INFO) << "Notify plugin sgagent_v1 to update.";
  return true;
}

OUTAPI TProcessor* CreateProcessorObject() {
  LOG(INFO) << "Sgagent_v1 create processor object.";
  sg_agent::SGAgentHandler* my_handler = new sg_agent::SGAgentHandler();
  boost::shared_ptr<sg_agent::SGAgentHandler> handler(my_handler);
  g_processor = new SGAgentProcessor(handler);
  return g_processor;
}

OUTAPI void DestroyProcessorObject() {
  LOG(INFO) << "Sgagent_v1 destroy processor object.";
  if (g_processor) {
    delete g_processor;
    g_processor = NULL;
  }
}
