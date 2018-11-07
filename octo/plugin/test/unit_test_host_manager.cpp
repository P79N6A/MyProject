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

void ResetConfig(){
    string file_name = "config.xml";
    string config = "<Plugins>\n"
            "    <Plugin>\n"
            "        <Name>cr_agent</Name>\n"
            "        <Version>1.3.3</Version>\n"
            "    </Plugin>\n"
            "</Plugins>";
    ofstream config_file;
    config_file.open(file_name.c_str(), ios::out );
    config_file << config ;
    config_file.close();

}

class HostManagerTest : public testing::Test {
public:
    HostManagerTest() {
        p_host_manager_ = new HostManager();
        p_host_manager_->Init();
    }
    ~HostManagerTest(){
        ResetConfig();
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

TEST_F(HostManagerTest, HandleInit) {

   EXPECT_TRUE( p_host_manager_->strPluginMap_.size() == 1 );

}


TEST_F(HostManagerTest, HandleStop_TRUE) {
    std::string plugin_name = "cr_agent";
    int32_t plugin_id =  123456;
    int32_t task_id   =  654321;

    p_host_manager_->Stop( plugin_name, plugin_id, task_id) ;

    sleep(2);

    EXPECT_TRUE( p_host_manager_->strPluginMap_.size() == 1 );
    EXPECT_TRUE( p_host_manager_->strPluginMap_[plugin_name].statu_  == PLUGIN_STATU_STOPED );
}

TEST_F(HostManagerTest, HandleStop_FALSE) {
    std::string plugin_name = "cr_agent";
    int32_t plugin_id =  123456;
    int32_t task_id   =  654321;

    p_host_manager_->Stop( plugin_name, plugin_id, task_id) ;

    sleep(2);

    EXPECT_TRUE( p_host_manager_->strPluginMap_.size() == 1 );

    int stop_count = 0;
    StrPluginMap::iterator it = p_host_manager_->strPluginMap_.begin();
    for(; it != p_host_manager_->strPluginMap_.end(); it++){
        if(it->second.statu_ == PLUGIN_STATU_STOPED){
        stop_count++;
        }
    }

    EXPECT_TRUE( stop_count  == 1 );
}

TEST_F(HostManagerTest, HandleStart_True) {
    std::string plugin_name = "cr_agent";
    int32_t plugin_id =  123456;
    int32_t task_id   =  654321;

    p_host_manager_->Stop( plugin_name, plugin_id, task_id) ;

    sleep(1);
    EXPECT_TRUE( p_host_manager_->strPluginMap_.size() == 1 );
    EXPECT_TRUE( p_host_manager_->strPluginMap_[plugin_name].statu_  == PLUGIN_STATU_STOPED );

    p_host_manager_->Start( plugin_name, plugin_id, task_id) ;

    sleep(2);

    EXPECT_TRUE( p_host_manager_->strPluginMap_.size() == 1 );
    EXPECT_TRUE( p_host_manager_->strPluginMap_[plugin_name].statu_  == PLUGIN_STATU_RUNING );

}

TEST_F(HostManagerTest, HandleRestart_TRUE) {
    std::string plugin_name = "cr_agent";
    int32_t plugin_id =  123456;
    int32_t task_id   =  654321;

    pid_t pid = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;

    p_host_manager_->ReStart( plugin_name, plugin_id, task_id) ;

    sleep(2);

    EXPECT_TRUE( pid !=  p_host_manager_->strPluginMap_[plugin_name].plugin_pid_);

}

TEST_F(HostManagerTest, HandleStrartNew_TRUE) {
   std::string plugin_name = "sg_agent";
   std::string version =  "sg_agent_test";

   int32_t plugin_id =  123456;
   int32_t task_id   =  654321;



   p_host_manager_->StartNew( plugin_name, version, plugin_id, task_id) ;

   sleep(2);

   EXPECT_TRUE( p_host_manager_->strPluginMap_.size() == 2 );

}

TEST_F(HostManagerTest, HandleRemove_TRUE) {
    std::string plugin_name = "cr_agent";
    std::string version =  "1.3.3";

    int32_t plugin_id =  123456;
    int32_t task_id   =  654321;



    sleep(2);

    EXPECT_TRUE( p_host_manager_->strPluginMap_.size() == 1 );


    p_host_manager_->Remove( plugin_name, plugin_id, task_id) ;

    sleep(2);

    EXPECT_TRUE( p_host_manager_->strPluginMap_.size() == 0 );

}

TEST_F(HostManagerTest, HandleUpgrade_TRUE) {

    std::string plugin_name = "cr_agent";
    std::string version_new =  "1.5.5";
    int32_t plugin_id =  123456;
    int32_t task_id   =  654321;


    pid_t pid = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;


    p_host_manager_->Upgrade( plugin_name, version_new, plugin_id, task_id) ;

    sleep(2);

    EXPECT_TRUE( pid ==  p_host_manager_->strPluginMap_[plugin_name].plugin_pid_);

    EXPECT_TRUE( version_new == p_host_manager_->strPluginMap_[plugin_name].now_version_);

}

TEST_F(HostManagerTest, HandleRollBack_TRUE) {
    std::string plugin_name = "cr_agent";
    std::string version_old =  "1.5.5";
    int32_t plugin_id =  123456;
    int32_t task_id   =  654321;


    sleep(2);


    pid_t pid = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;


    p_host_manager_->RollBack( plugin_name, version_old, plugin_id, task_id) ;

    sleep(2);

    EXPECT_TRUE( pid ==  p_host_manager_->strPluginMap_[plugin_name].plugin_pid_);
    EXPECT_TRUE( version_old == p_host_manager_->strPluginMap_[plugin_name].now_version_);

}

TEST_F(HostManagerTest, GetPluginInfos) {
#if 0
p_host_manager_->GetRunningInfo();
std::map<std::string, TInfos>  _return;
p_host_manager_->GetPluginInfos( _return) ;

EXPECT_TRUE(_return.size() == 1);
EXPECT_TRUE(_return["cr_agent"].host_info.version == "1.3.3");
#endif
}


TEST_F(HostManagerTest, ResetPlugin_True) {
std::string plugin_name = "cr_agent";
std::string version_new =  "1.3.3";

pid_t pid = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;


p_host_manager_->ResetPlugin( pid) ;

pid = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;
bool is_ok = p_host_manager_->strPluginMap_[plugin_name].is_ok_;

EXPECT_TRUE(pid == 0 && is_ok == false);

}

TEST_F(HostManagerTest, ResetPlugin_False) {
std::string plugin_name = "cr_agent";
std::string version_new =  "1.3.3";

pid_t pid = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;


p_host_manager_->ResetPlugin( 123 ) ;

pid_t pid_new = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;
bool is_ok = p_host_manager_->strPluginMap_[plugin_name].is_ok_;

EXPECT_TRUE(pid == pid_new && is_ok == true);

}

TEST_F(HostManagerTest, StopAll) {
std::string plugin_name = "cr_agent";
std::string version_new =  "1.3.3";


p_host_manager_->StopAll(  ) ;

sleep(2);

for(StrPluginMap::iterator it = p_host_manager_->strPluginMap_.begin();
                           it != p_host_manager_->strPluginMap_.end(); it++){

     EXPECT_TRUE(it->second.is_ok_ == false && it->second.plugin_pid_ == 0 && it->second.statu_ == PLUGIN_STATU_STOPED);

}
}


TEST_F(HostManagerTest, BackendHandler_START_TRUE) {
std::string plugin_name = "cr_agent";
std::string version =  "1.3.3";

int32_t plugin_id =  123456;
int32_t task_id   =  654321;

RequestParams_t params_t(plugin_name, "",plugin_id, task_id , CPLUGIN_OPERATION_TYPE_START);

OperatorContextPtr context(
        new TaskContext<RequestParams_t, int32_t>(params_t));

p_host_manager_->BackendHandler( context ) ;

sleep(2);

pid_t pid = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;
EXPECT_TRUE(pid != 0);

}

TEST_F(HostManagerTest, BackendHandler_START_False) {
std::string plugin_name = "bad_agent";
std::string version =  "1.3.3";

int32_t plugin_id =  123456;
int32_t task_id   =  654321;

RequestParams_t params_t(plugin_name, "",plugin_id, task_id , CPLUGIN_OPERATION_TYPE_START);

OperatorContextPtr context(
        new TaskContext<RequestParams_t, int32_t>(params_t));

p_host_manager_->BackendHandler( context ) ;

sleep(2);

StrPluginMap::iterator it = p_host_manager_->strPluginMap_.find(plugin_name);

EXPECT_TRUE(it == p_host_manager_->strPluginMap_.end());

}

TEST_F(HostManagerTest, BackendHandler_REMOVE_True) {
std::string plugin_name = "cr_agent";
std::string version =  "1.3.3";

int32_t plugin_id =  123456;
int32_t task_id   =  654321;

int size_old = p_host_manager_->strPluginMap_.size();

RequestParams_t params_t(plugin_name, "",plugin_id, task_id , CPLUGIN_OPERATION_TYPE_REMOVE);

OperatorContextPtr context(
        new TaskContext<RequestParams_t, int32_t>(params_t));

p_host_manager_->BackendHandler( context ) ;

sleep(2);

int size_new = p_host_manager_->strPluginMap_.size();


EXPECT_TRUE(size_new == size_old - 1);

}

TEST_F(HostManagerTest, BackendHandler_REMOVE_False) {
std::string plugin_name = "cp_agent";
std::string version =  "1.3.3";

int32_t plugin_id =  123456;
int32_t task_id   =  654321;

int size_old = p_host_manager_->strPluginMap_.size();

RequestParams_t params_t(plugin_name, "",plugin_id, task_id , CPLUGIN_OPERATION_TYPE_REMOVE);

OperatorContextPtr context(
        new TaskContext<RequestParams_t, int32_t>(params_t));

p_host_manager_->BackendHandler( context ) ;

sleep(2);

int size_new = p_host_manager_->strPluginMap_.size();


EXPECT_TRUE(size_new == size_old);

}

TEST_F(HostManagerTest, BackendHandler_STOP_True) {
std::string plugin_name = "cr_agent";
std::string version =  "1.3.3";

int32_t plugin_id =  123456;
int32_t task_id   =  654321;

int size_old = p_host_manager_->strPluginMap_.size();

RequestParams_t params_t(plugin_name, "",plugin_id, task_id , CPLUGIN_OPERATION_TYPE_STOP);

OperatorContextPtr context(
        new TaskContext<RequestParams_t, int32_t>(params_t));

p_host_manager_->BackendHandler( context ) ;

sleep(2);

int size_new = p_host_manager_->strPluginMap_.size();


EXPECT_TRUE(size_new == size_old );
EXPECT_TRUE(p_host_manager_->strPluginMap_[plugin_name].statu_ == PLUGIN_STATU_STOPED );
}

TEST_F(HostManagerTest, BackendHandler_STOP_False) {
std::string plugin_name = "cp_agent";
std::string cr_plugin_name = "cr_agent";
std::string version =  "1.3.3";

int32_t plugin_id =  123456;
int32_t task_id   =  654321;

int size_old = p_host_manager_->strPluginMap_.size();

RequestParams_t params_t(plugin_name, "",plugin_id, task_id , CPLUGIN_OPERATION_TYPE_STOP);

OperatorContextPtr context(
        new TaskContext<RequestParams_t, int32_t>(params_t));

p_host_manager_->BackendHandler( context ) ;

sleep(2);

int size_new = p_host_manager_->strPluginMap_.size();


EXPECT_TRUE(size_new == size_old);
EXPECT_TRUE(p_host_manager_->strPluginMap_[cr_plugin_name].statu_ == PLUGIN_STATU_RUNING );
}

/*
   CPLUGIN_OPERATION_TYPE_START,
    CPLUGIN_OPERATION_TYPE_REMOVE,
    CPLUGIN_OPERATION_TYPE_STOP,
    CPLUGIN_OPERATION_TYPE_GETINFO,
    CPLUGIN_OPERATION_TYPE_RESTART,
    CPLUGIN_OPERATION_TYPE_UPGREAD,
    CPLUGIN_OPERATION_TYPE_ROLLBACK,
    CPLUGIN_OPERATION_TYPE_STARTNEW,

 */

TEST_F(HostManagerTest, BackendHandler_RESTART_True) {
std::string plugin_name = "cr_agent";
std::string version =  "1.3.3";

int32_t plugin_id =  123456;
int32_t task_id   =  654321;

pid_t pid_old  = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;

RequestParams_t params_t(plugin_name, "",plugin_id, task_id , CPLUGIN_OPERATION_TYPE_RESTART);

OperatorContextPtr context(
        new TaskContext<RequestParams_t, int32_t>(params_t));

p_host_manager_->BackendHandler( context ) ;

sleep(2);


pid_t pid_new  = p_host_manager_->strPluginMap_[plugin_name].plugin_pid_;

EXPECT_TRUE(pid_new != pid_old );
}

/*
TEST_F(HostManagerTest, CheckFileIntegrity_True) {
std::string plugin_name = "cr_agent";
std::string version =  "1.3.3";

bool ret = p_host_manager_->CheckFileIntegrityAdapter(plugin_name, version);


EXPECT_TRUE(ret);
}
*/
