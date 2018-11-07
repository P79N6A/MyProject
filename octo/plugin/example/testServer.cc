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

int32_t testRestart(){
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

int32_t testStop(){
    boost::shared_ptr<TSocket> socket(new TSocket("127.0.0.1", 5288));
    boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
    boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));

    CoreClient client(protocol);
    transport->open();
    // Notify core to stop plugin
    int32_t ret = client.Stop("sg_agent", 1, 2);
    if(ret != 0) {
        printf("stop failed %d\r\n", ret);
    }

    transport->close();

    return 0;
}

int32_t testStart(){
    boost::shared_ptr<TSocket> socket(new TSocket("127.0.0.1", 5288));
    boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
    boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));

    CoreClient client(protocol);
    transport->open();
    // Notify core to start plugin
    int32_t ret = client.Start("sg_agent", 1, 2);
    if(ret != 0) {
        printf("start failed %d\r\n", ret);
    }

    transport->close();

    return 0;
}

int32_t testUpgread(){
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

int32_t testRollback(){
    boost::shared_ptr<TSocket> socket(new TSocket("127.0.0.1", 5288));
    boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
    boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));

    CoreClient client(protocol);
    transport->open();
    // Notify core to update config
    int32_t ret = client.RollBack("sg_agent", "3.13", 1, 2);
    if(ret != 0) {
        printf("RollBack failed %d\r\n", ret);
    }

    transport->close();

    return 0;
}

int32_t testRestart_bad(){
    boost::shared_ptr<TSocket> socket(new TSocket("127.0.0.1", 5288));
    boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
    boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));

    CoreClient client(protocol);
    transport->open();
    // Notify core to start plugin
    int32_t ret = client.ReStart("sg_agent_bad", 1, 2);
    if(ret != 0) {
        printf("restart_bad failed %d", ret);
    }

    transport->close();

    return 0;
}

int32_t testStop_bad(){
    boost::shared_ptr<TSocket> socket(new TSocket("127.0.0.1", 5288));
    boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
    boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));

    CoreClient client(protocol);
    transport->open();
    // Notify core to stop plugin
    int32_t ret = client.Stop("sg_agent_bad", 1, 2);
    if(ret != 0) {
        printf("stop_bad failed %d\r\n", ret);
    }

    transport->close();

    return 0;
}

int32_t testStart_bad(){
    boost::shared_ptr<TSocket> socket(new TSocket("127.0.0.1", 5288));
    boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
    boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));

    CoreClient client(protocol);
    transport->open();
    // Notify core to start plugin
    int32_t ret = client.Start("sg_agent_bad", 1, 2);
    if(ret != 0) {
        printf("start_bad failed %d\r\n", ret);
    }

    transport->close();

    return 0;
}

int32_t testUpgread_bad(){
    boost::shared_ptr<TSocket> socket(new TSocket("127.0.0.1", 5288));
    boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
    boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));

    CoreClient client(protocol);
    transport->open();
    // Notify core to update config
    int32_t ret = client.Upgrade("sg_agent_bad", "3.15", 1, 2);
    if(ret != 0) {
        printf("Upgrade_bad failed %d\r\n", ret);
    }
    transport->close();

    return 0;
}

int32_t testRollback_bad(){
    boost::shared_ptr<TSocket> socket(new TSocket("127.0.0.1", 5288));
    boost::shared_ptr<TTransport> transport(new TFramedTransport(socket));
    boost::shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));

    CoreClient client(protocol);
    transport->open();
    // Notify core to update config
    int32_t ret = client.RollBack("sg_agent_bad", "3.13", 1, 2);
    if(ret != 0) {
        printf("RollBack_bad failed %d\r\n", ret);
    }

    transport->close();

    return 0;
}

int main() {
    testRestart();
    testStop();
    testStart();
    testUpgread();
    testRollback();

    testRestart_bad();
    testStop_bad();
    testStart_bad();
    testUpgread_bad();
    testRollback_bad();

    return 0;
}
