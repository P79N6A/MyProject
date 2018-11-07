#include <unistd.h>
#include <string>
#include <iostream>
#include <transport/TSocket.h>
#include <protocol/TBinaryProtocol.h>
#include <transport/TBufferTransports.h>
#include "SGAgentHandler.h"
#include <core/gen-cpp/Core.h>

using namespace std;
using namespace ::apache::thrift;
using namespace ::apache::thrift::protocol;
using namespace ::apache::thrift::transport;

int main() {
  boost::shared_ptr<TSocket> socket(new TSocket("localhost", 5277));
  boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
  boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
  SGAgentClient client(protocol); 

  transport->open();
  SGService sgs;
  client.registService(sgs);
  transport->close(); 

  return 0;
}
