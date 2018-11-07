#include "interface_base.h"
#include "test_sg_agent.h"
#include <gtest/gtest.h>
#include <boost/property_tree/ptree.hpp>
#include <boost/optional/optional.hpp>
#include <boost/property_tree/json_parser.hpp>
using boost::property_tree::ptree;

class SgMtConfigInterfaceTest: public InterfaceBase
{
    protected:
        virtual void SetUp()
        {
           appkey_1 = "com.sankuai.inf.mcc_test";
           start_time_ = time(NULL);

           conf.appkey = appkey_1;
           conf.env = "stage";
           conf.path = "/";

           sg_agent_handler_.init(appkey_s, ip_, port_);
        }

        virtual void TearDown()
        {
        }

        SGAgentHandler sg_agent_handler_;

        string appkey_;
        string appkey_1;
        proc_conf_param_t conf;
};

TEST_F(SgMtConfigInterfaceTest, Get) {
    int ret = 0;
    std::string result = "";
    sg_agent_handler_.client_->getConfig(result, conf); 

    ptree pt;
    try {
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

        std::string octo = data.get<std::string>("octo");
        EXPECT_STREQ(octo.c_str(), "222");

        std::string xzj_test = data.get<std::string>("xzj_test");
        EXPECT_STREQ(xzj_test.c_str(), "bbb");

        std::string bbb = data.get<std::string>("bbb");
        EXPECT_STREQ(bbb.c_str(), "bbb");

        std::string test = data.get<std::string>("test");
        EXPECT_STREQ(test.c_str(), "test1");

    }
    catch (...)
    {
        EXPECT_EQ(0, 1);
        return;
    }
}

TEST_F(SgMtConfigInterfaceTest, GetAndSetAndGet)
{
    int ret = 0;
    std::string result = "";
    sg_agent_handler_.client_->getConfig(result, conf); 

    ptree pt;
    try {
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

        std::string octo = data.get<std::string>("octo");
        EXPECT_STREQ(octo.c_str(), "222");

        std::string xzj_test = data.get<std::string>("xzj_test");
        EXPECT_STREQ(xzj_test.c_str(), "bbb");

        std::string bbb = data.get<std::string>("bbb");
        EXPECT_STREQ(bbb.c_str(), "bbb");

        std::string test = data.get<std::string>("test");
        EXPECT_STREQ(test.c_str(), "test1");

    }
    catch (...)
    {
        EXPECT_EQ(0, 1);
        return;
    }

    result = "{\"octo\":\"222\",\"xzj_test\":\"bbb\",\"bbb\":\"bbb\",\"test\":\"valueischanged\"}";
    conf.__set_conf(result);
    EXPECT_EQ(0, sg_agent_handler_.client_->setConfig(conf)); 
    sleep(5);

    sg_agent_handler_.client_->getConfig(result, conf); 

    try {
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
            std::string msg = pt.get<std::string>("msg");
            EXPECT_STREQ(msg.c_str(), "success");
        }


        child = pt.get_child_optional("data");
        if (!child)
        {
            EXPECT_EQ(0, 1);
        }

        ptree data = pt.get_child("data");

        std::string octo = data.get<std::string>("octo");
        EXPECT_STREQ(octo.c_str(), "222");
	
        std::string bbb = data.get<std::string>("bbb");
        EXPECT_STREQ(bbb.c_str(), "bbb");
        
				std::string xzj_test = data.get<std::string>("xzj_test");
        EXPECT_STREQ(xzj_test.c_str(), "xxoo");


        std::string test = data.get<std::string>("test");
        EXPECT_STREQ(test.c_str(), "valueischanged");

    }
    catch (...)
    {
        EXPECT_EQ(0, 1);
        return;
    }
}

TEST_F(SgMtConfigInterfaceTest, Set) {
    string result = "{\"octo\":\"222\",\"xzj_test\":\"xxoo\",\"bbb\":\"bbb\",\"test\":\"valueischanged\"}";
    conf.__set_conf(result);
    EXPECT_EQ(0, sg_agent_handler_.client_->setConfig(conf)); 
}


TEST_F(SgMtConfigInterfaceTest, SetAndGet)
{
    std::string result = "{\"octo\":\"222\",\"xzj_test\":\"bbb\",\"bbb\":\"bbb\",\"test\":\"test1\"}";
    conf.__set_conf(result);
    EXPECT_EQ(0, sg_agent_handler_.client_->setConfig(conf)); 
    sleep(5);

    sg_agent_handler_.client_->getConfig(result, conf); 
    ptree pt;
    int ret = 0;
    try {
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
            std::string msg = pt.get<std::string>("msg");
            EXPECT_STREQ(msg.c_str(), "success");
        }


        child = pt.get_child_optional("data");
        if (!child)
        {
            EXPECT_EQ(0, 1);
        }

        ptree data = pt.get_child("data");

        std::string octo = data.get<std::string>("octo");
        EXPECT_STREQ(octo.c_str(), "222");
        std::string xzj_test = data.get<std::string>("xzj_test");
        EXPECT_STREQ(xzj_test.c_str(), "bbb");

        std::string bbb = data.get<std::string>("bbb");
        EXPECT_STREQ(bbb.c_str(), "bbb");
        std::string test = data.get<std::string>("test");
        EXPECT_STREQ(test.c_str(), "test1");

    }
    catch (...)
    {
        EXPECT_EQ(0, 1);
        return;
    }

}


TEST_F(SgMtConfigInterfaceTest, updateConfig)
{
    ConfigNode node;
    node.__set_appkey(appkey_1);
    node.__set_env("stage");
    node.__set_path("/");

    std::vector<ConfigNode> nodes;
    nodes.push_back(node);
    ConfigUpdateRequest req;
    req.__set_nodes(nodes);
    std::cout << "nodes' size = " << nodes.size() << std::endl;

    EXPECT_EQ(0, sg_agent_handler_.client_->updateConfig(req)); 
}

