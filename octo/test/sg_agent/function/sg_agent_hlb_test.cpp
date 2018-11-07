#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"
#include "common_define.h"

class SgAgentHlb: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
            sg_agent_handler_.init(appkey_s, ip_, port_);

        }

        virtual void TearDown()
        {
            const time_t end_time = time(NULL);
            EXPECT_TRUE(end_time - start_time_ <= 1) << "SgAgentTimeout testcase took too long.";
        }

        SGAgentHandler sg_agent_handler_;
        SGService oservice;
};

TEST_F(SgAgentHlb, RegistHttp)
{
    oservice.appkey = appkey_s;
    oservice.ip = ip_;
    oservice.port = 5266;
    oservice.weight = 10;
    oservice.status = 2;
    oservice.role = 0;
    oservice.envir = 2;
    oservice.fweight = 0.10;

    oservice.serverType = 1;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));

    usleep(50000);
};

TEST_F(SgAgentHlb, GetHttpServiceList) {
  vector<SGService> serviceList;
  bool is_exist = false;
  for (int i = 0; i < retrytimes; ++i) {
    sg_agent_handler_.client_->getHttpServiceList(serviceList, appkey_s, appkey_s); 
    is_exist = false;
    for(std::vector<SGService>::iterator iter = serviceList.begin(); serviceList.end() != iter; ++iter){
      if (ip_ == iter->ip && 5266 == iter->port) {
	is_exist = true;
        break;
      }
    }
  }
  EXPECT_TRUE(is_exist);
};
