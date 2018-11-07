// This autogenerated skeleton file illustrates how to build a server.
// You should copy it to another filename to avoid overwriting it.

#include "MtConfigService.h"
#include <protocol/TBinaryProtocol.h>
#include <server/TSimpleServer.h>
#include <transport/TServerSocket.h>
#include <transport/TBufferTransports.h>

using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;
using namespace ::apache::thrift::server;

using boost::shared_ptr;

using namespace  ;

class MtConfigServiceHandler : virtual public MtConfigServiceIf {
 public:
  MtConfigServiceHandler() {
    // Your initialization goes here
  }

  void getMergeData(ConfigDataResponse& _return, const GetMergeDataRequest& request) {
    // Your implementation goes here
    printf("getMergeData\n");
  }

  int32_t setData(const std::string& appkey, const std::string& env, const std::string& path, const int64_t version, const std::string& jsonData) {
    // Your implementation goes here
    printf("setData\n");
  }

  int32_t syncRelation(const std::vector<ConfigNode> & usedNodes, const std::string& requestIp) {
    // Your implementation goes here
    printf("syncRelation\n");
  }

  void getDefaultConfig(DefaultConfigResponse& _return) {
    // Your implementation goes here
    printf("getDefaultConfig\n");
  }

  void setFileConfig(file_param_t& _return, const file_param_t& files) {
    // Your implementation goes here
    printf("setFileConfig\n");
  }

  void getFileConfig(file_param_t& _return, const file_param_t& files) {
    // Your implementation goes here
    printf("getFileConfig\n");
  }

  void getFileList(file_param_t& _return, const file_param_t& files) {
    // Your implementation goes here
    printf("getFileList\n");
  }

  void distributeConfigFile(ConfigFileResponse& _return, const ConfigFileRequest& request) {
    // Your implementation goes here
    printf("distributeConfigFile\n");
  }

  void enableConfigFile(ConfigFileResponse& _return, const ConfigFileRequest& request) {
    // Your implementation goes here
    printf("enableConfigFile\n");
  }

  void getGroupInfo(ConfigGroupResponse& _return, const std::string& appkey, const std::string& env, const std::string& groupID) {
    // Your implementation goes here
    printf("getGroupInfo\n");
  }

  void getGroups(ConfigGroupsResponse& _return, const std::string& appkey, const std::string& env) {
    // Your implementation goes here
    printf("getGroups\n");
  }

  void addGroup(ConfigGroupResponse& _return, const std::string& appkey, const std::string& env, const std::string& groupName, const std::vector<std::string> & ips) {
    // Your implementation goes here
    printf("addGroup\n");
  }

  void updateGroup(ConfigGroupResponse& _return, const std::string& appkey, const std::string& env, const std::string& groupId, const std::vector<std::string> & ips) {
    // Your implementation goes here
    printf("updateGroup\n");
  }

  int32_t deleteGroup(const std::string& appkey, const std::string& env, const std::string& groupId) {
    // Your implementation goes here
    printf("deleteGroup\n");
  }

  void getGroupID(std::string& _return, const std::string& appkey, const std::string& env, const std::string& ip) {
    // Your implementation goes here
    printf("getGroupID\n");
  }

  bool createPR(const PullRequest& pr, const std::vector<PRDetail> & detailList) {
    // Your implementation goes here
    printf("createPR\n");
  }

  bool detelePR(const int64_t prID) {
    // Your implementation goes here
    printf("detelePR\n");
  }

  bool updatePR(const PullRequest& pr) {
    // Your implementation goes here
    printf("updatePR\n");
  }

  void getPullRequest(std::vector<PullRequest> & _return, const std::string& appkey, const int32_t env, const int32_t status) {
    // Your implementation goes here
    printf("getPullRequest\n");
  }

  bool mergePR(const int64_t prID) {
    // Your implementation goes here
    printf("mergePR\n");
  }

  bool updatePRDetail(const int64_t prID, const std::vector<PRDetail> & detailList) {
    // Your implementation goes here
    printf("updatePRDetail\n");
  }

  void getPRDetail(std::vector<PRDetail> & _return, const int64_t prID) {
    // Your implementation goes here
    printf("getPRDetail\n");
  }

  void getReview(std::vector<Review> & _return, const int64_t prID) {
    // Your implementation goes here
    printf("getReview\n");
  }

  bool createReview(const Review& review) {
    // Your implementation goes here
    printf("createReview\n");
  }

};

int main(int argc, char **argv) {
  int port = 9090;
  shared_ptr<MtConfigServiceHandler> handler(new MtConfigServiceHandler());
  shared_ptr<TProcessor> processor(new MtConfigServiceProcessor(handler));
  shared_ptr<TServerTransport> serverTransport(new TServerSocket(port));
  shared_ptr<TTransportFactory> transportFactory(new TBufferedTransportFactory());
  shared_ptr<TProtocolFactory> protocolFactory(new TBinaryProtocolFactory());

  TSimpleServer server(processor, serverTransport, transportFactory, protocolFactory);
  server.serve();
  return 0;
}
