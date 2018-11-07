#include <limits.h>
#include "test_zk_client.h"
#include "test_sg_agent.h"
#include "interface_base.h"
#include <gtest/gtest.h>

class ZkMonoLogCollector: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
            appkey_ = "com.sankuai.inf.logCollector";
						std::cout<<"ip is"<<ip_<<",port_"<<std::cout<<endl;
						sg_agent_handler_.init(appkey_, ip_, port_);
            zk_operator_.init(appkey_, zkserver_);
        }

        virtual void TearDown()
        {
            sg_agent_handler_.deinit();
            zk_operator_.deinit();
        }

        SGAgentHandler sg_agent_handler_;
        ZkClientOperation zk_operator_;
        string appkey_;
};

TEST_F(ZkMonoLogCollector, logCollectorRestartTest)
{
    vector<SGService> serviceList;
    sg_agent_handler_.getServiceList(serviceList);
    if(serviceList.size()==3){
		   EXPECT_EQ(3, serviceList.size());
    }else if(serviceList.size()==4){
		   EXPECT_EQ(4, serviceList.size());
		}else{
		std::cout<<"the wrong serviceList size="<<serviceList.size()<<std::cout<<endl;
		}
    //重新注册模拟重启
    SGService sg_service;
    sg_service.appkey = appkey_;
    sg_service.ip = "192.168.3.163";
    sg_service.port = 8920;
    sg_service.status = 2;
    sg_service.weight = 10;
    //删除节点
    zk_operator_.deleteZNode(sg_service);
    sleep(1);
    EXPECT_EQ(0, sg_agent_handler_.registerService(sg_service));

    SGModuleInvokeInfo oModuleInfo;
    oModuleInfo.type = 1;

    for(int i = 0; i < 20; ++i)
    {
        EXPECT_EQ(0, sg_agent_handler_.uploadModuleInvoke(oModuleInfo)); 
    }

}
