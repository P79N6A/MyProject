#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"

class SgAgentGetEnv: public InterfaceBase
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


TEST_F(SgAgentGetEnv, getEnv)
{
    int ret = sg_agent_handler_.client_->getEnv();
    cout << "getEnv(): " << ret << endl;
    EXPECT_EQ(3, ret);
};

