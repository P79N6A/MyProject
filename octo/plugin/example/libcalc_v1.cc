#include "libcalc.h"

#include <core/pluginimpl.h>
#include <muduo/base/Logging.h>
#include <muduo/base/TimeZone.h>

using namespace std;

class Test : public ITest {
 public:
  static void* Create();
  static void Destroy(void* p);
  virtual ~Test() {}
  virtual int Add(int a, int b); 
  virtual int Desc(int a, int b); 
};

int Test::Add(int a, int b) {
  return a + b;
}

int Test::Desc(int a, int b) {
  return a - b;
}

void* Test::Create() {
  ITest* p = new Test;
  return p;
}

void Test::Destroy(void* p) {
  if (p) {
    ITest* p1 = static_cast<ITest*>(p);
    delete p1;
  }
}

int InitializePlugin(
    const std::map<std::string, HANDLE>& preload_map, 
    const HostServices& host_services,
    PluginInfo info) {
  LOG_INFO << "calc_v1 init.";

  RegisterParams rp;
  rp.create_func = Test::Create;
  rp.destroy_func = Test::Destroy;
  host_services.register_func("Test", rp);

  return 0;  
}

int UninitializePlugin() {
  return 0;
}
