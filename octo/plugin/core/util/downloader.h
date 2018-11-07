#ifndef DOWNLOADER_H_
#define DOWNLOADER_H_

#include <string>

namespace cplugin {

class Downloader {
 public:
  Downloader();
  ~Downloader();

  void Init(const std::string& host, const std::string& port);

  int GetConfig(std::string* response, const std::string& version, const std::string& plugin,
                const std::string& cnetos, const std::string& cfgname = "config.xml");
  int GetDSO(const std::string& lib_name,const std::string& version,  const std::string& cnetos, std::string* response);
 private:
  bool is_init_;

  std::string host_;
  std::string port_;
};

} // namespace cplugin 

#endif // DOWNLOADER_H_
