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

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace inf::hlb;

void singleThreadTest() {
    SgAgentClientCollector sgAgentCli;
    int bizCode = 1;

    std::cout<<"\n==================== getAppKeyListByBusinessLine =======================\n";
    vector< string> appKeyList;
    sgAgentCli.getAppKeyListByBusinessLine( appKeyList, bizCode);
    for (vector< string>::const_iterator it1 = appKeyList.begin();
         it1 != appKeyList.end() ; ++it1) {
        cout<<"  "<<*it1;
    }
    cout <<"\n TOTAL APPKET LIST SIZE = "<< appKeyList.size()<<"\n";


    std::cout<<"\n==================== getAppKeyListByBusinessLine =======================\n";
    std::map< std::string, std::vector< SGService> > httpServiceListMap;
    sgAgentCli.getHttpServiceListByBusinessLine( httpServiceListMap, 1);
    int count=0;
    for (map< string, vector< SGService> >::const_iterator it2 = httpServiceListMap.begin();
         it2 != httpServiceListMap.end() ; ++it2) {
        cout<<"\n["<< ++count <<"] "<<it2->first<<" # ";
        for (vector< SGService>::const_iterator it3 = (it2->second).begin();
             it3 != (it2->second).end() ; ++it3) {
            cout<<" - "<< (*it3).ip <<":"<< (*it3).port;
        }
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
