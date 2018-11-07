#include "interface_base.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include <boost/property_tree/ptree.hpp>
#include <boost/optional/optional.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <stdio.h>
using boost::property_tree::ptree;

const int NUM = 2;

std::string keys [12]= {"com.meituan.inf.chenxin11", "com.sankuai.cos.mtconfig",
    "com.sankuai.inf.mcc_test", "com.sankuai.hotel.dealing.dev",
    "com.sankuai.inf.data.statistic", "com.sankuai.inf.logCollector",
    "com.sankuai.inf.msgp", "com.sankuai.inf.sg_agent",
    "com.sankuai.pay.account", "com.sankuai.pay.config",
    "com.sankuai.waimai.openmessagecenter", "com.sankuai.waimai.rabbitmqplugin"};

class SgMtConfigMtAppkey: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
           start_time_ = time(NULL);

           conf.env = "stage";
           conf.path = "/";

           sg_agent_handler_.init(appkey_s, ip_, port_);
        }

        virtual void TearDown()
        {
            const time_t end_time = time(NULL);
        }

        SGAgentHandler sg_agent_handler_;
        time_t start_time_;
        proc_conf_param_t conf;
};

TEST_F(SgMtConfigMtAppkey, Get_multi_appkey) {
  for(int i = 0; i < NUM; ++i) {
    conf.appkey = keys[i];
    std::string result = "";
    sg_agent_handler_.client_->getConfig(result, conf); 
    EXPECT_FALSE(result.empty()) << "fail! appkey: " << keys[i];
    sleep(1);
  }
}

TEST_F(SgMtConfigMtAppkey, Get_multi_key_value)
{
    conf.appkey = appkey_s;
    int ret = 0;
    std::string result = "";
    sg_agent_handler_.client_->getConfig(result, conf); 

    ptree pt;

    std::stringstream ss(result);
    read_json(ss, pt);

    boost::optional<ptree&> child;
    child = pt.get_child_optional("ret");
    if (!child)
    {
        cout << "json is not have ret" << endl;
        return;
    }

    ret = pt.get<int>("ret");
    EXPECT_EQ(0, ret);

    if (ret != 0)
    {
        child = pt.get_child_optional("msg");
        std::string msg = pt.get<string>("msg");
        EXPECT_STREQ(msg.c_str(), "success");
    }


    child = pt.get_child_optional("data");
    if (!child)
    {
        EXPECT_EQ(0, 1);
    }

    ptree data = pt.get_child("data");

    std::stringstream ss_key;
    for(int i = 0; i < 7; ++i) 
    {
        ss_key << i;
        EXPECT_STREQ(ss_key.str().c_str(), data.get<std::string>(ss_key.str()).c_str());
        ss_key.str("");
    }
}


