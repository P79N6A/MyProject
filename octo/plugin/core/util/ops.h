#ifndef OPS_H_
#define OPS_H_

#include <string>

namespace cplugin {

class Ops {
 public:
  Ops() {} 
  ~Ops() {}
  bool Init();
  std::string GetOwt();
  bool ParseSgagentResponse(const std::string& response, std::string* config_version);
 private:
  std::string owt_;
};

} // namespace cplugin

#endif // OPS_H_
