#include "ops.h"

#include <glog/logging.h>
#include <iostream>
#include <rapidjson/document.h>
#include <rapidjson/writer.h>
#include <rapidjson/stringbuffer.h>

#include "http_client.h"

using namespace std;
using namespace rapidjson;

namespace cplugin {

bool Ops::Init() {
  char hostname[1024];
  if (0 != gethostname(hostname, 1024)) {
    LOG(ERROR) << "Cannot get local hostname, error: " << strerror(errno);
    return false; 
  }

  char* pos = strstr(hostname, ".");
  if (pos != NULL)
    *pos = '\0';

  string url;
  url.append("http://ops.sankuai.com/api/stree/host/tag?host=");
  url.append(hostname);

  CHttpClient client;
  client.SetHeader("Authorization: Bearer 6e0f033b45a278d2a6cad32940de88c9b4bd5725");
  long status = 0;
  string response;
  if (0 != client.Get(url, &response, &status)) {
    LOG(ERROR) << "Get owt failed";
  }
  
  LOG(INFO) << response;
  if (status == 200) {
    Document doc;
    doc.Parse<0>(response.c_str());
    if (doc.HasParseError()) {
      LOG(ERROR) << "parse error.";
      return false;
    }
    if (!doc.HasMember("code")) {
      return false;
    }
    int code = doc["code"].GetInt();
    if (code != 200) {
      return false;
    }

    Value& data = doc["data"];
    if (data.IsObject() && data.HasMember(hostname)) {
      Value& host_value = data[hostname];
      for (SizeType i = 0; i < host_value.Size(); ++i) {
        owt_ = host_value[i].GetString();
        LOG(INFO) << owt_;
      }
    } else {
      return false;
    }

    return true;
  }

  LOG(INFO) << "Url: " << url << " status: " << status;
  return false;
}

string Ops::GetOwt() {
  return owt_;
}

bool Ops::ParseSgagentResponse(const string& response, string* config_version) {
  Document doc;
  doc.Parse<0>(response.c_str());
  if (doc.HasParseError()) {
    LOG(ERROR) << "parse error.";
    return false;
  }
  if (!doc.HasMember("code")) {
    return false;
  }
  int code = doc["code"].GetInt();
  if (code != 200) {
    return false;
  }

  *config_version = doc["config_version"].GetString();
  return true;
}

} // namespace cplugin
