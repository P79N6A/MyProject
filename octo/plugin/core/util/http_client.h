#ifndef HTTP_CURL_H_ 
#define HTTP_CURL_H_  
  
#include <string>  
#include <curl/curl.h> 

namespace cplugin {

class CHttpClient {  
 public:  
  CHttpClient();  
  ~CHttpClient();  
  
 public:  
  int Post(const std::string& url, const std::string& post,
           std::string* response);
  int Get(const std::string& url, std::string* response, long* status);
  int Posts(const std::string& url, const std::string& post, 
            std::string* response, const char* ca_path = NULL);
  int Gets(const std::string& url, std::string* response, 
           const char* ca_path = NULL);
  
  void SetHeader(const std::string& header);
  int GetStatus();

 public:  
    void SetDebug(bool debug);  
  
 private:  
    bool debug_; 
    curl_slist* chunk_;
};

} // namespace core
  
#endif // HTTP_CURL_H_ 
