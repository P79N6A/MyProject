#include "downloader.h"

#include <glog/logging.h>
#include <iostream>
#include <boost/algorithm/string.hpp>

#include "http_client.h"

using namespace std;

namespace cplugin {

Downloader::Downloader()
  : is_init_(false)
   {}

Downloader::~Downloader() {}

void Downloader::Init(const std::string& host, const std::string& port){
  host_ = host;
  port_ = port;

  is_init_ = true;
}

int Downloader::GetConfig(string* response, const string& version, const string& plugin,
                          const string& centos, const std::string& cfgname) {

  if(!is_init_){
    LOG(ERROR) << "need init";
    return -1;
  }

  string url; 
  url.append("http://");
  url.append(host_);
  url.append(":");
  url.append(port_);
  url.append("/res/"  + centos + "/opt/meituan/apps/" + plugin  + "/" + version + "/" + cfgname);

  CHttpClient client;
  long status = 0;
  if (0 != client.Get(url, response, &status)) {
    LOG(ERROR) << "Get config failed";
    cout << response << endl;
  }
  
  if (status != 200) {
    LOG(ERROR) << "Url: " << url << " status: " << status;
    return -1;
  }

  if(response->empty()){
    LOG(ERROR) << "Url: " << url << " body size empty "  ;
    return -1;
  }

  return 0;
}

///opt/meituan/www/res/opt/meituan/apps/sg_agent/libsgcommon.so
// add version

string FmormatVersionPath(const string& version, const string& lib_name) {
   std::size_t  pos = lib_name.find_last_of("/");
   if(pos == std::string::npos){
     return lib_name;
   }else{
     string tempPrefix = lib_name.substr(0, pos);
     string tempfile = lib_name.substr(pos);

     return (tempPrefix + "/" + version + tempfile);

   }
}

int Downloader::GetDSO(const string& lib_name, const string& version,  const std::string& centos, string* response) {

  if(!is_init_){
    LOG(ERROR) << "need init";
    return -1;
  }

  string url;
  url.append("http://");
  url.append(host_);
  url.append(":");
  url.append(port_);
  url.append("/res/");
  url.append(centos);
  url.append("/");
  url.append(FmormatVersionPath(version, lib_name));


  CHttpClient client;
  long status = 0;
  if (0 != client.Get(url, response, &status)) {
    LOG(ERROR) << "Get dso failed";
  }

  if (status != 200) {
    LOG(INFO) << "Url: " << url << " status: " << status;
    return -1;
  }


  if(response->empty()){
    LOG(ERROR) << "Url: " << url << " body size empty "  ;
    return -1;
  }

  return 0;
}

} // namespace cplugin 
