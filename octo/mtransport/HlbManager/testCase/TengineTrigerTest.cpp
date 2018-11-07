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
#include "../TengineTriger.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace inf::hlb;


void testDyupsDataChanged() {
    SgAgentClientCollector sgAgentCli;
    int bizCode = 1;
    string remoteAppkey = "com.sankuai.inf.jinluTestHTTP";

    std::vector< SGService> originHttpServiceList;
    sgAgentCli.getHttpServiceList( originHttpServiceList, "fakeAppkey", remoteAppkey);
    if (originHttpServiceList.size() <= 0) {
        cout<<"[ERROR] cannot get HttpServiceList\n";
        return;
    }

    map< string, vector< SGService> > diffHttpServiceListMap;
    map< string, HttpProperties> diffPropertiesMap;
    diffHttpServiceListMap[remoteAppkey] = originHttpServiceList;

    HttpProperties prop;
    prop["health_check"] = "check interval=3000 rise=2 fall=5 timeout=1000 type=http; check_http_send \"GET / HTTP/1.0\\r\\n\\r\\n\";"; 
    diffPropertiesMap[remoteAppkey] = prop;
    
    TengineTriger::instance().dyupsDataChanged( diffHttpServiceListMap, diffPropertiesMap);
}

int main(int argc, char** argv) {
    log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT("log4cplus.conf"));
    int line = 0;
    if(0 != (line = HlbConfig::instance().initialization("Hlb.xml"))) {
        cout<< "HlbConfig Init error, at:" << line << " line!";
        return -1;
    }
    cout<<"\n[appkey] "<< HlbConfig::instance().m_hlbManagerAppkey  <<"\n";

    testDyupsDataChanged();
    return 0;
}
