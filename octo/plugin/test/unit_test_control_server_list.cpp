//
// Created by huixiangbo on 2017/10/12.
//
#include <string>
#include <vector>
#include <gtest/gtest.h>
#include "../core/util/tinyxml2.h"

#define private public
#define protected public
#include "../core/controlServerManager.h"
#include "../core/host_manager.h"
#undef private
#undef protected


using namespace std;
using namespace cplugin;
using namespace tinyxml2;
using testing::Types;


void ControlManager_Reset(){
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

class ControlManagerTest : public testing::Test {
public:
    ControlManagerTest() {
        p_host_manager_ = new HostManager();
        p_host_manager_->Init();

        p_ControlManager_ = new ControlManager(p_host_manager_);
    }
    ~ControlManagerTest(){
        ControlManager_Reset();
        if( p_host_manager_ ){
            delete p_host_manager_;
        }

        if( p_ControlManager_ ){
            delete p_ControlManager_;
        }
    }



    virtual void SetUp() {

    }
    virtual void TearDown() {

    }


public:
    HostManager *p_host_manager_;
    ControlManager *p_ControlManager_;
};

TEST_F(ControlManagerTest, Handle_LIST_EQUAL) {

 vector<cplugin_sgagent::SGService> vec_sgservice_src;
 vector<cplugin_sgagent::SGService> vec_sgservice_old;
 vector<cplugin_sgagent::SGService> vec_sgservice_add;
 vector<cplugin_sgagent::SGService> vec_sgservice_remove;
 for(int i= 0; i< 5; i++){
   cplugin_sgagent::SGService sg;
   sg.ip = "127.0.0.1";
   sg.port = 1000+i;
   sg.status = 2;
   vec_sgservice_src.push_back(sg);
}

p_ControlManager_->update(vec_sgservice_src);

for(int i= 0; i< 5; i++){
    cplugin_sgagent::SGService sg;
    sg.ip = "127.0.0.1";
    sg.port = 1000+i;
    sg.status = 2;
    vec_sgservice_old.push_back(sg);
}


UpdateType type = p_ControlManager_->getType(vec_sgservice_old , vec_sgservice_src, vec_sgservice_add, vec_sgservice_remove);

EXPECT_TRUE( type == LIST_EQUAL );

}


TEST_F(ControlManagerTest, Handle_LIST_INCREASE) {

vector<cplugin_sgagent::SGService> vec_sgservice_src;
vector<cplugin_sgagent::SGService> vec_sgservice_old;
vector<cplugin_sgagent::SGService> vec_sgservice_add;
vector<cplugin_sgagent::SGService> vec_sgservice_remove;
for(int i= 0; i< 5; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 1000+i;
sg.status = 2;
vec_sgservice_src.push_back(sg);
}

p_ControlManager_->update(vec_sgservice_src);

for(int i= 0; i< 6; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 1000+i;
sg.status = 2;
vec_sgservice_old.push_back(sg);
}


UpdateType type = p_ControlManager_->getType(vec_sgservice_old , vec_sgservice_src, vec_sgservice_add, vec_sgservice_remove);

EXPECT_TRUE( type == LIST_INCREASE );

}

TEST_F(ControlManagerTest, Handle_LIST_DECREASE) {

vector<cplugin_sgagent::SGService> vec_sgservice_src;
vector<cplugin_sgagent::SGService> vec_sgservice_old;
vector<cplugin_sgagent::SGService> vec_sgservice_add;
vector<cplugin_sgagent::SGService> vec_sgservice_remove;
for(int i= 0; i< 5; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 1000+i;
sg.status = 2;
vec_sgservice_src.push_back(sg);
}

p_ControlManager_->update(vec_sgservice_src);

for(int i= 0; i< 4; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 1000+i;
sg.status = 2;
vec_sgservice_old.push_back(sg);
}


UpdateType type = p_ControlManager_->getType(vec_sgservice_old , vec_sgservice_src, vec_sgservice_add, vec_sgservice_remove);

EXPECT_TRUE( type == LIST_DECREASE );

}

TEST_F(ControlManagerTest, Handle_LIST_DECREASE_ON) {

vector<cplugin_sgagent::SGService> vec_sgservice_src;
vector<cplugin_sgagent::SGService> vec_sgservice_old;
vector<cplugin_sgagent::SGService> vec_sgservice_add;


vector<cplugin_sgagent::SGService> vec_sgservice_remove;
for(int i= 0; i< 5; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 1000+i;
sg.status = 2;
vec_sgservice_src.push_back(sg);
}

p_ControlManager_->update(vec_sgservice_src);

p_ControlManager_->current_ip_ = "127.0.0.1";
p_ControlManager_->current_port_ = 1004;

for(int i= 0; i< 4; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 1000+i;
sg.status = 2;
vec_sgservice_old.push_back(sg);
}


UpdateType type = p_ControlManager_->getType(vec_sgservice_old , vec_sgservice_src, vec_sgservice_add, vec_sgservice_remove);

EXPECT_TRUE( type == LIST_DECREASE_ON );

}

TEST_F(ControlManagerTest, Handle_LIST_BOTH) {

vector<cplugin_sgagent::SGService> vec_sgservice_src;
vector<cplugin_sgagent::SGService> vec_sgservice_old;
vector<cplugin_sgagent::SGService> vec_sgservice_add;


vector<cplugin_sgagent::SGService> vec_sgservice_remove;
for(int i= 0; i< 5; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 1000+i;
sg.status = 2;
vec_sgservice_src.push_back(sg);
}

p_ControlManager_->update(vec_sgservice_src);

p_ControlManager_->current_ip_ = "127.0.0.1";
p_ControlManager_->current_port_ = 1000;

for(int i= 0; i< 5; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 998+i;
sg.status = 2;
vec_sgservice_old.push_back(sg);
}


UpdateType type = p_ControlManager_->getType(vec_sgservice_old , vec_sgservice_src, vec_sgservice_add, vec_sgservice_remove);

EXPECT_TRUE( type == LIST_BOTH );

}

TEST_F(ControlManagerTest, Handle_LIST_BOTH_ON) {

vector<cplugin_sgagent::SGService> vec_sgservice_src;
vector<cplugin_sgagent::SGService> vec_sgservice_old;
vector<cplugin_sgagent::SGService> vec_sgservice_add;


vector<cplugin_sgagent::SGService> vec_sgservice_remove;
for(int i= 0; i< 5; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 1000+i;
sg.status = 2;
vec_sgservice_src.push_back(sg);
}

p_ControlManager_->update(vec_sgservice_src);

p_ControlManager_->current_ip_ = "127.0.0.1";
p_ControlManager_->current_port_ = 1004;

for(int i= 0; i< 5; i++){
cplugin_sgagent::SGService sg;
sg.ip = "127.0.0.1";
sg.port = 998+i;
sg.status = 2;
vec_sgservice_old.push_back(sg);
}


UpdateType type = p_ControlManager_->getType(vec_sgservice_old , vec_sgservice_src, vec_sgservice_add, vec_sgservice_remove);

EXPECT_TRUE( type == LIST_BOTH_ON );

}
