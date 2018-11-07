#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include <time.h>
#include "interface_base.h"

class SgAgentRegister: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            start_time_ = time(NULL);
            sg_agent_handler_.init(appkey_s, ip_, port_);

            oservice.appkey = "com.sankuai.octo.mnscheck";
            cout << "appkey = " << oservice.appkey << endl;
            oservice.ip = "10.4.245.3";
            oservice.port = 5266;
            oservice.weight = 11;
            oservice.role = 0;
            oservice.envir = 2;
            oservice.fweight = 11.0;
            oservice.lastUpdateTime = start_time_;
            oservice.status = 4;
            oservice.heartbeatSupport = 3;
        }

        virtual void TearDown()
        {
            const time_t end_time = time(NULL);
            EXPECT_TRUE(end_time - start_time_ <= 6) << "register testcase took too long.";
        }

        SGAgentHandler sg_agent_handler_;
        SGService oservice;
};

TEST_F(SgAgentRegister, old_RegistThriftNode)
{
    oservice.serverType = 0;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));
};

TEST_F(SgAgentRegister, old_UnRegistThriftNode)
{
    oservice.serverType = 0;
    EXPECT_EQ(0, sg_agent_handler_.unRegisterService(oservice));
};

TEST_F(SgAgentRegister, new_RegistThriftNode)
{
    SGService tmp_service = oservice;
    tmp_service.protocol = "thrift";
    tmp_service.version = "mtthrift-v1.7.3";
    EXPECT_EQ(0, sg_agent_handler_.registerService(tmp_service));
};

TEST_F(SgAgentRegister, new_UnRegistThriftNode)
{
    SGService tmpSvr = oservice;
    tmpSvr.protocol = "thrift";
    tmpSvr.version = "notworker";
    EXPECT_EQ(0, sg_agent_handler_.unRegisterService(tmpSvr));
};

TEST_F(SgAgentRegister, old_RegistHttpNode)
{
    oservice.serverType = 1;
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));
};

TEST_F(SgAgentRegister, old_UnRegistHttpNode)
{
    oservice.serverType = 1;
    EXPECT_EQ(0, sg_agent_handler_.unRegisterService(oservice));
};

TEST_F(SgAgentRegister, new_RegistHttpNode)
{
    oservice.protocol = "http";
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));
};

TEST_F(SgAgentRegister, new_UnRegistHttpNode)
{
    oservice.protocol = "http";
    EXPECT_EQ(0, sg_agent_handler_.unRegisterService(oservice));
};

TEST_F(SgAgentRegister, RegistTairNode)
{
    oservice.protocol = "tair";
    EXPECT_EQ(0, sg_agent_handler_.registerService(oservice));
};

TEST_F(SgAgentRegister, UnRegistTairNode)
{
    oservice.protocol = "tair";
    EXPECT_EQ(0, sg_agent_handler_.unRegisterService(oservice));
};

TEST_F(SgAgentRegister, RegistError)
{
    oservice.serverType = 4;
    EXPECT_GT(0, sg_agent_handler_.registerService(oservice));
};

TEST_F(SgAgentRegister, UnRegistError)
{
    oservice.serverType = 4;
    EXPECT_GT(0, sg_agent_handler_.unRegisterService(oservice));
};
