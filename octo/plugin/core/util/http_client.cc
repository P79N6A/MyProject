#include "http_client.h"

#include <string>  

namespace cplugin {
  
CHttpClient::CHttpClient() 
  : debug_(false),
    chunk_(NULL) {}  
  
CHttpClient::~CHttpClient() {}  
  
static int OnDebug(
    CURL*, curl_infotype type, char* data, size_t size, void*) { 
  switch (type) {
    case CURLINFO_TEXT:
      printf("[CURLINFO_TEXT]%s\n", data);
      break;
    case CURLINFO_HEADER_IN:
      printf("[CURLINFO_HEADER_IN]%s\n", data);
      break;
    case CURLINFO_HEADER_OUT:
      printf("[CURLINFO_HEADER_OUT]%s\n", data);
      break;
    case CURLINFO_DATA_IN:
      printf("[CURLINFO_DATA_IN]%s\n", data);
      break;
    case CURLINFO_DATA_OUT:
      printf("[CURLINFO_DATA_OUT]%s\n", data);
      break;
    case CURLINFO_SSL_DATA_IN:
      printf("[CURLINFO_SSL_DATA_IN]%s\n", data);
          break;
    case CURLINFO_SSL_DATA_OUT:
      printf("[CURLINFO_SSL_DATA_OUT]%s\n", data);
          break;
    case CURLINFO_END:
      printf("[CURLINFO_END]%s\n", data);
      break;
  }
  return 0;  
}  
  
static int OnWriteData(void* buffer, size_t size, size_t nmemb, void* lpVoid) 
{  
  std::string* str = dynamic_cast<std::string*>((std::string*)lpVoid);  
  if(!str || !buffer) {  
    return -1; 
  }  
  
  char* data = static_cast<char*>(buffer);  
  str->append(data, size * nmemb);  
  return nmemb;  
}  
  
void CHttpClient::SetHeader(const std::string& header) {
  chunk_ = curl_slist_append(chunk_, header.c_str());
}

int CHttpClient::Post(
  const std::string& url, const std::string& post, std::string* response) {  
  CURLcode res;  
  CURL* curl = curl_easy_init();  
  if(!curl) {  
    return CURLE_FAILED_INIT;  
  } 

  if(debug_) { 
    curl_easy_setopt(curl, CURLOPT_VERBOSE, 1);  
    curl_easy_setopt(curl, CURLOPT_DEBUGFUNCTION, OnDebug);  
  }

  curl_easy_setopt(curl, CURLOPT_URL, url.c_str());  
  curl_easy_setopt(curl, CURLOPT_POST, 1);  
  curl_easy_setopt(curl, CURLOPT_POSTFIELDS, post.c_str());  
  curl_easy_setopt(curl, CURLOPT_READFUNCTION, NULL);  
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, OnWriteData);  
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, static_cast<void*>(response));  
  curl_easy_setopt(curl, CURLOPT_NOSIGNAL, 1);  
  curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 3);  
  curl_easy_setopt(curl, CURLOPT_TIMEOUT, 60); // timeout 60s 
  res = curl_easy_perform(curl);  
  curl_easy_cleanup(curl);  
  return res;  
} 
  
int CHttpClient::Get(const std::string& url, 
                     std::string* response, long* status) {
  CURLcode res;  
  CURL* curl = curl_easy_init();  
  if(!curl) {  
    return CURLE_FAILED_INIT;  
  }  
  if(debug_) {  
    curl_easy_setopt(curl, CURLOPT_VERBOSE, 1);  
    curl_easy_setopt(curl, CURLOPT_DEBUGFUNCTION, OnDebug);  
  }
  if (NULL != chunk_) {
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, chunk_);
  }  
  curl_easy_setopt(curl, CURLOPT_URL, url.c_str());  
  curl_easy_setopt(curl, CURLOPT_READFUNCTION, NULL);  
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, OnWriteData);  
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, static_cast<void*>(response));  

  curl_easy_setopt(curl, CURLOPT_NOSIGNAL, 1);  
  curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 3);  
  curl_easy_setopt(curl, CURLOPT_TIMEOUT, 60); // timeout 60s 
  res = curl_easy_perform(curl);
  if (res == CURLE_OK) {
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, status);
  }
  curl_easy_cleanup(curl);  
  curl_slist_free_all(chunk_);

  return res;  
}

int CHttpClient::GetStatus() {
  return -1;
}
  
int CHttpClient::Posts(const std::string& url, const std::string& post, 
                       std::string* response, const char* ca_path) { 
  CURLcode res; 
  CURL* curl = curl_easy_init();  
  if(!curl) {  
    return CURLE_FAILED_INIT;  
  }  
  if(debug_) {  
    curl_easy_setopt(curl, CURLOPT_VERBOSE, 1);  
    curl_easy_setopt(curl, CURLOPT_DEBUGFUNCTION, OnDebug);  
  }  
  curl_easy_setopt(curl, CURLOPT_URL, url.c_str());  
  curl_easy_setopt(curl, CURLOPT_POST, 1);  
  curl_easy_setopt(curl, CURLOPT_POSTFIELDS, post.c_str());  
  curl_easy_setopt(curl, CURLOPT_READFUNCTION, NULL);  
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, OnWriteData);  
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, static_cast<void*>(response));  
  curl_easy_setopt(curl, CURLOPT_NOSIGNAL, 1);  
  if(!ca_path) {  
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, false);  
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, false);  
  } else {  
    // Default case: "PEM", don't need to set here  
    // curl_easy_setopt(curl,CURLOPT_SSLCERTTYPE,"PEM");  
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, true);  
    curl_easy_setopt(curl, CURLOPT_CAINFO, ca_path);  
  }  
  curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 3);  
  curl_easy_setopt(curl, CURLOPT_TIMEOUT, 3);  
  res = curl_easy_perform(curl);  
  curl_easy_cleanup(curl);  
  return res;  
}  
  
int CHttpClient::Gets(
    const std::string& url, std::string* response, const char* ca_path) { 
  CURLcode res;  
  CURL* curl = curl_easy_init();  
  if(!curl) {  
    return CURLE_FAILED_INIT;  
  }  
  if(debug_) {  
    curl_easy_setopt(curl, CURLOPT_VERBOSE, 1);  
    curl_easy_setopt(curl, CURLOPT_DEBUGFUNCTION, OnDebug);  
  }  
  curl_easy_setopt(curl, CURLOPT_URL, url.c_str());  
  curl_easy_setopt(curl, CURLOPT_READFUNCTION, NULL);  
  curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, OnWriteData);  
  curl_easy_setopt(curl, CURLOPT_WRITEDATA, static_cast<void*>(response));  
  curl_easy_setopt(curl, CURLOPT_NOSIGNAL, 1);  
  if(!ca_path) {  
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, false);  
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, false);  
  } else {  
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, true);  
    curl_easy_setopt(curl, CURLOPT_CAINFO, ca_path);  
  }  
  curl_easy_setopt(curl, CURLOPT_CONNECTTIMEOUT, 3);  
  curl_easy_setopt(curl, CURLOPT_TIMEOUT, 3);  
  res = curl_easy_perform(curl);  
  curl_easy_cleanup(curl);  
  return res;  
}  
  
void CHttpClient::SetDebug(bool debug) {  
    debug_ = debug;  
}

} // namespace core
