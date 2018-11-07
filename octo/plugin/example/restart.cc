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
    // Notify core to start plugin
    int32_t ret = client.ReStart("sg_agent", 1, 2);
    if(ret != 0) {
        printf("restart failed %d\r\n", ret);
    }

    transport->close();


    return 0;
}
