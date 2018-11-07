#include <iostream>
#include <stdio.h>
#include <unistd.h>
#include <sys/time.h>

#include <protocol/TBinaryProtocol.h>
#include <transport/TSocket.h>
#include <transport/TTransportUtils.h>

#include "../hlb_gen_cpp/SGAgent.h"
#include "../utils/log4cplus.h"
#include "../Config.h"
#include "../SgAgentClient.h"
#include "../MNSCacheClient.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace inf::hlb;

void singleThreadTest() {
    MNSCacheClientCollector mnscCli;
    
    std::cout<<"\n==================== getHlbUpstream =======================\n";
    string nginx_type = "web";
    string idc_type = "idc-dx";
    std::map<std::string, std::string> upstreams;
    mnscCli.getHlbUpstream( upstreams, nginx_type, idc_type);
    int count=0;
    for (map< string, string >::const_iterator it2 = upstreams.begin();
         it2 != upstreams.end() ; ++it2) {
        cout<<"\n["<< ++count <<"] "<<it2->first<<" # "<<it2->second;
    }
    cout <<"\n";
}

void multiThreadTest() {
    ;
}

int main(int argc, char** argv) {
    log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT("log4cplus.conf"));
    int line = 0;
    if(0 != (line = HlbConfig::instance().initialization("Hlb.xml"))) {
        cout<< "HlbConfig Init error, at:" << line << " line!";
        return -1;
    }
    cout<<"\n[appkey] "<< HlbConfig::instance().m_hlbManagerAppkey  <<"\n";

    singleThreadTest();
    //multiThreadTest();
    return 0;
}
