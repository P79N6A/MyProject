#include <limits.h>
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include "interface_base.h"
#include "cJSON.h"

class SgAgentAuth: public InterfaceBase
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

        int _json2status(const std::string &strJson)
        {
            cJSON* root = cJSON_Parse(strJson.c_str());
            if (NULL == root)
            {
                return -1;
            }
            int status = cJSON_GetObjectItem(root, "env")->valueint;
            return status; 
        }

        SGAgentHandler sg_agent_handler_;
};

TEST_F(SgAgentAuth, getAuthorizedConsumers)
{
    string content = "{\r\n  \"user\" : \"chenxin11(chenxin11)\",\r\n  \"updateTime\" : 1445910609,\r\n  \"status\" : 1,\r\n  \"ips\" : [ \"10.4.243.122\", \"10.4.243.211\" ]\r\n}";
    
    string result = "";
    sg_agent_handler_.client_->getAuthorizedConsumers(result, appkey_s);
    EXPECT_STREQ(content.c_str(), result.c_str());

    usleep(10000);

    string result_1 = "";
    sg_agent_handler_.client_->getAuthorizedConsumers(result_1, appkey_s);
    EXPECT_STREQ(content.c_str(), result_1.c_str());
};

TEST_F(SgAgentAuth, getAuthorizedProviders)
{
    string content = "{\r\n  \"user\" : \"chenxin11(chenxin11)\",\r\n  \"updateTime\" : 1445910959,\r\n  \"status\" : 1,\r\n  \"ips\" : [ \"10.4.243.121\", \"192.168.4.252\" ]\r\n}";
    string result = "";
    sg_agent_handler_.client_->getAuthorizedProviders(result, appkey_s); 
    EXPECT_STREQ(content.c_str(), result.c_str());

    usleep(10000);

    string result_1 = "";
    sg_agent_handler_.client_->getAuthorizedProviders(result_1, appkey_s);
    EXPECT_STREQ(content.c_str(), result_1.c_str());
};

TEST_F(SgAgentAuth, empty_node)
{
    string result = "";
    sg_agent_handler_.client_->getAuthorizedConsumers(result, "com.sankuai.mns.timeout.test");
    EXPECT_STREQ("", result.c_str());
};
