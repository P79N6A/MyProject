#ifndef CORE_SERVER_H_
#define CORE_SERVER_H_

namespace cplugin {

class HostManager;

class CoreServer {
 public:
  explicit CoreServer(HostManager*); 
  ~CoreServer();
  bool Start();
 private:
  HostManager* host_manager_;
};

} // namespace cplugin 

#endif // CORE_SERVER_H_
