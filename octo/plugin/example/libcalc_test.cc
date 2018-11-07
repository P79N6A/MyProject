#include <unistd.h>
#include <pthread.h>
#include <iostream>
#include <core/hostimpl.h>
#include <core/plugin_manager.h>
#include <boost/bind.hpp>
#include <muduo/base/Logging.h>
#include <muduo/base/TimeZone.h>
#include <muduo/net/EventLoop.h>
#include <muduo/net/EventLoopThread.h>
#include <muduo/net/EventLoopThreadPool.h>

#include "libcalc.h"

using namespace std;
using namespace boost;
using namespace cplugin;
using namespace muduo;
using namespace muduo::net;

#define SafeCall(p, f) if (p) p->f
#define SafeCallIf(p, f, v)                     ((p) ? (p->f) : (v))

static ITest* volatile g_test = NULL;

static EventLoopThread* g_loop_thread = NULL;
static EventLoop* g_loop = NULL;
static PluginManager* g_plugin_manager = NULL;

void print() {
  cout << "Test add result: " << SafeCallIf(g_test, Add(1, 4), 0) << endl;
}

OUTAPI int InitHost(const std::map<std::string, HANDLE>& preload_map, 
                     const std::vector<PluginInfo>& plugins) {
  LOG_INFO << "Plugin calc test initialization.";

  g_plugin_manager = PluginManager::Instance();

  HostServices host_services;
  host_services.register_func = PluginManager::RegisterFunc;
  g_plugin_manager->LoadPlugins(preload_map, plugins, host_services);

  RegisterParams rp;
  if (!g_plugin_manager->FindClassCreator("Test", &rp)) {
    LOG_INFO << "don't find class Test.";
    return -1;
  }

  g_test = static_cast<ITest*>(rp.create_func());

  g_loop_thread = new EventLoopThread();
  g_loop = g_loop_thread->startLoop();
  g_loop->runEvery(1.0, boost::bind(&print));

  return 0;  
}

OUTAPI int UpgradePlugins(const std::vector<PluginInfo>& plugins) {
  LOG_INFO << "Notify plugin calc test to update.";

  map<std::string, HANDLE> preload_map;
  HostServices host_services;
  host_services.register_func = PluginManager::RegisterFunc;

  vector<string> updated_plugins;
  g_plugin_manager->UpgradePlugins(preload_map, plugins, host_services, &updated_plugins);

  // get new class
  RegisterParams rp;
  if (!g_plugin_manager->FindClassCreator("Test", &rp)) {
    LOG_INFO << "don't find class Test.";
    return -1;
  }

  g_test = static_cast<ITest*>(rp.create_func());

  return 0;
}
