#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"

class SgSelfCheck: public InterfaceBase
{
protected:
    virtual void SetUp()
    {
        start_time_ = time(NULL);
        sg_agent_handler_.init(appkey_s, ip_, port_);
    }

    virtual void TearDown()
    {
        sg_agent_handler_.deinit();
        const time_t end_time = time(NULL);
        EXPECT_TRUE(end_time - start_time_ <= 1) << "The test took too long.";
    }

    SGAgentHandler sg_agent_handler_;
    string appkey_;
};

TEST_F(SgSelfCheck, GetDataForZabbix)
{
};
