#include <unistd.h>
#include <string>
#include <iostream>
#include <transport/TSocket.h>
#include <protocol/TBinaryProtocol.h>
#include <transport/TBufferTransports.h>
#include "SGAgentHandler.h"
#include <core/gen-cpp/Core.h>

using namespace std;
using namespace cplugin;
using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;

int main() {
  boost::shared_ptr<TSocket> socket(new TSocket("127.0.0.1", 5288));
  boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
  boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));

  CoreClient client(protocol);
  transport->open();
  // Notify core to update config
  std::map<std::string, std::string>  _return;
  std::vector<std::string>  sgs;
  sgs.push_back("cr_agent");
  sgs.push_back("sg_agent");
  client.GetMonitorInfos(_return,  sgs);
  std::map<std::string, std::string>::iterator it = _return.begin();
  for(; it != _return.end(); it++){
    printf("%s     :    %s\r\n", it->first.c_str(), it->second.c_str());
  }

  transport->close();

  return 0;
}
