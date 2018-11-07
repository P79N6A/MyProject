#include <unistd.h>
#include <signal.h>
#include <pthread.h>
#include <core/pluginimpl.h>
#include <glog/logging.h>
#include <protocol/TBinaryProtocol.h>
#include <server/TNonblockingServer.h>
#include <transport/TServerSocket.h>
#include <transport/TBufferTransports.h>
#include <concurrency/ThreadManager.h>
#include <concurrency/PosixThreadFactory.h>

using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;
using namespace ::apache::thrift::server;
using namespace ::apache::thrift::concurrency;

using namespace std;

static pthread_t g_tid;
typedef TProcessor* (*CreateObject)();
static volatile CreateObject g_create_fn = NULL;
static HANDLE volatile g_sgagent_handle = NULL;
static TProcessor* volatile g_processor = NULL;
static TNonblockingServer* volatile g_server = NULL; 

static void* StartServer(void*) {
  LOG(INFO) << "Server start.";
  
  const char* clientPort = "5266";

  int port = atoi(clientPort);

  boost::shared_ptr<TProcessor> processor(g_processor);
  boost::shared_ptr<TProtocolFactory> protocolFactory(new TBinaryProtocolFactory());

  boost::shared_ptr<ThreadManager> threadManager
      = ThreadManager::newSimpleThreadManager(1);
  boost::shared_ptr<PosixThreadFactory> threadFactory
      = boost::shared_ptr<PosixThreadFactory > (new PosixThreadFactory());
  threadManager->threadFactory(threadFactory);
  threadManager->start();
  
  TNonblockingServer server(processor, protocolFactory, port, threadManager);
  g_server = &server;

  try {
    server.serve();
  } catch(TException &e) {
    return NULL;
  }

  return NULL;
}

bool InitializePlugin(const std::map<std::string, HANDLE>& preload_map, 
                      const map<string, HANDLE>& handle_map) {
  LOG(INFO) << "Plugin server InitializePlugin.";
  g_sgagent_handle = handle_map.at("sgagent");
  g_create_fn = (CreateObject)dlsym(g_sgagent_handle, "CreateProcessorObject");
  g_processor = g_create_fn();

  pthread_create(&g_tid, NULL, StartServer, NULL);
  return true;  
}

bool UninitializePlugin() {
  LOG(INFO) << "Plugin server UninitializePlugin.";
  return true;
}

bool UpdatePluginNotify(
    const map<string, HANDLE>& update_handle_map) {
  LOG(INFO) << "Notify plugin server to update.";

  if (update_handle_map.find("sgagent") != update_handle_map.end()) {
    HANDLE sgagent_handle = g_sgagent_handle;
    g_sgagent_handle = update_handle_map.at("sgagent");

    g_create_fn = (CreateObject)dlsym(g_sgagent_handle, "CreateProcessorObject");
    g_processor = g_create_fn();
    boost::shared_ptr<TProcessor> processor(g_processor);
    boost::shared_ptr<TProcessorFactory> process_factory(
        new TSingletonProcessorFactory(processor));
    g_server->setProcessorFactory(process_factory); 
    g_server->switchSgagentIndex();
    while (g_server->getOldSgagentHandlers() != 0) {
      usleep(1000 * 1000);
    }

    typedef TProcessor* (*DestroyObject)();
    DestroyObject destroy_fn = (DestroyObject)dlsym(sgagent_handle, 
                                                    "DestroyProcessorObject");
    destroy_fn();
  }

  return true;
}
