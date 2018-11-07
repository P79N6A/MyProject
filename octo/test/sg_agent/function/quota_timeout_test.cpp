#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"
#include <time.h>

class SgQuota: public InterfaceBase
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
        EXPECT_TRUE(end_time - start_time_ <= 1) << "quota test took too long.";
    }


    SGAgentHandler sg_agent_handler_;
    time_t start_time_;
};

TEST_F(SgQuota, GetDegradeActions)
{
   vector<DegradeAction> actions;
   sg_agent_handler_.client_->getDegradeActions(actions, appkey_s, appkey_s);
	 cout<<"action size = "<<actions.size()<<endl;
	 EXPECT_EQ(0, actions.size());
   for (std::vector<DegradeAction>::iterator it = actions.begin() ; it != actions.end(); ++it)
   {
            cout << "id = " << it->id 
                 << "degradeRatio = " << it->degradeRatio << endl;
   }
};

TEST_F(SgQuota, empty_node)
{
   vector<DegradeAction> actions;
   sg_agent_handler_.client_->getDegradeActions(actions, "com.sankuai.inf.hulk.marina", "com.sankuai.inf.hulk.harbor"); 
   EXPECT_EQ(0, actions.size());
};
