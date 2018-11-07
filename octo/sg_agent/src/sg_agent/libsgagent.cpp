#include <unistd.h>
#include <pthread.h>
#include <algorithm>
#include <boost/bind.hpp>
#include <core/hostimpl.h>
#include <core/plugin_manager.h>

#include "comm/log4cplus.h"
#include "SGAgentHandler.h"
#include "service_def.h"

#include <server/TNonblockingServer.h>
#include <concurrency/PosixThreadFactory.h>

#include "zk_client.h"
#include "mnscache_client.h"
#include "mns/mns_impl.h"
#include "sg_agent_conf.h"
#include "util/global_def.h"
#include "util/idc_util.h"
#include "remote/falcon/falcon_collector.h"
#include "mns.h"
#include "sg_agent_init.h"
#include <sys/prctl.h>
#include "util/config_adapter.h"
#include "http_service.h"
#include "remote/monitor/monitor_collector.h"

using namespace std;
using namespace cplugin;
using namespace sg_agent;
using namespace ::apache::thrift;

using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;
using namespace ::apache::thrift::server;
using namespace ::apache::thrift::concurrency;

using boost::shared_ptr;

void *g_handle = NULL;
GlobalVar *g_global_var = NULL;
MNS *g_mns = NULL;
SgAgentInit *g_init = NULL;
bool g_is_test = false;

static pthread_t g_tid;

void *StartServer(void *) {
  int port = g_is_test ? 5268 : 5266;

  sg_agent::SGAgentHandler *my_handler = new sg_agent::SGAgentHandler();
  if (my_handler->Init() != 0) {
    LOG_ERROR("ERR SGAgentHandler init failed");
    return NULL;
  }

  LOG_INFO("RUN Process : " << getpid()
                            << ", start serve(), port = " << port
                            << ", env = " << g_global_var->gEnvStr);

  shared_ptr<sg_agent::SGAgentHandler> handler(my_handler);
  shared_ptr<TProcessor> processor(new SGAgentProcessor(handler));
  shared_ptr<TProtocolFactory> protocolFactory(new TBinaryProtocolFactory());

  boost::shared_ptr<ThreadManager> threadManager
      = ThreadManager::newSimpleThreadManager(1);
  boost::shared_ptr<PosixThreadFactory> threadFactory
      = boost::shared_ptr<PosixThreadFactory>(new PosixThreadFactory());
  threadManager->threadFactory(threadFactory);
  threadManager->start();

  TNonblockingServer server(processor, protocolFactory, port, threadManager);

  try {
    server.serve();
  } catch (TException &e) {
    return NULL;
  }

  return NULL;
}

int sg_agent::InvokeService(int service_name, void *service_params) {
  int ret = 0;
  timeval tvalStart;
  timeval tvalEnd;
  long deltaTime;
  gettimeofday(&tvalStart, NULL);
  switch (service_name) {
    case ZK_GET: {
      ZkGetInvokeParams *req = static_cast<ZkGetInvokeParams *>(service_params);
      ret = ZkClient::getInstance()->ZkGet(req);
      gettimeofday(&tvalEnd, NULL);
      deltaTime = DeltaTime(tvalEnd, tvalStart);
      LOG_DEBUG("zk get deltaTime: " << deltaTime);
      break;
    }
    case ZK_WGET: {
      ZkWGetInvokeParams *req = static_cast<ZkWGetInvokeParams *>(service_params);
      ret = ZkClient::getInstance()->ZkWGet(req);
      LOG_DEBUG("Invoker wget_zk, serive_name: "
                    << service_name
                    << ", zk_path: "
                    << req->zk_wget_request.path);
      gettimeofday(&tvalEnd, NULL);
      deltaTime = DeltaTime(tvalEnd, tvalStart);
      LOG_DEBUG("zk wget deltaTime: " << deltaTime);
      break;
    }
    case ZK_WGET_CHILDREN: {
      ZkWGetChildrenInvokeParams *req = static_cast<ZkWGetChildrenInvokeParams *>(service_params);
      ret = ZkClient::getInstance()->ZkWGetChildren(req);
      gettimeofday(&tvalEnd, NULL);
      deltaTime = DeltaTime(tvalEnd, tvalStart);
      LOG_DEBUG("zk wget children deltaTime: " << deltaTime);
      break;
    }
    case GET_MNS_CACHE: {
      MNSCacheRequest *req = static_cast<MNSCacheRequest *>(service_params);
      ret = MnsCacheCollector::getInstance()->getServiceList(*req->serviceList,
                                                             req->providerSize,
                                                             *req->appkey,
                                                             *req->version,
                                                             *req->env,
                                                             *req->protocol);
      gettimeofday(&tvalEnd, NULL);
      deltaTime = DeltaTime(tvalEnd, tvalStart);
      LOG_DEBUG("mns get deltaTime: " << deltaTime);

      break;
    }
    default:break;
  }

  return ret;
}

int DoSgAgentInit(const map<std::string, HANDLE> &preload_map,
                  const std::vector<PluginInfo> &plugins) {

  // start to check the healthy
  g_init->CheckHealthy();
  // wait until it's healthy.
  g_init->healthy_countdownlatch.wait();  //wait until healthy
  // start to falcon upload.
  FalconCollector::StartCollect();
	
	PluginManager::Instance()->Init();

  HostServices host_services;
  host_services.register_func = PluginManager::RegisterFunc;
  host_services.invoke_service_func = sg_agent::InvokeService;
  host_services.context = g_global_var;

  if (!PluginManager::Instance()->LoadPlugins(preload_map, plugins, host_services)) {
    LOG_ERROR("failed to load plugins");
    return false;
  }

  g_mns = new MNS();

  int ret = g_mns->Init();
  if (0 != ret) {
    LOG_ERROR("failed to init mns Class, errorCode = " << ret);
    return ret;
  }
  //http thread
  SgHttpService::GetInstance()->StartHttpServer();
  pthread_create(&g_tid, NULL, StartServer, NULL);

  LOG_INFO("==================================【END】 init sg_agent host=============================================");

  return SUCCESS;
}

int InitHost(const map<std::string, HANDLE> &preload_map,
             const std::vector<PluginInfo> &plugins) {

  prctl(PR_SET_NAME, "sg_agent");  //specify name for thread for core recognized reason

  //must be init log first
  log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT(
                                                   "/opt/meituan/apps/sg_agent/log4cplus.conf"));

  LOG_INFO("=================================【START】 init sg_agent host=============================================");

  if (!g_is_test) {
    if (preload_map.find("sgcommon") == preload_map.end()) {
      LOG_ERROR("no libsgcommon found.");
      return FAILURE;
    }
    g_handle = preload_map.at("sgcommon");
  }

  g_init = new SgAgentInit();
  // use another thread to run the sg_agent initialization， in order to quick return cplugin
  g_init->sg_agent_sub_loop_->runInLoop(boost::bind(DoSgAgentInit,
                                                    preload_map,
                                                    plugins));
  sg_agent::ConfigAdapter::GetInstance()->Init();
  return SUCCESS;
}

int UpgradePlugins(const std::vector<PluginInfo> &plugins) {
  LOG_INFO("plugins's size = " << plugins.size());

  map<std::string, HANDLE> preload_map;
  HostServices host_services;
  host_services.register_func = PluginManager::RegisterFunc;
  host_services.invoke_service_func = sg_agent::InvokeService;
  host_services.context = g_global_var;

  vector<string> updated_plugins;
  bool is_updated = PluginManager::Instance()->UpgradePlugins(preload_map,
                                                              plugins,
                                                              host_services,
                                                              &updated_plugins);

  // If some so updated, update corresponding plugin objects
  if (is_updated) {
    vector<string>::iterator it = std::find(updated_plugins.begin(),
                                            updated_plugins.end(),
                                            string("mns"));
    if (it != updated_plugins.end()) {
      g_mns->ResetMnsPlugin();
    }
  }

  return SUCCESS;
}

int GetPluginInfos(std::vector<PluginInfo> *plugins) {
  return PluginManager::Instance()->GetPluginInfos(plugins);
}

int Monitor(std::string& monitor_info){

  if (SUCCESS != SgMonitorCollector::GetInstance()->GetCollectorMonitorInfo(monitor_info)) {
    LOG_ERROR("Get collectorMonitorInfo failed");
    return FAILURE;
  }
  return SUCCESS;
}

