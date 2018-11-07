#include "interface_base.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include <boost/property_tree/ptree.hpp>
#include <boost/optional/optional.hpp>
#include <boost/property_tree/json_parser.hpp>
using boost::property_tree::ptree;

class SgMtConfigTimeout: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
           appkey_1 = "com.sankuai.mns.backuptest";

           conf.appkey = appkey_1;
           conf.env = "prod";
           conf.path = "/";

           sg_agent_handler_.init(appkey_s, ip_, port_);
           start_time_ = time(NULL);
        }

        virtual void TearDown()
        {
        }

        std::string parseConfigResult(std::string &result, const std::string &key) {
          ptree pt;
          int ret = 0;
          try {
              std::stringstream ss(result);
              read_json(ss, pt);

              boost::optional<ptree&> child;
              child = pt.get_child_optional("ret");
              if (!child)
              {
                  return "json is not have ret";
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

              return data.get<std::string>(key);
          }
          catch (...)
          {
              EXPECT_EQ(0, 1);
              return "error";
          }
        }

    public:
        SGAgentHandler sg_agent_handler_;

        string appkey_;
        string appkey_1;
        proc_conf_param_t conf;
};

TEST_F(SgMtConfigTimeout, GetTimeout) {
    int ret = 0;
    std::string result = "";
    sg_agent_handler_.client_->getConfig(result, conf); 
    std::string key = "octo";
    EXPECT_STREQ("first", parseConfigResult(result, key).c_str());

    const time_t end_time = time(NULL);
    EXPECT_TRUE(end_time - start_time_ <= 0.5);
}

TEST_F(SgMtConfigTimeout, update_Timeout)
{
    srand(time(NULL));
    std::string value = "change";
    std::string key = "octo";

    std::string result = "{\"" + key + "\"" + ":\"" + value + "\"}";
    conf.__set_conf(result);
    EXPECT_EQ(0, sg_agent_handler_.client_->setConfig(conf)); 

    time_t end_time = time(NULL);
    do
    {
        sg_agent_handler_.client_->getConfig(result, conf); 
        sleep(0.8);
        end_time = time(NULL);
    }while(value != parseConfigResult(result, key));
		std::cout<<"the diff time ="<<end_time-start_time_<<std::cout<<endl;
		EXPECT_TRUE(end_time - start_time_ <= 2);

    //还原值
    sleep(1);
    value = "first";
    result = "{\"" + key + "\"" + ":\"" + value + "\"}";
    conf.__set_conf(result);
    EXPECT_EQ(0, sg_agent_handler_.client_->setConfig(conf)); 
}
