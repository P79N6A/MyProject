/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#include <stdio.h>
#include <unistd.h>
#include <sys/time.h>
#include <ctime>
#include <protocol/TBinaryProtocol.h>
#include <transport/TSocket.h>
#include <transport/TTransportUtils.h>

#include "gen-cpp/SGAgent.h"
#include "gen-cpp/sgagent_types.h"

#include <fstream>

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

using namespace boost;

int main(int argc, char** argv) {
    //sg_agent ip and port
    shared_ptr<TTransport> socket(new TSocket("127.0.0.1", 5266));
    shared_ptr<TTransport> transport(new TFramedTransport(socket));
    shared_ptr<TProtocol> protocol(new TBinaryProtocol(transport));
    SGAgentClient client(protocol);

    ofstream file;
    string localAppKey = "abc";
    string remoteAppKey = "com.sankuai.inf.sg_agent";

    try {
        transport->open();

        file.open("./sg_agent_monitor.txt", std::ofstream::app);

        int retry_time = 0;
        do
        {
            time_t start_time = time(NULL);
            vector<SGService> serviceList;
            client.getServiceList(serviceList, localAppKey, remoteAppKey);
            time_t end_time = time(NULL);

            if(end_time - start_time > 1)
            {
                file << "Sg_Agent Service timeout, cost: " << end_time - start_time << " seconds;" << ctime(&end_time) << endl;
                ++retry_time;
            }
            else
            {
                file << "Sg_Agent Service OK!" << endl; 
                break;
            }
        }while(retry_time < 3);
        
        if(retry_time == 3)
        {
            file << "restart the sg_agent" << endl; 
            system("/opt/meituan/apps/sg_agent/svc.sh");
        }

        transport->close();
        file.close();
    }  catch (TException &tx) {
        time_t cur = time(NULL);
        file << "ERROR: " << tx.what() << ctime(&cur) << endl;
    }
}
