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
#include "../DyupsDataManager.h"

using namespace std;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;
using namespace inf::hlb;


void testIsVectorSGServiceDifference() {
    SgAgentClientCollector sgAgentCli;
    int bizCode = 1;
    string remoteAppkey = "com.sankuai.pay.bizcore";

    std::vector< SGService> originHttpServiceList;
    sgAgentCli.getHttpServiceList( originHttpServiceList, "fakeAppkey", remoteAppkey);
    if (originHttpServiceList.size() <= 0) {
        cout<<"[ERROR] cannot get HttpServiceList\n";
        return;
    }
    cout<< DyupsDataManager::instance().printSGServiceVector( "originHttpServiceList", originHttpServiceList);
    int pos = originHttpServiceList.size()/2 ;

    //=========== TEST 1 ip 发生变化 ==================================================
    std::vector< SGService> test1_vec( originHttpServiceList);
    test1_vec.at(pos).__set_ip("192.3.250.38");
    cout<< DyupsDataManager::instance().printSGServiceVector( "test1_vec", test1_vec);
    bool isDiff1 = DyupsDataManager::instance().isVectorSGServiceDifference( originHttpServiceList, test1_vec);
    cout <<"\n [TEST 1] change ip. \t"<< isDiff1<<"\n";

    //=========== TEST 2 port 发生变化 ==================================================
    std::vector< SGService> test2_vec( originHttpServiceList);
    test2_vec.at(pos).__set_port(98765);
    cout<< DyupsDataManager::instance().printSGServiceVector( "test2_vec", test2_vec);
    bool isDiff2 = DyupsDataManager::instance().isVectorSGServiceDifference( originHttpServiceList, test2_vec);
    cout <<"\n [TEST 2] change port. \t"<< isDiff2 <<"\n";

    //=========== TEST 3 weight 发生变化 ==================================================
    std::vector< SGService> test3_vec( originHttpServiceList);
    test2_vec.at(pos).__set_weight( 30);
    cout<< DyupsDataManager::instance().printSGServiceVector( "test3_vec", test3_vec);
    bool isDiff3 = DyupsDataManager::instance().isVectorSGServiceDifference( originHttpServiceList, test3_vec);
    cout <<"\n [TEST 3] change weight. \t"<< isDiff3 <<"\n";

    //=========== TEST 4 不变 ==================================================
    std::vector< SGService> test4_vec( originHttpServiceList);
    bool isDiff4 = DyupsDataManager::instance().isVectorSGServiceDifference( originHttpServiceList, test4_vec);
    cout <<"\n [TEST 4] no change. \t"<< isDiff4 <<"\n";

}


void testGetHttpPropertiesDiff() {
    sleep(5);
    SgAgentClientCollector sgAgentCli;
    int bizCode = 1;
    string remoteAppkey = "com.sankuai.inf.jinluTestHTTP";

    map< string, vector<SGService> > newHttpServiceListMap;
    sgAgentCli.getHttpServiceListByBusinessLine( newHttpServiceListMap, bizCode);
    if (newHttpServiceListMap.size() <= 0) {
        cout<<"[ERROR] cannot get getHttpServiceListByBusinessLine\n";
        return;
    }

    //=========== TEST 1 delete appkey ==================================================
    cout <<" 3\n";
    map< string, vector<SGService> > diffHttpServiceListMap_1;
    cout <<" 4\n";
    newHttpServiceListMap.erase( remoteAppkey);
    cout <<" 5\n";
    DyupsDataManager::instance().getHttpServiceDiff( bizCode, newHttpServiceListMap, diffHttpServiceListMap_1);
    cout << "\n=========== TEST 1 delete appkey =============\n";
    cout << DyupsDataManager::instance().printSGServiceMap( "DELETE APPKEY", diffHttpServiceListMap_1) <<"\n";
    cout <<" 6\n";

    //=========== TEST 2 add new appkey ==================================================
    map< string, vector<SGService> > diffHttpServiceListMap_2;
    vector<SGService> emptyVec;
    newHttpServiceListMap[remoteAppkey+".hahaha"] = emptyVec;
    DyupsDataManager::instance().getHttpServiceDiff( bizCode, newHttpServiceListMap, diffHttpServiceListMap_2);
    cout << "\n=========== TEST 2 add new appkey =============\n";
    cout << DyupsDataManager::instance().printSGServiceMap( "ADD APPKEY", diffHttpServiceListMap_2) <<"\n";
}


void *DyupsDataUpdater(void * args) {
    DyupsDataManager::instance().DyupsDataPullTimer();
}

int main(int argc, char** argv) {
    log4cplus::PropertyConfigurator::doConfigure(LOG4CPLUS_TEXT("log4cplus.conf"));
    int line = 0;
    if(0 != (line = HlbConfig::instance().initialization("Hlb.xml"))) {
        cout<< "HlbConfig Init error, at:" << line << " line!";
        return -1;
    }
    cout<<"\n[appkey] "<< HlbConfig::instance().m_hlbManagerAppkey  <<"\n";
    if(! DyupsDataManager::instance().initialization() ) {
        cout<< "DyupsDataManager initialization ERROR\n";
        return -1;
    }

    pthread_t updatetid;
    pthread_create( &updatetid, NULL, DyupsDataUpdater, NULL);

    testGetHttpPropertiesDiff();
    //testIsVectorSGServiceDifference();

    pthread_join(updatetid, NULL);
    return 0;
}
