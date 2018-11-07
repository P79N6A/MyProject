#ifndef _TEST_SG_WORKER_H_
#define _TEST_SG_WORKER_H_

#include <stdio.h>
#include <unistd.h>
#include <sys/time.h>
#include <string.h>
#include <iostream>
#include <vector>
#include <protocol/TBinaryProtocol.h>
#include <transport/TSocket.h>
#include <transport/TTransportUtils.h>

#include "SGAgentWorker.h"
#include "sgagent_service_types.h"
#include "sgagent_common_types.h"
#include "quota_common_types.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

using namespace boost;

class SGWorkerHandler
{
    public:
        int init(const std::string &ip, int port);
        int deinit();
        SGAgentWorkerClient *client_;
        
    private:
        int checkConnection();
        int closeConnection();
        

    private:
        string ip_;
        int port_;
        shared_ptr<TSocket> socket_;
        shared_ptr<TTransport> transport_;
        shared_ptr<TProtocol> protocol_;
};
#endif
