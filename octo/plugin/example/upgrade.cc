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
  int32_t ret = client.Upgrade("sg_agent", "3.15", 1, 2);
  if(ret != 0) {
    printf("Upgrade failed %d\r\n", ret);
  }
  transport->close();

  return 0;
}
