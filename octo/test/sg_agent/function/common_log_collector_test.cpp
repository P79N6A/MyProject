#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"

class SgLogInterface: public InterfaceBase
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
            cost_time_ = end_time - start_time_;
            if(cost_time_>0.05){
							std::cout<<"the cost time >0.05"<<std::cout<<endl;
							EXPECT_FALSE(cost_time_ <= 0.05) << "uploadCommonLog took too long.";
						}else{
							std::cout<<"the cost time <0.05"<<std::cout<<endl;
							EXPECT_TRUE(cost_time_ <= 0.05) << "uploadCommonLog took too long.";
						}
        }

        time_t cost_time_;
        SGAgentHandler sg_agent_handler_;
};

TEST_F(SgLogInterface, SGLogTest)
{
    SGLog oLog;
    string content = "com.sankuai.inf.x";
    oLog.appkey = content;
    oLog.content = "2121";
    oLog.level = 1;
    for(int i = 0; i < 10; ++i)
    {
        EXPECT_EQ(0, sg_agent_handler_.uploadLog(oLog)); 
    }
}

TEST_F(SgLogInterface, CommonLogTest)
{
    CommonLog oCommonLog;
    oCommonLog.cmd = 7;
    string content = "chenxin123";
    oCommonLog.content = content;
    for(int i = 0; i < 10; ++i)
    {
        EXPECT_EQ(0, sg_agent_handler_.client_->uploadCommonLog(oCommonLog)); 
    }
}

TEST_F(SgLogInterface, ModuleInvokeTest)
{
    SGModuleInvokeInfo oModuleInfo;
    oModuleInfo.start = start_time_;
    oModuleInfo.cost = cost_time_;
    oModuleInfo.type = 1;
    for(int i = 0; i < 10; ++i)
    {
        EXPECT_EQ(0, sg_agent_handler_.uploadModuleInvoke(oModuleInfo)); 
    }
}


TEST_F(SgLogInterface, CombineTest)
{
    SGLog oLog;
    string content = "com.sankuai.inf.x";
    oLog.content = content;
    oLog.level = 1;

    CommonLog oCommonLog;
    oCommonLog.cmd = 1;
    oCommonLog.content = content;

    SGModuleInvokeInfo oModuleInfo;
    oModuleInfo.start = start_time_;
    oModuleInfo.cost = cost_time_;
    oModuleInfo.type = 1;

    for(int i = 0; i < 10; ++i)
    {
        EXPECT_EQ(0, sg_agent_handler_.client_->uploadModuleInvoke(oModuleInfo)); 
        EXPECT_EQ(0, sg_agent_handler_.client_->uploadLog(oLog)); 
        EXPECT_EQ(0, sg_agent_handler_.client_->uploadCommonLog(oCommonLog)); 
    }
}
