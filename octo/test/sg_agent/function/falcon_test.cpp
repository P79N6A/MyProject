#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"

class SgAgentFalcon: public InterfaceBase
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
};


TEST_F(SgAgentFalcon, get)
{
    ZabbixSelfCheck ret;
    sg_agent_handler_.client_->getZabbixInfo(ret);
    cout << "bufferSize's size: " << ret.bufferSize.size() << endl;
    EXPECT_EQ(4,ret.bufferSize.size());
    cout << "reqStat's size: " << ret.reqStat.size() << endl;
    EXPECT_EQ(5,ret.reqStat.size());
};

