//
// Created by huixiangbo on 2017/10/12.
//
#include <string>
#include <vector>
#include <gtest/gtest.h>
#include "../core/util/tinyxml2.h"

#define private public
#define protected public
#include "../core/host_manager.h"
#undef private
#undef protected


using namespace std;
using namespace cplugin;
using namespace tinyxml2;
using testing::Types;

typedef boost::shared_ptr< TaskContext<RequestParams_t, int32_t> > OperatorContextPtr;

void ResetConfigCplugin(){
    string file_name = "cplugin.xml";
    string config = "<CPluginConf>\n"
                    "<!-- downloader host name -->\n"
                    "<HostOffLine>cplugin.inf.test.sankuai.com</HostOffLine>\n"
                    "<HostOnLine>cplugin.inf.vip.sankuai.com</HostOnLine>\n"
                    "<SentinelUpdateTime>1800</SentinelUpdateTime>\n"
                    "<ShortConn>0</ShortConn>\n"
                     "<SwitchForServer>1</SwitchForServer>\n"
                     "<DownloadFailedKeepTimes>5</DownloadFailedKeepTimes>\n"
                     "<StartFailedIntervalTime>86400</StartFailedIntervalTime>\n"
                     "<OperatorTimeOut>5000000</OperatorTimeOut>\n"
                     "<AgentDebugMode>0</AgentDebugMode>\n"
                      "</CPluginConf>";
    ofstream config_file;
    config_file.open(file_name.c_str(), ios::out );
    config_file << config ;
    config_file.close();

}

class HostManager_Test : public testing::Test {
public:
    HostManager_Test() {
        p_host_manager_ = new HostManager();
          //p_host_manager_->Init();
    }
    ~HostManager_Test(){
        ResetConfigCplugin();
        if( p_host_manager_ ){
            delete p_host_manager_;
        }
    }



    virtual void SetUp() {

    }
    virtual void TearDown() {

    }


public:
    HostManager *p_host_manager_;
};

TEST_F(HostManager_Test, HandleDebugModule) {

string file_name = "cplugin.xml";
string config = "<CPluginConf>\n"
        "<!-- downloader host name -->\n"
        "<HostOffLine>cplugin.inf.test.sankuai.com</HostOffLine>\n"
        "<HostOnLine>cplugin.inf.vip.sankuai.com</HostOnLine>\n"
        "<SentinelUpdateTime>1800</SentinelUpdateTime>\n"
        "<ShortConn>0</ShortConn>\n"
        "<SwitchForServer>1</SwitchForServer>\n"
        "<DownloadFailedKeepTimes>5</DownloadFailedKeepTimes>\n"
        "<StartFailedIntervalTime>86400</StartFailedIntervalTime>\n"
        "<OperatorTimeOut>5000000</OperatorTimeOut>\n"
        "<AgentDebugMode>1</AgentDebugMode>\n"
        "</CPluginConf>";
ofstream config_file;
config_file.open(file_name.c_str(), ios::out );
config_file << config ;
config_file.close();

p_host_manager_->InitCpluginConfig();

EXPECT_TRUE( p_host_manager_->OnCheckFileIntegrity("cr_agent","2ecda0df0da62392df0696a05c7d0352") );

}


TEST_F(HostManager_Test, HandleMoniter) {
std::map<std::string, std::string>  _return;
std::vector<std::string>  agents;

 agents.push_back("cr_agent");
 p_host_manager_->GetMonitorInfos(_return, agents);


 EXPECT_TRUE( _return.size() == 1 );
}



